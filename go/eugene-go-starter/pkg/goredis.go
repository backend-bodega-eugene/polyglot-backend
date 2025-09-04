package multicache

import (
	"context"
	"time"

	"github.com/redis/go-redis/v9"
)

type GoRedisV9 struct{ cli *redis.Client }

func NewGoRedisV9(cli *redis.Client) *GoRedisV9 { return &GoRedisV9{cli: cli} }

func (g *GoRedisV9) Get(ctx context.Context, key string) (string, error) {
	s, err := g.cli.Get(ctx, key).Result()
	if err == redis.Nil {
		return "", ErrRedisNil
	}
	return s, err
}

func (g *GoRedisV9) SetEX(ctx context.Context, key, value string, ttl time.Duration) error {
	return g.cli.SetEx(ctx, key, value, ttl).Err()
}

func (g *GoRedisV9) Del(ctx context.Context, keys ...string) (int64, error) {
	return g.cli.Del(ctx, keys...).Result()
}
