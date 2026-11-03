package admin

import (
	"context"
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"math"
	"net/netip"
	"strconv"
	"sync"
	"time"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
	"ths-service/internal/response"
)

const loginWindow = time.Minute
const loginFreeze = 10 * time.Minute
const loginIPMax = 5
const loginAccountMax = 3

type loginLimit struct {
	KeyHash     string `gorm:"primaryKey"`
	Attempts    string
	LockedUntil *time.Time
	ExpiresAt   time.Time
}

func (loginLimit) TableName() string { return "admin_login_limits" }

type loginGuard struct {
	db          *gorm.DB
	now         func() time.Time
	cleanupMu   sync.Mutex
	lastCleanup time.Time
}

func newLoginGuard(db *gorm.DB) *loginGuard { return &loginGuard{db: db, now: time.Now} }

func normalizedIP(ip string) string {
	if address, err := netip.ParseAddr(ip); err == nil {
		return address.Unmap().String()
	}
	return ip
}
func limitKey(kind, value string) string {
	sum := sha256.Sum256([]byte("admin-login-v1\x00" + kind + "\x00" + value))
	return hex.EncodeToString(sum[:])
}

// pruneLoginAttempts implements a rolling minute, rather than a fixed window.
func pruneLoginAttempts(attempts []int64, now time.Time) []int64 {
	result := []int64{}
	cutoff := now.Add(-loginWindow).UnixMilli()
	for _, at := range attempts {
		if at > cutoff {
			result = append(result, at)
		}
	}
	return result
}
func remainingLock(until *time.Time, now time.Time) int {
	if until == nil || !now.Before(*until) {
		return 0
	}
	return max(1, int(math.Ceil(until.Sub(now).Seconds())))
}

// gate serializes all attempts for this identity across goroutines and instances.
// IP requests are reserved before parsing/verification. Account failures are
// recorded after checking the password while holding the account counter lock.
func (g *loginGuard) gate(ctx context.Context, kind, key string, verify func() bool) (int, error) {
	retry := 0
	err := g.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		now := g.now()
		row := loginLimit{KeyHash: limitKey(kind, key), Attempts: "[]", ExpiresAt: now.Add(loginWindow)}
		if err := tx.Clauses(clause.OnConflict{DoNothing: true}).Create(&row).Error; err != nil {
			return err
		}
		if err := tx.Clauses(clause.Locking{Strength: "UPDATE"}).Where("key_hash = ?", row.KeyHash).Take(&row).Error; err != nil {
			return err
		}
		// Refresh time after acquiring a possibly contended row lock.
		now = g.now()
		if retry = remainingLock(row.LockedUntil, now); retry > 0 {
			return nil
		}
		var attempts []int64
		if err := json.Unmarshal([]byte(row.Attempts), &attempts); err != nil {
			return err
		}
		attempts = pruneLoginAttempts(attempts, now)
		row.LockedUntil = nil
		if kind == "ip" {
			if len(attempts) >= loginIPMax {
				until := now.Add(loginFreeze)
				row.LockedUntil = &until
				retry = int(loginFreeze.Seconds())
			} else {
				attempts = append(attempts, now.UnixMilli())
			}
		} else {
			if verify() {
				attempts = []int64{}
			} else {
				attempts = append(attempts, now.UnixMilli())
				failureLimit := loginAccountMax
				if kind == "apk-download-ip" {
					failureLimit = 5
				}
				if len(attempts) >= failureLimit {
					until := now.Add(loginFreeze)
					row.LockedUntil = &until
					retry = int(loginFreeze.Seconds())
				}
			}
		}
		encoded, _ := json.Marshal(attempts)
		row.Attempts = string(encoded)
		row.ExpiresAt = now.Add(loginWindow)
		if row.LockedUntil != nil {
			row.ExpiresAt = *row.LockedUntil
		}
		return tx.Model(&loginLimit{}).Where("key_hash = ?", row.KeyHash).Updates(map[string]any{"attempts": row.Attempts, "locked_until": row.LockedUntil, "expires_at": row.ExpiresAt}).Error
	})
	if err == nil {
		g.cleanup(ctx)
	}
	return retry, err
}
func (g *loginGuard) cleanup(ctx context.Context) {
	now := g.now()
	g.cleanupMu.Lock()
	run := g.lastCleanup.IsZero() || now.Sub(g.lastCleanup) >= time.Minute
	if run {
		g.lastCleanup = now
	}
	g.cleanupMu.Unlock()
	if run {
		// Expired rows are dispensable; bounded cleanup never resets active freezes.
		g.db.WithContext(ctx).Exec("DELETE FROM admin_login_limits WHERE expires_at <= ? LIMIT 1000", now)
	}
}
func loginBlocked(c *gin.Context, retry int) {
	c.Header("Retry-After", strconv.Itoa(retry))
	response.Error(c, 429, response.CodeTooManyRequests, fmt.Sprintf("登录尝试过于频繁，请在%d秒后重试", retry))
}
func (h *Handler) LoginIPGuard(c *gin.Context) {
	retry, err := h.guard.gate(c.Request.Context(), "ip", normalizedIP(c.ClientIP()), nil)
	if err != nil {
		h.fail(c, err)
		c.Abort()
		return
	}
	if retry > 0 {
		loginBlocked(c, retry)
		c.Abort()
		return
	}
	c.Next()
}
