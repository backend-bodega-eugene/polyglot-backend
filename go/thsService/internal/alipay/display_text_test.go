package alipay

import (
	"encoding/json"
	"strings"
	"testing"
	"ths-service/internal/auth"
)

func TestGeneratedDisplayText(t *testing.T) {
	assertText := func(text string) {
		t.Helper()
		for _, marker := range []string{"模拟", "测试", "演示", "仅供展示", "仅为展示", "仅用于展示", "仅作展示", "仅限展示"} {
			if strings.Contains(text, marker) {
				t.Fatalf("generated display text contains %q: %s", marker, text)
			}
		}
	}
	check := func(v any) {
		t.Helper()
		b, e := json.Marshal(v)
		if e != nil {
			t.Fatal(e)
		}
		assertText(string(b))
	}
	for _, s := range Schemas {
		year := 0
		if s.Yearly {
			year = 2025
		}
		check(generate(s.Kind, auth.User{ID: 7, RealName: "张三"}, year))
	}
}
