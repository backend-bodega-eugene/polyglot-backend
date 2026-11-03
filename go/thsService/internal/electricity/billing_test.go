package electricity

import (
	"encoding/json"
	"fmt"
	"testing"
	"time"
)

func TestTierBoundariesAndSnapshots(t *testing.T) {
	for _, tc := range []struct {
		month      int
		usage      int64
		quantities [3]string
	}{
		{9, 36890, [3]string{"260.00", "108.90", "0.00"}},
		{9, 61049, [3]string{"260.00", "340.00", "10.49"}},
		{1, 45000, [3]string{"200.00", "200.00", "50.00"}},
		{5, 26000, [3]string{"260.00", "0.00", "0.00"}},
	} {
		start := time.Date(2026, time.Month(tc.month), 1, 0, 0, 0, 0, profileZone)
		v := generatedBilling(tc.usage, start, start.AddDate(0, 1, -1))
		for i, q := range tc.quantities {
			if v.Tiers[i].Usage != q {
				t.Fatalf("%+v", v)
			}
		}
		raw, _ := json.Marshal(map[string]any{"billing": v})
		req := BillWrite{Code: start.Format("200601"), StartDate: start.Format("2006-01-02"), EndDate: start.AddDate(0, 1, -1).Format("2006-01-02"), Amount: v.ElectricityFee, TotalUsage: fixedUsage(tc.usage), Detail: raw}
		if e := validateBill(req); e != nil {
			t.Fatal(e)
		}
		b := Bill{Amount: req.Amount, TotalUsage: req.TotalUsage, Detail: string(raw)}
		got := appBills([]BillView{billView(b)})[0].Billing
		if got.Tiers[1].Usage != tc.quantities[1] || got.LateFeeStartDate == "" {
			t.Fatal(got)
		}
		v.Tiers[0].Amount = "0.01"
		raw, _ = json.Marshal(map[string]any{"billing": v})
		req.Detail = raw
		if err := validateBill(req); err != nil {
			t.Fatalf("manual mismatched tier should be allowed: %v", err)
		}
	}
}

func fixedUsage(v int64) string { return fmt.Sprintf("%d.%02d", v/100, v%100) }

func TestLegacyBillingDoesNotInventDates(t *testing.T) {
	b := Bill{Amount: "128.56", TotalUsage: "210.00", Detail: `{"other":"retained"}`}
	v := BillingForBill(b)
	if v.ElectricityFee != "128.56" || v.DueDate != "" || v.LateFeeStartDate != "" || v.Tiers == nil || len(v.Tiers) != 0 {
		t.Fatal(v)
	}
}
