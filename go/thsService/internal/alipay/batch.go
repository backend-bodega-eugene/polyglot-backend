package alipay

import (
	"context"
	"crypto/rand"
	"encoding/json"
	"strings"
	"time"

	"gorm.io/gorm"
)

// ContributionRule uses decimal strings: rates are percentages, amounts are yuan.
type ContributionRule struct {
	Kind         string `json:"kind"` // pension, medical, unemployment, injury, maternity
	Base         string `json:"base"` // Approved contribution base; empty uses salary.
	CompanyRate  string `json:"companyRate"`
	PersonalRate string `json:"personalRate"`
}
type GenerateContributions struct {
	UserID     uint64             `json:"userId"`
	StartMonth string             `json:"startMonth"` // YYYY-MM, inclusive.
	EndMonth   string             `json:"endMonth"`   // YYYY-MM, inclusive; no future months.
	Salary     string             `json:"salary"`
	Company    string             `json:"company"`
	Rules      []ContributionRule `json:"rules"`
}
type ContributionCalculation struct {
	Kind         string `json:"kind"`
	Base         string `json:"base"`
	CompanyRate  string `json:"companyRate"`
	PersonalRate string `json:"personalRate"`
	CompanyPay   string `json:"companyPay"`
	PersonalPay  string `json:"personalPay"`
}
type ContributionPreview struct {
	Months      int                       `json:"months"`
	CompanyPay  string                    `json:"companyPay"`  // Monthly employer total.
	PersonalPay string                    `json:"personalPay"` // Monthly employee total.
	Total       string                    `json:"total"`       // Monthly five-insurance total, excluding housing fund.
	Breakdown   []ContributionCalculation `json:"breakdown"`
	Created     int                       `json:"created"`
	Skipped     int                       `json:"skipped"`
}

func CalculateContributions(w GenerateContributions) (ContributionPreview, error) {
	out := ContributionPreview{}
	start, e1 := time.Parse("2006-01", w.StartMonth)
	end, e2 := time.Parse("2006-01", w.EndMonth)
	if e1 != nil || e2 != nil || start.Format("2006-01") != w.StartMonth || end.Format("2006-01") != w.EndMonth || start.Year() < 2000 || start.After(end) || w.EndMonth > time.Now().In(Zone).Format("2006-01") {
		return out, invalid("月份须为2000年起至本月的YYYY-MM，开始月份不得晚于结束月份")
	}
	out.Months = (end.Year()-start.Year())*12 + int(end.Month()-start.Month()) + 1
	if out.Months > 120 {
		return out, invalid("一次最多生成120个月")
	}
	if w.UserID == 0 || !moneyPattern.MatchString(w.Salary) || cents(w.Salary) <= 0 || len(strings.TrimSpace(w.Company)) == 0 || len([]rune(strings.TrimSpace(w.Company))) > 100 {
		return out, invalid("请填写会员、正数月薪和单位名称（最多100字）")
	}
	if len(w.Rules) != 5 {
		return out, invalid("须提交养老、医疗、失业、工伤、生育五项规则")
	}
	allowed := map[string]bool{"pension": true, "medical": true, "unemployment": true, "injury": true, "maternity": true}
	var company, personal int64
	for _, r := range w.Rules {
		if !allowed[r.Kind] {
			return out, invalid("险种重复或无效")
		}
		delete(allowed, r.Kind)
		base := r.Base
		if base == "" {
			base = w.Salary
		}
		if !moneyPattern.MatchString(base) || cents(base) <= 0 || !moneyPattern.MatchString(r.CompanyRate) || !moneyPattern.MatchString(r.PersonalRate) || cents(r.CompanyRate) > 10000 || cents(r.PersonalRate) > 10000 {
			return out, invalid("缴费基数须大于0，费率须在0至100之间，最多两位小数")
		}
		if (r.Kind == "injury" || r.Kind == "maternity") && cents(r.PersonalRate) != 0 {
			return out, invalid("工伤和生育保险个人费率必须为0")
		}
		c := (cents(base)*cents(r.CompanyRate) + 5000) / 10000
		p := (cents(base)*cents(r.PersonalRate) + 5000) / 10000
		company += c
		personal += p
		out.Breakdown = append(out.Breakdown, ContributionCalculation{r.Kind, money(cents(base)), money(cents(r.CompanyRate)), money(cents(r.PersonalRate)), money(c), money(p)})
	}
	// Existing App/admin amounts have the same nine-digit integer limit.
	if company > 99999999999 || personal > 99999999999 {
		return out, invalid("缴费金额超出限制")
	}
	out.CompanyPay = money(company)
	out.PersonalPay = money(personal)
	out.Total = money(company + personal)
	return out, nil
}

// GenerateInsurance adds only missing months for this member, regardless of company.
// A single transaction and the existing member/scope locks serialize manual and App writes.
func (s *Service) GenerateInsurance(ctx context.Context, admin uint64, w GenerateContributions, preview bool) (ContributionPreview, error) {
	out, e := CalculateContributions(w)
	if e != nil {
		return out, e
	}
	e = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		return lock(tx, "records", w.UserID, 0, false, func() error {
			var existing []Record
			if e := tx.Where("kind=? AND user_id=?", "records", w.UserID).Find(&existing).Error; e != nil {
				return e
			}
			occupied := map[string]bool{}
			for _, r := range existing {
				if m, ok := payload(r)["month"].(string); ok {
					occupied[m] = true
				}
			}
			start, _ := time.Parse("2006-01", w.StartMonth)
			for i := 0; i < out.Months; i++ {
				d := start.AddDate(0, i, 0)
				month := d.Format("200601")
				if occupied[month] {
					out.Skipped++
					continue
				}
				out.Created++
				if preview {
					continue
				}
				scope := Scope{"records", w.UserID, d.Year()}
				// lock already holds the member row, matching every other writer.
				if e := tx.Where(scope).FirstOrCreate(&scope).Error; e != nil {
					return e
				}
				p := map[string]any{"month": month, "company": strings.TrimSpace(w.Company), "type": "正常核定", "base": money(cents(w.Salary)), "identity": "城镇职工", "companyPay": out.CompanyPay, "personalPay": out.PersonalPay}
				if e := Validate("records", d.Year(), p); e != nil {
					return e
				}
				b, e := json.Marshal(p)
				if e != nil {
					return e
				}
				record := Record{Kind: "records", UserID: w.UserID, Year: d.Year(), Payload: b, Version: rand.Text()}
				if e = tx.Create(&record).Error; e != nil {
					return e
				}
				if e = audit(tx, &admin, "admin_generate", nil, &record); e != nil {
					return e
				}
			}
			return nil
		})
	})
	return out, e
}
