package alipay

import (
	"errors"
	"fmt"
	"regexp"
	"strconv"
	"strings"
	"time"
	"unicode/utf8"
)

var ErrValidation = errors.New("支付宝模拟资料无效")
var ErrConflict = errors.New("记录已存在或被修改，请刷新重试")

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

var Schemas = []Schema{
	{"profile", "我的与账户", false, false, []Field{{"name", "姓名", "text", true}, {"phone", "手机号", "text", false}, {"accountName", "账户展示姓名", "text", true}, {"idNo", "展示证件号", "text", true}}},
	{"insurance", "参保资料", false, false, []Field{{"manageCode", "个人编号", "text", true}, {"cityName", "参保城市", "text", true}, {"status", "参保状态", "text", true}, {"interestAmount", "模拟利息", "money", true}}},
	{"records", "社保缴费记录及详情", false, true, []Field{{"month", "费款所属期 YYYYMM", "month", true}, {"company", "单位名称", "text", true}, {"type", "核定类型", "text", true}, {"base", "缴费基数", "money", true}, {"identity", "参保身份", "text", true}, {"companyPay", "单位缴纳", "money", true}, {"personalPay", "个人缴纳", "money", true}}},
	{"accounts", "生活缴费户号", false, false, []Field{{"leftName", "左侧户名", "text", true}, {"leftAccountNo", "左侧户号", "text", true}, {"rightName", "右侧户名（可空）", "text", false}, {"rightAccountNo", "右侧户号（可空）", "text", false}}},
	{"electric", "电费", false, false, []Field{{"payee", "收款单位", "text", true}, {"amount", "应缴金额", "money", true}, {"accountNo", "户号", "text", true}, {"accountName", "户名", "text", true}, {"address", "用电地址", "text", true}, {"supplier", "供电单位", "text", true}}},
	{"water", "水费账单", false, false, []Field{{"amount", "应缴金额", "money", true}, {"accountNo", "户号", "text", true}, {"accountName", "户名", "text", true}, {"addressLine1", "地址第一行", "text", true}, {"addressLine2", "地址第二行（可空）", "text", false}, {"supplier", "供水单位", "text", true}}},
}

func Definition(kind string) (Schema, error) {
	for _, s := range Schemas {
		if s.Kind == kind {
			return s, nil
		}
	}
	return Schema{}, invalid("未知业务类型")
}
func ValidYear(year int) bool { return year >= 2000 && year <= time.Now().In(Zone).Year() }

var moneyPattern = regexp.MustCompile(`^[0-9]{1,9}(\.[0-9]{1,2})?$`)
var phonePattern = regexp.MustCompile(`^1[3-9][0-9]{9}$`)

func cents(s string) int64 {
	p := strings.SplitN(s, ".", 2)
	n, _ := strconv.ParseInt(p[0], 10, 64)
	f := "00"
	if len(p) == 2 {
		f = (p[1] + "00")[:2]
	}
	decimal, _ := strconv.ParseInt(f, 10, 64)
	return n*100 + decimal
}
func money(n int64) string { return fmt.Sprintf("%d.%02d", n/100, n%100) }
func Validate(kind string, year int, p map[string]any) error {
	def, e := Definition(kind)
	if e != nil {
		return e
	}
	if def.Yearly && !ValidYear(year) {
		return invalid("年度须为2000年至当前年份")
	}
	if !def.Yearly && year != 0 {
		return invalid("此业务不按年度保存")
	}
	if p == nil {
		return invalid("业务字段必填")
	}
	allowed := map[string]bool{}
	for _, f := range def.Fields {
		allowed[f.Key] = true
		v, ok := p[f.Key]
		if !ok {
			if f.Required {
				return invalid(f.Label + "必填")
			}
			p[f.Key] = ""
			continue
		}
		text, ok := v.(string)
		if !ok {
			return invalid(f.Label + "须为字符串")
		}
		text = strings.TrimSpace(text)
		p[f.Key] = text
		if (f.Required && text == "") || utf8.RuneCountInString(text) > 255 {
			return invalid(f.Label + "长度无效")
		}
		if f.Type == "money" {
			if !moneyPattern.MatchString(text) {
				return invalid(f.Label + "须为非负金额，最多9位整数2位小数")
			}
			p[f.Key] = money(cents(text))
		}
		if f.Type == "month" {
			d, e := time.Parse("200601", text)
			if e != nil || d.Format("200601") != text || d.Year() != year {
				return invalid("所属期须为与年度一致的YYYYMM")
			}
		}
	}
	for k := range p {
		if !allowed[k] {
			return invalid("不支持字段 " + k)
		}
	}
	if kind == "profile" {
		phone := p["phone"].(string)
		if phone != "" && !phonePattern.MatchString(phone) {
			return invalid("手机号格式无效")
		}
	}
	if kind == "accounts" {
		if (p["rightName"] == "") != (p["rightAccountNo"] == "") {
			return invalid("右侧户名和户号须同时填写或同时留空")
		}
	}
	return nil
}
