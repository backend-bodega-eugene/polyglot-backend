package auth

import "testing"

func TestNormalizeAccount(t *testing.T) {
	tests := []struct {
		name        string
		account     string
		wantPhone   bool
		wantAccount string
		wantError   bool
	}{
		{name: "phone", account: "13800138000", wantPhone: true, wantAccount: "13800138000"},
		{name: "email", account: "User@Example.com", wantAccount: "user@example.com"},
		{name: "invalid", account: "not-an-account", wantError: true},
	}

	for _, test := range tests {
		t.Run(test.name, func(t *testing.T) {
			phone, email, normalized, err := normalizeAccount(test.account)
			if test.wantError {
				if err == nil {
					t.Fatal("expected validation error")
				}
				return
			}
			if err != nil {
				t.Fatalf("normalize account: %v", err)
			}
			if (phone != nil) != test.wantPhone {
				t.Fatalf("phone presence = %v, want %v", phone != nil, test.wantPhone)
			}
			if !test.wantPhone && email == nil {
				t.Fatal("email should be set")
			}
			if normalized != test.wantAccount {
				t.Fatalf("normalized = %q, want %q", normalized, test.wantAccount)
			}
		})
	}
}

func TestMaskAccount(t *testing.T) {
	if got := maskAccount("13800138000", true); got != "138****8000" {
		t.Fatalf("masked phone = %q", got)
	}
	if got := maskAccount("user@example.com", false); got != "us***@example.com" {
		t.Fatalf("masked email = %q", got)
	}
}
