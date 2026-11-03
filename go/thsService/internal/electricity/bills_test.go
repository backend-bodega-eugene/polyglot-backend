package electricity

import (
	"encoding/json"
	"testing"
	"time"
)

func TestGeneratedBillsDatesAndAmounts(t *testing.T) {
	now := time.Date(2024, 3, 1, 0, 1, 0, 0, profileZone)
	rows, err := generateBills(7, now)
	if err != nil {
		t.Fatal(err)
	}
	if len(rows) != 20 || rows[0].Code != "202402" || rows[0].EndDate != "2024-02-29" || rows[19].Code != "202207" {
		t.Fatal(rows)
	}
	for _, b := range rows {
		if BillingForBill(b).ElectricityFee != b.Amount {
			t.Fatal("billing total mismatch")
		}
		if err := validateBill(BillWrite{Code: b.Code, StartDate: b.StartDate, EndDate: b.EndDate, Amount: b.Amount, TotalUsage: b.TotalUsage, Detail: json.RawMessage(b.Detail)}); err != nil {
			t.Fatal(err)
		}
	}
}
func TestBillValidation(t *testing.T) {
	valid := BillWrite{Code: "202601", StartDate: "2026-01-01", EndDate: "2026-01-31", Amount: "100.00", TotalUsage: "200.00", Detail: json.RawMessage(`{}`)}
	for _, change := range []func(*BillWrite){func(r *BillWrite) { r.Code = "202613" }, func(r *BillWrite) { r.Amount = "-1" }, func(r *BillWrite) { r.TotalUsage = "1.001" }, func(r *BillWrite) { r.EndDate = "2025-12-31" }, func(r *BillWrite) { r.Detail = json.RawMessage(`"string"`) }, func(r *BillWrite) { r.Detail = json.RawMessage(`null`) }} {
		r := valid
		change(&r)
		if err := validateBill(r); err == nil {
			t.Fatal("invalid bill accepted")
		}
	}
}
