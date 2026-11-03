package alipay

import (
	"regexp"
	"testing"
)

func TestManagementCodeFormat(t *testing.T) {
	pattern := regexp.MustCompile(`^110500[1-9]{9}$`)
	seen := map[string]bool{}
	for i := 0; i < 100; i++ {
		code := newManagementCode()
		if !pattern.MatchString(code) {
			t.Fatal(code)
		}
		seen[code] = true
	}
	if len(seen) < 2 {
		t.Fatal("management codes are not randomized")
	}
}
