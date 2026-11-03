package alipay

import "testing"

func batchFixture() GenerateContributions {
	return GenerateContributions{UserID: 1, StartMonth: "2021-01", EndMonth: "2025-12", Salary: "20000", Company: "深圳市科技有限公司", Rules: []ContributionRule{
		{Kind: "pension", CompanyRate: "16", PersonalRate: "8"},
		{Kind: "medical", CompanyRate: "5", PersonalRate: "2"},
		{Kind: "unemployment", CompanyRate: "0.8", PersonalRate: "0.2"},
		{Kind: "injury", CompanyRate: "0.2", PersonalRate: "0"},
		{Kind: "maternity", CompanyRate: "0.5", PersonalRate: "0"},
	}}
}
func TestContributionCalculation(t *testing.T) {
	w := batchFixture()
	p, e := CalculateContributions(w)
	if e != nil || p.Months != 60 || p.CompanyPay != "4500.00" || p.PersonalPay != "2040.00" || p.Total != "6540.00" {
		t.Fatalf("%+v %v", p, e)
	}
	// Each insurer rounds separately; a separate approved base is supported.
	w.Rules[0].Base = "20000.01"
	w.Rules[3].Base = "12345.67"
	w.Rules[3].CompanyRate = "0.14"
	p, e = CalculateContributions(w)
	if e != nil || p.Breakdown[3].CompanyPay != "17.28" || p.Total != "6517.28" {
		t.Fatalf("rounding: %+v %v", p, e)
	}
}
func TestInvalidContributionCalculation(t *testing.T) {
	for _, change := range []func(*GenerateContributions){
		func(w *GenerateContributions) { w.StartMonth = "2026-02"; w.EndMonth = "2026-01" },
		func(w *GenerateContributions) { w.EndMonth = "2099-01" },
		func(w *GenerateContributions) { w.StartMonth = "2000-01" },
		func(w *GenerateContributions) { w.Salary = "NaN" },
		func(w *GenerateContributions) { w.Salary = "0" },
		func(w *GenerateContributions) { w.Rules[1].Kind = "pension" },
		func(w *GenerateContributions) { w.Rules[4].PersonalRate = "0.5" },
		func(w *GenerateContributions) { w.Rules[0].CompanyRate = "100.01" },
	} {
		w := batchFixture()
		change(&w)
		if _, e := CalculateContributions(w); e == nil {
			t.Fatalf("accepted %+v", w)
		}
	}
}
