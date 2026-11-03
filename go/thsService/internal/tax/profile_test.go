package tax

import (
	"regexp"
	"strings"
	"testing"
	"ths-service/internal/auth"
	"time"
)

func TestGeneratedTaxProfile(t *testing.T) {
	for range 20 {
		p, err := generate(auth.User{ID: 7, RealName: "模拟会员"})
		if err != nil {
			t.Fatal(err)
		}
		if p.UserID != 7 || p.Version == "" || p.CardType != "居民身份证" {
			t.Fatal("invalid simulation")
		}
		for _, number := range []string{p.IDCard, p.TaxNumber, p.BankNum} {
			if !regexp.MustCompile(`^[1-9][0-9]{11}$`).MatchString(number) {
				t.Fatalf("invalid display number %s", number)
			}
		}
		values := strings.Split(makeView(p).TaxBackMoney, ",")
		if len(values) != 5 || values[0] != p.Refund2019 || values[4] != p.Refund2023 {
			t.Fatal("refund mapping")
		}
		if err := validate(&p.Fields, time.Now()); err != nil {
			t.Fatal(err)
		}
	}
}
func TestTaxValidation(t *testing.T) {
	p0, _ := generate(auth.User{ID: 7, RealName: "测试会员"})
	for _, bankType := range []string{"", "自定义银行类型", "  任意文本  "} {
		p := p0
		p.Fields.BankType = bankType
		if e := validate(&p.Fields, time.Now()); e != nil {
			t.Fatal(e)
		}
	}
	for _, bankType := range []string{strings.Repeat("字", 65), "无效\x00类型"} {
		p := p0
		p.Fields.BankType = bankType
		if validate(&p.Fields, time.Now()) == nil {
			t.Fatal("invalid bank type accepted")
		}
	}
	for _, change := range []func(*Fields){func(f *Fields) { f.Name = "" }, func(f *Fields) { f.Birth = "2999-01-01" }, func(f *Fields) { f.TaxBackMoney = "1,2,3" }, func(f *Fields) { f.TaxBackMoney = "1,2,3,4,-1" }, func(f *Fields) { f.TaxBackMoney = "1,2,3,4,1.001" }, func(f *Fields) { f.TaxBackMoney = "1,2,3,4,1000000000" }, func(f *Fields) { f.Phone = "invalid" }, func(f *Fields) { f.Email = "invalid" }} {
		p, err := generate(auth.User{ID: 7, RealName: "模拟会员"})
		if err != nil {
			t.Fatal(err)
		}
		change(&p.Fields)
		if err := validate(&p.Fields, time.Now()); err == nil {
			t.Fatal("invalid profile accepted")
		}
	}
	p, _ := generate(auth.User{ID: 7, RealName: "模拟会员"})
	p.TaxBackMoney = "1,2.1,3,4, 5.00 "
	if err := validate(&p.Fields, time.Now()); err != nil {
		t.Fatal(err)
	}
	if p.TaxBackMoney != "1.00,2.10,3.00,4.00,5.00" {
		t.Fatal("decimal canonicalization")
	}
}
