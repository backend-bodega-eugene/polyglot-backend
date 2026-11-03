package admin

import (
	"fmt"
	"net/http/httptest"
	"strings"
	"sync"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
)

func TestLoginWindowAndIPNormalization(t *testing.T) {
	now := time.Now()
	attempts := []int64{now.Add(-time.Minute - time.Millisecond).UnixMilli(), now.Add(-time.Minute).UnixMilli(), now.Add(-time.Minute + time.Millisecond).UnixMilli(), now.UnixMilli()}
	if got := pruneLoginAttempts(attempts, now); len(got) != 2 {
		t.Fatal(got)
	}
	until := now.Add(time.Second + time.Millisecond)
	if remainingLock(&until, now) != 2 {
		t.Fatal("Retry-After must round up")
	}
	if remainingLock(&now, now) != 0 {
		t.Fatal("lock did not expire")
	}
	if normalizedIP("::ffff:192.0.2.1") != normalizedIP("192.0.2.1") {
		t.Fatal("mapped IPv4 bypass")
	}
}
func testLoginGuardIntegration(t *testing.T, db *gorm.DB) {
	t.Helper()
	now := time.Now().UTC().Truncate(time.Millisecond)
	makeGuard := func() *loginGuard { g := newLoginGuard(db); g.now = func() time.Time { return now }; return g }
	g := makeGuard()
	gate := func(kind, key string, verify func() bool, want int) {
		retry, err := g.gate(t.Context(), kind, key, verify)
		if err != nil || retry != want {
			var row loginLimit
			db.Where("key_hash = ?", limitKey(kind, key)).Take(&row)
			t.Fatalf("%s %s at %s retry%d err%v want%d state%+v", kind, key, now.Format(time.RFC3339Nano), retry, err, want, row)
		}
	}
	for range 5 {
		gate("ip", "192.0.2.41", nil, 0)
	}
	gate("ip", "192.0.2.41", nil, 600)
	now = now.Add(2 * time.Minute)
	gate("ip", "192.0.2.41", nil, 480)
	g = makeGuard()
	gate("ip", "192.0.2.41", nil, 480) // persists across handler restart
	gate("ip", "192.0.2.42", nil, 0)
	now = now.Add(8 * time.Minute)
	gate("ip", "192.0.2.41", nil, 0)
	wrong := func() bool { return false }
	right := func() bool { return true }
	gate("account", "reset-on-success", wrong, 0)
	gate("account", "reset-on-success", wrong, 0)
	gate("account", "reset-on-success", right, 0)
	gate("account", "reset-on-success", wrong, 0)
	gate("account", "rolling", wrong, 0)
	now = now.Add(time.Minute)
	gate("account", "rolling", wrong, 0)
	gate("account", "rolling", wrong, 0)
	gate("account", "rolling", wrong, 600)
	called := false
	gate("account", "rolling", func() bool { called = true; return true }, 600)
	if called {
		t.Fatal("verified password while frozen")
	}
	now = now.Add(10 * time.Minute)
	gate("account", "rolling", right, 0)
	// Concurrent requests cannot exceed the five-request allowance.
	var wg sync.WaitGroup
	var mu sync.Mutex
	allowed, blocked := 0, 0
	for range 12 {
		wg.Go(func() {
			retry, err := g.gate(t.Context(), "ip", "192.0.2.43", nil)
			if err != nil {
				t.Error(err)
				return
			}
			mu.Lock()
			defer mu.Unlock()
			if retry == 0 {
				allowed++
			} else {
				blocked++
			}
		})
	}
	wg.Wait()
	if allowed != 5 || blocked != 7 {
		t.Fatalf("concurrent allowed%d blocked%d", allowed, blocked)
	}

	// HTTP integration: aliases use the administrator ID and errors span IPs.
	h := NewHandler(db, "test-secret", 3600)
	h.guard = g
	r := gin.New()
	r.SetTrustedProxies(nil)
	r.POST("/login", h.LoginIPGuard, h.Login)
	request := func(username, ip string, want int) {
		req := httptest.NewRequest("POST", "/login", strings.NewReader(fmt.Sprintf(`{"username":%q,"password":"incorrect-password"}`, username)))
		req.RemoteAddr = ip + ":1234"
		req.Header.Set("Content-Type", "application/json")
		req.Header.Set("X-Forwarded-For", "203.0.113.123")
		w := httptest.NewRecorder()
		r.ServeHTTP(w, req)
		if w.Code != want {
			t.Fatalf("login HTTP%d want%d: %s", w.Code, want, w.Body.String())
		}
		if want == 429 && w.Header().Get("Retry-After") != "600" {
			t.Fatal("missing Retry-After")
		}
	}
	request("eugene", "198.51.100.1", 401)
	request("EUGENE", "198.51.100.2", 401)
	request(" eugene ", "198.51.100.3", 429)
	request("eugene", "198.51.100.4", 429)
	// Spoofed XFF is ignored for untrusted peers; malformed JSON counts as IP traffic.
	for i := 0; i < 6; i++ {
		req := httptest.NewRequest("POST", "/login", strings.NewReader("{"))
		req.RemoteAddr = "198.51.100.9:1234"
		req.Header.Set("Content-Type", "application/json")
		req.Header.Set("X-Forwarded-For", fmt.Sprintf("203.0.113.%d", i+1))
		w := httptest.NewRecorder()
		r.ServeHTTP(w, req)
		want := 400
		if i == 5 {
			want = 429
		}
		if w.Code != want {
			t.Fatalf("malformed request%d HTTP%d want%d", i, w.Code, want)
		}
	}
}
