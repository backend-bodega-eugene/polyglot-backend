package nanwang

import (
	"encoding/json"
	"testing"
	"ths-service/internal/electricity"
)

func TestGeneratedBusinessFields(t *testing.T) {
	v := electricity.View{Profile: electricity.Profile{Fields: electricity.Fields{DisplayName: "模拟户名", UserIDNumber: "0943000049338409", Address: "模拟地址"}}, ID: 7}
	for _, s := range Schemas {
		rows := generate(s.Kind, v)
		if len(rows) == 0 {
			t.Fatal(s.Kind)
		}
		for _, p := range rows {
			if err := Validate(s.Kind, p); err != nil {
				t.Fatal(s.Kind, err)
			}
		}
	}
	if err := Validate("charging", map[string]any{"hasPile": "false"}); err == nil {
		t.Fatal("accepted string boolean")
	}
	p := generate("news", v)[0]
	p["readCount"] = json.Number("-1")
	if Validate("news", p) == nil {
		t.Fatal("negative count")
	}
	p = generate("news", v)[0]
	p["link"] = "javascript:alert(1)"
	if Validate("news", p) == nil {
		t.Fatal("unsafe link")
	}
	p = generate("arrears", v)[0]
	p["arrears"] = "-1"
	if Validate("arrears", p) == nil {
		t.Fatal("negative amount")
	}
	p = generate("points", v)[0]
	p["password"] = "x"
	if Validate("points", p) == nil {
		t.Fatal("unknown field")
	}
}
