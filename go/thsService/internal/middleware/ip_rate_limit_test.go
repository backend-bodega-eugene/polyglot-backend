package middleware

import (
	"testing"
	"time"
)

func TestIPRateLimiter(t *testing.T) {
	now := time.Date(2026, time.October, 7, 0, 0, 0, 0, time.UTC)
	limiter := &ipRateLimiter{
		windows:     make(map[string]ipWindow),
		maxRequests: 2,
		window:      time.Minute,
		lastCleanup: now,
		now:         func() time.Time { return now },
	}

	if allowed, _ := limiter.allow("127.0.0.1"); !allowed {
		t.Fatal("first request should be allowed")
	}
	if allowed, _ := limiter.allow("127.0.0.1"); !allowed {
		t.Fatal("second request should be allowed")
	}
	if allowed, retryAfter := limiter.allow("127.0.0.1"); allowed || retryAfter != 60 {
		t.Fatalf("third request = (%v, %d), want (false, 60)", allowed, retryAfter)
	}
	if allowed, _ := limiter.allow("127.0.0.2"); !allowed {
		t.Fatal("a different IP should be allowed")
	}

	now = now.Add(time.Minute)
	if allowed, _ := limiter.allow("127.0.0.1"); !allowed {
		t.Fatal("request in a new window should be allowed")
	}
}
