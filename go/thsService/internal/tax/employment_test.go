package tax

import (
	"strings"
	"testing"
	"time"
)

func TestEmploymentGenerationAndValidation(t *testing.T) {
	now := time.Date(2026, 10, 18, 12, 0, 0, 0, zone)
	for range 20 {
		e, err := generateEmployment(7, now)
		if err != nil {
			t.Fatal(err)
		}
		if e.UserID != 7 || e.Version == "" || e.EndDate != "" || !strings.HasPrefix(e.TaxID, "SIM-") {
			t.Fatal("invalid generated employment")
		}
		if err := validateEmployment(&e.EmploymentFields); err != nil {
			t.Fatal(err)
		}
	}
	for _, change := range []func(*EmploymentFields){func(f *EmploymentFields) { f.CompanyName = "" }, func(f *EmploymentFields) { f.TaxID = "" }, func(f *EmploymentFields) { f.CompanyNum = strings.Repeat("x", 65) }, func(f *EmploymentFields) { f.StartDate = "bad-date" }, func(f *EmploymentFields) { f.EndDate = "1900-01-01" }} {
		e, _ := generateEmployment(7, now)
		change(&e.EmploymentFields)
		if err := validateEmployment(&e.EmploymentFields); err == nil {
			t.Fatal("invalid employment accepted")
		}
	}
	e, _ := generateEmployment(7, now)
	e.EndDate = now.Format("2006-01-02")
	if err := validateEmployment(&e.EmploymentFields); err != nil {
		t.Fatal("valid departure rejected", err)
	}
}
