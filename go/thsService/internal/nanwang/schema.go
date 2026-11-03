package nanwang

import (
	"encoding/json"
	"errors"
	"fmt"
	"net/url"
	"strconv"
	"strings"
	"time"
	"unicode/utf8"
)

var ErrValidation = errors.New("南网业务参数无效")
var ErrConflict = errors.New("记录已被修改，请刷新后重试")

func invalid(s string) error { return fmt.Errorf("%w：%s", ErrValidation, s) }

type Field struct {
	Key      string `json:"key"`
	Label    string `json:"label"`
	Type     string `json:"type"`
	Required bool   `json:"required"`
}
type Schema struct {
	Kind      string  `json:"kind"`
	Label     string  `json:"label"`
	Public    bool    `json:"public"`
	Singleton bool    `json:"singleton"`
	Fields    []Field `json:"fields"`
}

var Schemas = []Schema{
	{"news", "资讯", true, false, []Field{{"title", "标题", "text", true}, {"classId", "分类编号", "text", true}, {"areaCode", "地区码", "text", true}, {"issuedName", "发布单位", "text", true}, {"source", "来源", "text", false}, {"publishTime", "发布时间", "datetime", true}, {"createTime", "创建时间", "datetime", true}, {"imgUrl", "封面图片地址", "url", false}, {"coverUrl", "备用封面地址", "url", false}, {"link", "外链", "url", false}, {"readCount", "阅读数", "integer", true}, {"content", "正文（纯文本）", "textarea", true}}},
	{"recommended", "推荐户号", false, false, customerFields(false)},
	{"bound", "已绑定户号", false, false, customerFields(false)},
	{"arrears", "欠费户号", false, false, customerFields(true)},
	{"points", "计量点", false, false, []Field{{"eleCustId", "户号", "text", true}, {"pointName", "计量点名称", "text", true}}},
	{"halls", "营业厅", true, false, []Field{{"hallName", "营业厅名称", "text", true}, {"address", "地址", "text", true}, {"distance", "模拟距离（公里）", "decimal", true}}},
	{"messages", "未读消息", false, false, []Field{{"title", "消息标题", "text", true}}},
	{"charging", "充电桩状态", false, true, []Field{{"hasPile", "是否有充电桩", "boolean", true}}},
}

func customerFields(arrears bool) []Field {
	f := []Field{{"userName", "户名", "text", true}, {"custName", "备用户名", "text", false}, {"eleCustId", "户号", "text", true}, {"custNo", "备用户号", "text", false}, {"address", "用电地址", "text", true}, {"eleAddr", "备用地址", "text", false}, {"eleCustNumberId", "户号记录编号", "text", true}}
	if arrears {
		f = append(f, Field{"arrears", "欠费金额", "decimal", true}, Field{"totalArrears", "欠费总额", "decimal", true})
	}
	return f
}
func Definition(kind string) (Schema, error) {
	for _, s := range Schemas {
		if s.Kind == kind {
			return s, nil
		}
	}
	return Schema{}, invalid("未知业务类型")
}
func Validate(kind string, p map[string]any) error {
	s, err := Definition(kind)
	if err != nil {
		return err
	}
	if p == nil {
		return invalid("数据不能为空")
	}
	allowed := map[string]bool{}
	for _, f := range s.Fields {
		allowed[f.Key] = true
		v, exists := p[f.Key]
		if !exists {
			if f.Required {
				return invalid(f.Label + "必填")
			}
			continue
		}
		switch f.Type {
		case "boolean":
			if _, ok := v.(bool); !ok {
				return invalid(f.Label + "须为布尔值")
			}
		case "integer":
			n, ok := v.(json.Number)
			if !ok {
				return invalid(f.Label + "须为整数")
			}
			i, e := strconv.ParseInt(string(n), 10, 64)
			if e != nil || i < 0 || i > 1000000000 {
				return invalid(f.Label + "超出范围")
			}
		default:
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
			if f.Type == "datetime" {
				if _, e := time.Parse("2006-01-02 15:04:05", text); e != nil {
					return invalid(f.Label + "格式须为YYYY-MM-DD HH:mm:ss")
				}
			}
			if f.Type == "url" && text != "" {
				u, e := url.Parse(text)
				if e != nil || u.Host == "" || (u.Scheme != "https" && u.Scheme != "http") {
					return invalid("地址须为http或https URL")
				}
			}
			if f.Type == "decimal" {
				if !decimalPattern.MatchString(text) {
					return invalid(f.Label + "须为非负数，最多9位整数2位小数")
				}
			}
		}
	}
	for k := range p {
		if !allowed[k] {
			return invalid("不支持字段 " + k)
		}
	}
	return nil
}
