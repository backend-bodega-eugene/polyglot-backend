package jwtutil

import (
	"context"
	"sync"
	"time"
)

// JTI 黑名单（内存版）
type Blocklist struct {
	mu   sync.RWMutex
	data map[string]time.Time // jti -> 过期时间
}

func NewBlocklist() *Blocklist {
	return &Blocklist{data: make(map[string]time.Time)}
}

func (b *Blocklist) Block(_ context.Context, jti string, ttl time.Duration) {
	exp := time.Now().Add(ttl)
	b.mu.Lock()
	b.data[jti] = exp
	b.mu.Unlock()
}

func (b *Blocklist) IsBlocked(_ context.Context, jti string) bool {
	b.mu.RLock()
	exp, ok := b.data[jti]
	b.mu.RUnlock()
	if !ok {
		return false
	}
	// 过期就顺手清理
	if time.Now().After(exp) {
		b.mu.Lock()
		delete(b.data, jti)
		b.mu.Unlock()
		return false
	}
	return true
}
