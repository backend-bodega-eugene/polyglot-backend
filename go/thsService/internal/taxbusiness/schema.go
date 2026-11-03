package taxbusiness

import (
	"encoding/json"
	"errors"
	"fmt"
	"net/url"
	"regexp"
	"strconv"
	"strings"
	"time"
	"unicode/utf8"
)

var ErrValidation = errors.New("所得税业务参数无效")
var ErrConflict = errors.New("记录已被修改或已存在，请刷新重试")

func invalid(s string) error { return fmt.Errorf("%w：%s", ErrValidation, s) }

var Zone = time.FixedZone("Asia/Shanghai", 28800)

type Field struct {
	Key      string `json:"key"`
	Label    string `json:"label"`
	Type     string `json:"type"`
	Required bool   `json:"required"`
}
type Schema struct {
	Kind   string  `json:"kind"`
	Label  string  `json:"label"`
	Public bool    `json:"public"`
	Yearly bool    `json:"yearly"`
	Fields []Field `json:"fields"`
}

var MonthlyFields = []Field{
	{"rate", "模拟税率（如10%）", "text", true}, {"susuanCount", "速算扣除数", "money", true}, {"type", "类型编号", "integer", true},
	{"zhaiyao", "摘要", "text", true}, {"expect_date", "所属期 YYYY-MM", "month", true}, {"shenbao_date", "申报日期 YYYY-MM-DD", "date", true}, {"tax_name", "所得项目", "text", true}, {"channel", "申报渠道", "text", true}, {"xiaolei", "所得小类", "text", true}, {"obligor_name", "扣缴义务人", "text", true}, {"tax_num", "义务人识别号", "text", true}, {"tax_authority", "主管税务机关", "text", true}, {"company_name", "单位名称", "text", true}, {"company_num", "单位编号", "text", true}, {"tax_company", "任职税务单位", "text", true}, {"post", "职务", "text", true}, {"start_date", "任职日期 YYYY-MM-DD", "date", true}, {"end_date", "离职日期（在职留空）", "date", false},
	{"income", "收入", "money", true}, {"tax", "已缴税额 / 本期申报税额", "money", true}, {"tax_free_income", "免税收入", "money", true}, {"deduction_money", "减除费用", "money", true}, {"yanglao", "养老保险", "money", true}, {"yiliao", "医疗保险", "money", true}, {"shiye", "失业保险", "money", true}, {"gjj", "公积金", "money", true}, {"other_deduction", "其他扣除", "money", true}, {"donate_project", "准予扣除捐赠", "money", true},
	{"additional_deduction", "专项附加扣除", "money", true}, {"personal_pension", "个人养老金", "money", true}, {"tax_reduction", "减免税额", "money", true},
}
var Schemas = []Schema{{"monthly", "年度收入纳税明细 / 计算来源", false, true, MonthlyFields}, {"applications", "年度汇算申报", false, true, []Field{{"refund_amount", "模拟退税金额", "money", true}, {"status", "申报状态", "text", true}}}, {"openrecords", "纳税记录开具记录", false, false, []Field{{"create_time", "开具时间 YYYY-MM-DD HH:mm:ss", "datetime", true}, {"start_date", "开始所属期 YYYY-MM", "month", true}, {"end_date", "结束所属期 YYYY-MM", "month", true}, {"images_data", "图片地址（逗号分隔，可空）", "textarea", false}, {"yzm", "扫码验证码", "text", true}}}, {"messages", "消息", false, false, []Field{{"title", "标题", "text", true}, {"content", "内容", "textarea", true}, {"create_time", "时间 YYYY-MM-DD HH:mm:ss", "datetime", true}}}}

func Definition(kind string) (Schema, error) {
	for _, s := range Schemas {
		if s.Kind == kind {
			return s, nil
		}
	}
	return Schema{}, invalid("未知业务类型")
}
func ValidYear(y int) bool { return y >= 2019 && y <= time.Now().In(Zone).Year() }
func Month(s string) (time.Time, error) {
	d, e := time.ParseInLocation("2006-01", s, Zone)
	if e != nil || d.Format("2006-01") != s || !ValidYear(d.Year()) {
		return time.Time{}, invalid("所属期须为2019年至当前年份的YYYY-MM")
	}
	return d, nil
}

var moneyPattern = regexp.MustCompile(`^[0-9]{1,9}(\.[0-9]{1,2})?$`)

func cents(s string) int64 {
	parts := strings.SplitN(s, ".", 2)
	n, _ := strconv.ParseInt(parts[0], 10, 64)
	frac := "00"
	if len(parts) == 2 {
		frac = (parts[1] + "00")[:2]
	}
	f, _ := strconv.ParseInt(frac, 10, 64)
	return n*100 + f
}
func money(n int64) string { return fmt.Sprintf("%d.%02d", n/100, n%100) }
func Validate(kind string, year int, p map[string]any) error {
	s, e := Definition(kind)
	if e != nil {
		return e
	}
	if s.Yearly && !ValidYear(year) {
		return invalid("年度无效")
	}
	if !s.Yearly && year != 0 {
		return invalid("此业务不按年度分组")
	}
	if p == nil {
		return invalid("业务数据必填")
	}
	allowed := map[string]bool{}
	for _, f := range s.Fields {
		allowed[f.Key] = true
		v, ok := p[f.Key]
		if !ok {
			if f.Required {
				return invalid(f.Label + "必填")
			}
			p[f.Key] = ""
			continue
		}
		if f.Type == "integer" {
			n, ok := v.(json.Number)
			if !ok {
				return invalid(f.Label + "须为整数")
			}
			i, e := strconv.Atoi(string(n))
			if e != nil || i < 1 || i > 1000 {
				return invalid(f.Label + "范围无效")
			}
			continue
		}
		text, ok := v.(string)
		if !ok {
			return invalid(f.Label + "须为字符串")
		}
		text = strings.TrimSpace(text)
		p[f.Key] = text
		max := 255
		if f.Type == "textarea" {
			max = 20000
		}
		if utf8.RuneCountInString(text) > max || (f.Required && text == "") {
			return invalid(f.Label + "长度无效")
		}
		switch f.Type {
		case "money":
			if !moneyPattern.MatchString(text) {
				return invalid(f.Label + "须为非负金额，最多9位整数2位小数")
			}
			p[f.Key] = money(cents(text))
		case "month":
			if _, e = Month(text); e != nil {
				return e
			}
		case "date":
			if text != "" {
				if _, e = time.Parse("2006-01-02", text); e != nil {
					return invalid(f.Label + "格式无效")
				}
			}
		case "datetime":
			if _, e = time.ParseInLocation("2006-01-02 15:04:05", text, Zone); e != nil {
				return invalid(f.Label + "格式无效")
			}
		}
	}
	for k := range p {
		if !allowed[k] {
			return invalid("不支持字段 " + k)
		}
	}
	if kind == "monthly" {
		rate := strings.TrimSuffix(p["rate"].(string), "%")
		n, e := strconv.ParseFloat(rate, 64)
		if !strings.HasSuffix(p["rate"].(string), "%") || e != nil || !(n >= 0 && n <= 100) {
			return invalid("模拟税率须为0%至100%")
		}
		d, _ := Month(p["expect_date"].(string))
		if d.Year() != year {
			return invalid("所属期与年度不一致")
		}
		if p["end_date"] != "" && p["end_date"].(string) < p["start_date"].(string) {
			return invalid("离职不能早于任职")
		}
	}
	if kind == "openrecords" {
		if p["end_date"].(string) < p["start_date"].(string) {
			return invalid("结束所属期不能早于开始")
		}
		images := p["images_data"].(string)
		if images != "" {
			for _, raw := range strings.Split(images, ",") {
				u, e := url.Parse(strings.TrimSpace(raw))
				if e != nil || u.Host == "" || (u.Scheme != "https" && u.Scheme != "http") {
					return invalid("图片地址须为http或https URL")
				}
			}
		}
	}
	return nil
}
