package alipay

import (
	"testing"
	"ths-service/internal/auth"
)

func TestGeneratedAlipayData(t *testing.T) {
	phone := "13900000091"
	u := auth.User{ID: 91, RealName: "模拟会员", Phone: &phone}
	for _, def := range Schemas {
		year := 0
		if def.Yearly {
			year = 2025
		}
		rows := generate(def.Kind, u, year)
		if len(rows) == 0 {
			t.Fatal(def.Kind)
		}
		for _, p := range rows {
			if e := Validate(def.Kind, year, p); e != nil {
				t.Fatal(def.Kind, e)
			}
		}
	}
	p := generate("records", u, 2025)[0]
	p["companyPay"] = "-1"
	if Validate("records", 2025, p) == nil {
		t.Fatal("negative money")
	}
	p = generate("records", u, 2025)[0]
	p["month"] = "202413"
	if Validate("records", 2025, p) == nil {
		t.Fatal("invalid month")
	}
	p = generate("accounts", u, 0)[0]
	p["rightName"] = ""
	if Validate("accounts", 0, p) == nil {
		t.Fatal("incomplete right household")
	}
	p["rightAccountNo"] = ""
	if e := Validate("accounts", 0, p); e != nil {
		t.Fatal(e)
	}
	p = generate("electric", u, 0)[0]
	p["amount"] = 1
	if Validate("electric", 0, p) == nil {
		t.Fatal("numeric money accepted")
	}
}
