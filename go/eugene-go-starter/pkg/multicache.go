package multicache

import (
	"context"
	"encoding/json"
	"errors"
	"sync"
	"time"

	"golang.org/x/sync/singleflight"
)

// -------------------- L1: 简易内存缓存（带 TTL） --------------------

type memEntry[T any] struct {
	val       T
	expiresAt time.Time
}

type MemoryCache[T any] struct {
	mu   sync.RWMutex
	data map[string]memEntry[T]
	ttl  time.Duration
}

func NewMemoryCache[T any](ttl time.Duration) *MemoryCache[T] {
	return &MemoryCache[T]{data: make(map[string]memEntry[T]), ttl: ttl}
}

func (m *MemoryCache[T]) Get(key string) (val T, ok bool) {
	m.mu.RLock()
	e, found := m.data[key]
	m.mu.RUnlock()
	if !found {
		return
	}
	if time.Now().After(e.expiresAt) {
		// 过期主动清理
		m.mu.Lock()
		delete(m.data, key)
		m.mu.Unlock()
		return
	}
	return e.val, true
}

func (m *MemoryCache[T]) Set(key string, val T) {
	m.mu.Lock()
	m.data[key] = memEntry[T]{val: val, expiresAt: time.Now().Add(m.ttl)}
	m.mu.Unlock()
}

func (m *MemoryCache[T]) Del(key string) {
	m.mu.Lock()
	delete(m.data, key)
	m.mu.Unlock()
}

func (m *MemoryCache[T]) DelMulti(keys ...string) {
	m.mu.Lock()
	for _, k := range keys {
		delete(m.data, k)
	}
	m.mu.Unlock()
}

// -------------------- L2: Redis 适配接口 --------------------

type RedisClient interface {
	Get(ctx context.Context, key string) (string, error)
	SetEX(ctx context.Context, key, value string, ttl time.Duration) error
	Del(ctx context.Context, keys ...string) (int64, error)
}

// 可选：一个小错误对齐
var ErrRedisNil = errors.New("redis: nil")

// -------------------- 编解码（默认 JSON，可自定义） --------------------

type Codec[T any] interface {
	Encode(v T) (string, error)
	Decode(s string) (T, error)
}

type JSONCodec[T any] struct{}

func (JSONCodec[T]) Encode(v T) (string, error) {
	b, err := json.Marshal(v)
	return string(b), err
}
func (JSONCodec[T]) Decode(s string) (T, error) {
	var v T
	err := json.Unmarshal([]byte(s), &v)
	return v, err
}

// -------------------- DB Loader 回源函数 --------------------
// 由上层提供：返回 (值, 是否存在, 错误)
type Loader[T any] func(ctx context.Context, key string) (T, bool, error)

// -------------------- MultiCache 主体 --------------------

type MultiCache[T any] struct {
	l1        *MemoryCache[T]
	l2        RedisClient
	l1TTL     time.Duration
	l2TTL     time.Duration
	loader    Loader[T]
	codec     Codec[T]
	sfGroup   singleflight.Group // 防穿透/抖动
	keyPrefix string             // 可选的 redis 前缀
}

type Options[T any] struct {
	L1TTL     time.Duration
	L2TTL     time.Duration
	Codec     Codec[T] // 默认 JSON
	KeyPrefix string
}

func New[T any](l1 *MemoryCache[T], l2 RedisClient, loader Loader[T], opt Options[T]) *MultiCache[T] {
	codec := opt.Codec
	if codec == nil {
		codec = JSONCodec[T]{}
	}
	return &MultiCache[T]{
		l1:        l1,
		l2:        l2,
		l1TTL:     orDefault(opt.L1TTL, 30*time.Second),
		l2TTL:     orDefault(opt.L2TTL, 5*time.Minute),
		loader:    loader,
		codec:     codec,
		keyPrefix: opt.KeyPrefix,
	}
}

func orDefault[T comparable](v, d T) T {
	var zero T
	if v == zero {
		return d
	}
	return v
}

func (mc *MultiCache[T]) makeKey(key string) string {
	if mc.keyPrefix == "" {
		return key
	}
	return mc.keyPrefix + ":" + key
}

// Get：遵循策略：命中内存 => 返回；命中 Redis => 回填内存；命中 DB => 只写 Redis，不写内存
func (mc *MultiCache[T]) Get(ctx context.Context, key string) (T, bool, error) {
	// 1) L1
	if v, ok := mc.l1.Get(key); ok {
		return v, true, nil
	}

	// singleflight 防止同一 key 大量并发回源
	val, err, _ := mc.sfGroup.Do("get:"+key, func() (any, error) {
		// 2) L2
		rk := mc.makeKey(key)
		s, err := mc.l2.Get(ctx, rk)
		if err == nil && s != "" {
			v, decErr := mc.codec.Decode(s)
			if decErr == nil {
				// 命中 Redis → 回填内存
				mc.l1.Set(key, v)
				return got[T]{v, true}, nil
			}
			// 解码失败视作未命中：可做一次删除，避免脏数据反复命中
			_, _ = mc.l2.Del(ctx, rk)
		}

		// 3) L3（DB 回源）
		v, ok, err := mc.loader(ctx, key)
		if err != nil || !ok {
			return got[T]{v, false}, err
		}
		// 命中 DB → 只写 Redis，不写内存
		if enc, encErr := mc.codec.Encode(v); encErr == nil {
			_ = mc.l2.SetEX(ctx, rk, enc, mc.l2TTL)
		}
		return got[T]{v, true}, nil
	})
	if err != nil {
		var zero T
		return zero, false, err
	}
	out := val.(got[T])
	return out.v, out.ok, nil
}

// Invalidate：DB 写完之后调用，删除两级缓存
func (mc *MultiCache[T]) Invalidate(ctx context.Context, key string) {
	mc.l1.Del(key)
	_, _ = mc.l2.Del(ctx, mc.makeKey(key))
}

func (mc *MultiCache[T]) InvalidateMulti(ctx context.Context, keys ...string) {
	mc.l1.DelMulti(keys...)
	if len(keys) == 0 {
		return
	}
	rks := make([]string, 0, len(keys))
	for _, k := range keys {
		rks = append(rks, mc.makeKey(k))
	}
	_, _ = mc.l2.Del(ctx, rks...)
}

// 语法糖：DB 层数据变化后调用（新增/修改/删除）
func (mc *MultiCache[T]) OnDBChanged(ctx context.Context, keys ...string) {
	mc.InvalidateMulti(ctx, keys...)
}

// 小包装：传值返回
type got[T any] struct {
	v  T
	ok bool
}
