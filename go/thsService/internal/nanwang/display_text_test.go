package nanwang

import (
	"encoding/json"
	"strings"
	"testing"
	"ths-service/internal/electricity"
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
	v := electricity.View{Profile: electricity.Profile{Fields: electricity.Fields{DisplayName: "张三", Address: "天河路18号", UserIDNumber: "00123456789"}}, ID: 7}
	for _, s := range Schemas {
		check(generate(s.Kind, v))
	}
}
