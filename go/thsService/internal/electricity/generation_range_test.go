package electricity

import (
	"math/big"
	"testing"
	"time"
)

func TestGeneratedBillPriceRange(t *testing.T) {
	for month := 1; month <= 12; month++ {
		start := time.Date(2026, time.Month(month), 1, 0, 0, 0, 0, profileZone)
		end := start.AddDate(0, 1, -1)
		low, high := generatedUsageRange(start, end)
		fee := func(usage int64) *big.Rat {
			v, _ := new(big.Rat).SetString(generatedBilling(usage, start, end).ElectricityFee)
			return v
		}
		if low > high || fee(low).Cmp(big.NewRat(200, 1)) < 0 || fee(high).Cmp(big.NewRat(450, 1)) > 0 {
			t.Fatal("invalid monthly range", month, low, high)
		}
		if fee(low-1).Cmp(big.NewRat(200, 1)) >= 0 || fee(high+1).Cmp(big.NewRat(450, 1)) <= 0 {
			t.Fatal("range boundary not exact", month)
		}
	}
	rows, err := generateBills(1, time.Now())
	if err != nil {
		t.Fatal(err)
	}
	if len(rows) != 20 {
		t.Fatal("bill count")
	}
	for _, row := range rows {
		amount, ok := new(big.Rat).SetString(row.Amount)
		if !ok || amount.Cmp(big.NewRat(200, 1)) < 0 || amount.Cmp(big.NewRat(450, 1)) > 0 {
			t.Fatal("bill outside target", row.Amount)
		}
		usage, _ := new(big.Rat).SetString(row.TotalUsage)
		start, _ := time.Parse("2006-01-02", row.StartDate)
		end, _ := time.Parse("2006-01-02", row.EndDate)
		if calculated := generatedBilling(new(big.Rat).Mul(usage, big.NewRat(100, 1)).Num().Int64(), start, end); calculated.ElectricityFee != row.Amount {
			t.Fatal("usage and amount differ")
		}
	}
}
