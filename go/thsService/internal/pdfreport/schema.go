package pdfreport

import (
	"fmt"
	"math/big"
	"regexp"
	"strings"
	"time"
	"unicode/utf8"
)

type Field struct {
	Key      string `json:"key"`
	Label    string `json:"label"`
	Type     string `json:"type"`
	Required bool   `json:"required"`
}
type Table struct {
	Key     string  `json:"key"`
	Label   string  `json:"label"`
	Columns []Field `json:"columns"`
}
type Schema struct {
	Kind   string  `json:"kind"`
	Label  string  `json:"label"`
	Fields []Field `json:"fields"`
	Tables []Table `json:"tables"`
}
type Draft struct {
	Kind     string                         `json:"kind"`
	UserID   uint64                         `json:"userId"`
	Fields   map[string]string              `json:"fields"`
	Tables   map[string][]map[string]string `json:"tables"`
	Warnings []string                       `json:"warnings,omitempty"`
}

func f(k, l, t string, required bool) Field { return Field{k, l, t, required} }

var Schemas = []Schema{
	{"work", "工作信息报表", []Field{f("name", "姓名", "text", true), f("sex", "性别", "text", false), f("idNumber", "证件号码", "text", false), f("department", "部门", "text", false), f("post", "职务", "text", false), f("startDate", "入职日期", "date", false), f("endDate", "离职日期（在职可留空）", "date", false), f("monthlyIncome", "月均收入（元）", "money", true), f("companyName", "单位名称", "text", true), f("companyAddress", "单位地址", "text", false), f("issuedDate", "生成日期", "date", true)}, []Table{}},
	{"electricity", "用电账单报表", []Field{f("name", "户名", "text", true), f("accountNo", "户号", "text", false), f("address", "用电地址", "text", false), f("supplier", "供电单位", "text", false), f("meterNo", "电能表编号", "text", false), f("usageType", "用电类别", "text", false), f("voltage", "电压等级", "text", false), f("startDate", "账期开始", "date", true), f("endDate", "账期结束", "date", true), f("readDate", "抄表日期", "date", false), f("totalUsage", "本期电量（无抄表明细时使用）", "decimal", true), f("issuedDate", "打印日期", "date", true)}, []Table{
		{"readings", "电量明细", []Field{f("label", "示数类型", "text", true), f("previous", "上期示数", "decimal", true), f("current", "本期示数", "decimal", true), f("multiplier", "倍率", "decimal", true)}},
		{"fees", "电费明细", []Field{f("label", "费用组成", "text", true), f("quantity", "计费电量", "decimal", false), f("price", "计费标准（为空则手填金额）", "decimal", false), f("amount", "电费（有标准时自动计算）", "money", true)}},
	}},
	{"social", "社保缴费记录报表", []Field{f("name", "姓名", "text", true), f("idNumber", "证件号码", "text", false), f("manageCode", "个人管理码", "text", false), f("city", "参保城市", "text", false), f("issuedDate", "生成日期", "date", true)}, []Table{
		{"coverage", "参保基本情况", []Field{f("kind", "参保险种/口径", "text", true), f("startMonth", "参保开始年月", "month", false), f("months", "累计缴费月数", "integer", false), f("status", "参保状态", "text", false)}},
		{"payments", "缴费记录（单位及个人合计）", []Field{f("month", "缴费年月", "month", true), f("companyNo", "单位编号", "text", false), f("company", "单位名称", "text", true), f("base", "缴费基数", "money", true), f("companyPay", "单位缴费", "money", true), f("personalPay", "个人缴费", "money", true)}},
		{"details", "分险种明细（可选，不从合计推算）", []Field{f("month", "缴费年月", "month", true), f("kind", "险种", "text", true), f("base", "缴费基数", "money", true), f("companyPay", "单位缴费", "money", true), f("personalPay", "个人缴费", "money", true)}},
	}},
}

func Definition(kind string) (Schema, error) {
	for _, s := range Schemas {
		if s.Kind == kind {
			return s, nil
		}
	}
	return Schema{}, fmt.Errorf("不支持的报表类型")
}

var decimalPattern = regexp.MustCompile(`^[0-9]{1,9}(\.[0-9]{1,8})?$`)
var moneyPattern = regexp.MustCompile(`^[0-9]{1,9}(\.[0-9]{1,2})?$`)
var monthPattern = regexp.MustCompile(`^[0-9]{4}-(0[1-9]|1[0-2])$`)

func ParseAmount(s string) *big.Rat {
	r, ok := new(big.Rat).SetString(s)
	if !ok {
		return new(big.Rat)
	}
	return r
}
func Rounded(r *big.Rat) int64 {
	n := new(big.Int).Mul(r.Num(), big.NewInt(100))
	q, rem := new(big.Int), new(big.Int)
	q.QuoRem(n, r.Denom(), rem)
	if new(big.Int).Lsh(rem, 1).Cmp(r.Denom()) >= 0 {
		q.Add(q, big.NewInt(1))
	}
	return q.Int64()
}
func Money(n int64) string { return fmt.Sprintf("%d.%02d", n/100, n%100) }
func Fee(row map[string]string) int64 {
	if row["price"] != "" && row["calculation"] != "manual" {
		return Rounded(new(big.Rat).Mul(ParseAmount(row["price"]), ParseAmount(row["quantity"])))
	}
	return Rounded(ParseAmount(row["amount"]))
}
func Total(rows []map[string]string, key string) int64 {
	var sum int64
	for _, r := range rows {
		sum += Rounded(ParseAmount(r[key]))
	}
	return sum
}
func Usage(row map[string]string) *big.Rat {
	return new(big.Rat).Mul(new(big.Rat).Sub(ParseAmount(row["current"]), ParseAmount(row["previous"])), ParseAmount(row["multiplier"]))
}
func validateFields(values map[string]string, fields []Field) error {
	allowed := map[string]bool{}
	for _, field := range fields {
		allowed[field.Key] = true
		s := strings.TrimSpace(values[field.Key])
		values[field.Key] = s
		if field.Required && s == "" {
			return fmt.Errorf("请填写%s", field.Label)
		}
		if s == "" {
			continue
		}
		if !utf8.ValidString(s) || utf8.RuneCountInString(s) > 200 {
			return fmt.Errorf("%s最多200字", field.Label)
		}
		for _, r := range s {
			if r < 32 || r > 0xffff {
				return fmt.Errorf("%s含不支持的控制字符或特殊字符", field.Label)
			}
		}
		switch field.Type {
		case "money":
			if !moneyPattern.MatchString(s) {
				return fmt.Errorf("%s须为非负金额，最多两位小数", field.Label)
			}
		case "decimal":
			if !decimalPattern.MatchString(s) {
				return fmt.Errorf("%s须为非负数，最多八位小数", field.Label)
			}
		case "date":
			d, e := time.Parse("2006-01-02", s)
			if e != nil || d.Format("2006-01-02") != s || d.Year() < 1900 || d.Year() > 2100 {
				return fmt.Errorf("%s须为有效日期", field.Label)
			}
		case "month":
			if !monthPattern.MatchString(s) || s < "1900-01" || s > "2100-12" {
				return fmt.Errorf("%s须为YYYY-MM", field.Label)
			}
		case "integer":
			if !regexp.MustCompile(`^[0-9]{1,4}$`).MatchString(s) {
				return fmt.Errorf("%s须为0至9999的整数", field.Label)
			}
		}
	}
	for key := range values {
		if !allowed[key] {
			return fmt.Errorf("不支持的字段：%s", key)
		}
	}
	return nil
}
func Validate(d *Draft) error {
	s, e := Definition(d.Kind)
	if e != nil {
		return e
	}
	if d.UserID == 0 || d.Fields == nil || d.Tables == nil {
		return fmt.Errorf("会员、基本信息和明细不可缺少")
	}
	if e = validateFields(d.Fields, s.Fields); e != nil {
		return e
	}
	allowed := map[string]bool{}
	count := 0
	for _, table := range s.Tables {
		allowed[table.Key] = true
		for _, r := range d.Tables[table.Key] {
			if r == nil {
				return fmt.Errorf("明细行不能为空")
			}
			if e = validateFields(r, table.Columns); e != nil {
				return fmt.Errorf("%s：%w", table.Label, e)
			}
			count++
		}
	}
	for key := range d.Tables {
		if !allowed[key] {
			return fmt.Errorf("不支持的明细：%s", key)
		}
	}
	if count > 600 {
		return fmt.Errorf("一次最多600行明细")
	}
	if d.Kind == "work" && d.Fields["endDate"] != "" && d.Fields["startDate"] != "" && d.Fields["endDate"] < d.Fields["startDate"] {
		return fmt.Errorf("离职日期不得早于入职日期")
	}
	if d.Kind == "electricity" {
		if d.Fields["endDate"] < d.Fields["startDate"] {
			return fmt.Errorf("账期结束不得早于开始")
		}
		if len(d.Tables["fees"]) == 0 {
			return fmt.Errorf("至少填写一条电费明细")
		}
		for _, r := range d.Tables["readings"] {
			if ParseAmount(r["current"]).Cmp(ParseAmount(r["previous"])) < 0 || ParseAmount(r["multiplier"]).Sign() <= 0 {
				return fmt.Errorf("本期示数不可低于上期，倍率须大于0")
			}
		}
		for _, r := range d.Tables["fees"] {
			if r["calculation"] != "" && r["calculation"] != "manual" && r["calculation"] != "auto" {
				return fmt.Errorf("电费计算方式须为manual或auto")
			}
			if r["price"] != "" && r["quantity"] == "" && r["calculation"] != "manual" {
				return fmt.Errorf("填写计费标准时须填写计费电量")
			}
			if r["price"] != "" && r["calculation"] != "manual" && new(big.Rat).Mul(ParseAmount(r["price"]), ParseAmount(r["quantity"])).Cmp(ParseAmount("999999999.99")) > 0 {
				return fmt.Errorf("电费金额超出范围")
			}
			r["amount"] = Money(Fee(r))
		}
	}
	if d.Kind == "social" {
		if len(d.Tables["payments"]) == 0 {
			return fmt.Errorf("至少填写一条缴费记录")
		}
		seen := map[string]bool{}
		sums := map[string][2]int64{}
		for _, r := range d.Tables["payments"] {
			key := r["month"] + "\x00" + r["company"]
			if seen[key] {
				return fmt.Errorf("同月同单位记录重复")
			}
			seen[key] = true
			v := sums[r["month"]]
			v[0] += Rounded(ParseAmount(r["companyPay"]))
			v[1] += Rounded(ParseAmount(r["personalPay"]))
			sums[r["month"]] = v
		}
		details := map[string][2]int64{}
		for _, r := range d.Tables["details"] {
			if _, ok := sums[r["month"]]; !ok {
				return fmt.Errorf("分险种明细月份必须在缴费记录中")
			}
			v := details[r["month"]]
			v[0] += Rounded(ParseAmount(r["companyPay"]))
			v[1] += Rounded(ParseAmount(r["personalPay"]))
			details[r["month"]] = v
		}
		for month, v := range details {
			if v != sums[month] {
				return fmt.Errorf("%s的分险种金额合计须与该月缴费记录一致", month)
			}
		}
	}
	return nil
}
