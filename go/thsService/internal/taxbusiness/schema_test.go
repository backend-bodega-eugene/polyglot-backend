package taxbusiness

import (
	"encoding/json"
	"testing"
	"ths-service/internal/tax"
)

func TestGeneratedTaxData(t *testing.T) {
	v := tax.EmploymentView{Employment: tax.Employment{EmploymentFields: tax.EmploymentFields{CompanyName: "模拟单位", TaxID: "SIM-TAX", CompanyNum: "SIM-COMPANY", Post: "职员"}}}
	for _, kind := range []string{"monthly", "applications", "openrecords", "messages"} {
		year := 0
		if kind == "monthly" || kind == "applications" {
			year = 2024
		}
		rows := generate(kind, 1, year, v)
		if len(rows) == 0 {
			t.Fatal(kind)
		}
		for _, p := range rows {
			if e := Validate(kind, year, p); e != nil {
				t.Fatal(kind, e)
			}
		}
	}
	p := generate("monthly", 1, 2024, v)[0]
	p["income"] = "-1"
	if Validate("monthly", 2024, p) == nil {
		t.Fatal("accepted negative income")
	}
	p = generate("monthly", 1, 2024, v)[0]
	p["type"] = json.Number("1.5")
	if Validate("monthly", 2024, p) == nil {
		t.Fatal("accepted non-integer type")
	}
	p = generate("monthly", 1, 2024, v)[0]
	p["rate"] = "NaN%"
	if Validate("monthly", 2024, p) == nil {
		t.Fatal("accepted NaN rate")
	}
	p = generate("openrecords", 1, 0, v)[0]
	p["images_data"] = "javascript:alert(1)"
	if Validate("openrecords", 0, p) == nil {
		t.Fatal("unsafe image URL")
	}
	for _, raw := range []string{`2024`, `"2024"`} {
		if year, e := yearValue(json.RawMessage(raw)); e != nil || year != 2024 {
			t.Fatal(raw, e)
		}
	}
}
