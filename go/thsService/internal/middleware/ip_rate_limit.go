package middleware

import (
	"math"
	"net/http"
	"strconv"
	"sync"
	"time"

	"github.com/gin-gonic/gin"

	"ths-service/internal/response"
)

type ipWindow struct {
	count   int
	resetAt time.Time
}

type ipRateLimiter struct {
	mu          sync.Mutex
	windows     map[string]ipWindow
	maxRequests int
	window      time.Duration
	lastCleanup time.Time
	now         func() time.Time
}

func IPRateLimit(maxRequests int, window time.Duration, message string) gin.HandlerFunc {
	limiter := &ipRateLimiter{
		windows:     make(map[string]ipWindow),
		maxRequests: maxRequests,
		window:      window,
		lastCleanup: time.Now(),
		now:         time.Now,
	}

	return func(c *gin.Context) {
		allowed, retryAfter := limiter.allow(c.ClientIP())
		if !allowed {
			c.Header("Retry-After", strconv.Itoa(retryAfter))
			response.Error(c, http.StatusTooManyRequests, response.CodeTooManyRequests, message)
			c.Abort()
			return
		}

		c.Next()
	}
}

func (l *ipRateLimiter) allow(ip string) (bool, int) {
	l.mu.Lock()
	defer l.mu.Unlock()

	now := l.now()
	if now.Sub(l.lastCleanup) >= l.window {
		for key, currentWindow := range l.windows {
			if !now.Before(currentWindow.resetAt) {
				delete(l.windows, key)
			}
		}
		l.lastCleanup = now
	}

	currentWindow, exists := l.windows[ip]
	if !exists || !now.Before(currentWindow.resetAt) {
		l.windows[ip] = ipWindow{count: 1, resetAt: now.Add(l.window)}
		return true, 0
	}
	if currentWindow.count >= l.maxRequests {
		retryAfter := int(math.Ceil(currentWindow.resetAt.Sub(now).Seconds()))
		if retryAfter < 1 {
			retryAfter = 1
		}
		return false, retryAfter
	}

	currentWindow.count++
	l.windows[ip] = currentWindow
	return true, 0
}
