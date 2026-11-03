package electricity

import (
	"strings"
	"testing"
	"ths-service/internal/auth"
)

func TestGenerateAndValidate(t *testing.T) {
	u := auth.User{ID: 7, RealName: "模拟会员"}
	for range 20 {
		p, err := generate(u)
		if err != nil {
			t.Fatal(err)
		}
		if p.UserID != 7 || p.Version == "" || len(p.UserIDNumber) != 16 || len(p.MeterNo) != 18 || !strings.HasPrefix(p.UserIDNumber, "050200") {
			t.Fatal(p)
		}
	}
	for _, change := range []func(*Fields){func(f *Fields) { f.Address = " " }, func(f *Fields) { f.DisplayName = strings.Repeat("名", 65) }, func(f *Fields) { f.UserIDNumber = "123" }, func(f *Fields) { f.MeterNo = "12345" }, func(f *Fields) { f.PostalCode = "51000A" }} {
		p, _ := generate(u)
		change(&p.Fields)
		if err := validate(&p.Fields); err == nil {
			t.Fatal("invalid fields accepted")
		}
	}
}
