package config

import (
	"log"
	"os"
	"github.com/joho/godotenv"
	"time"
)

type Config struct {
	Env      string // dev / prod / test
	HTTPAddr string // 0.0.0.0:8080
	AppName  string // eugene-go-starter
}

func Load() *Config {
		if err := godotenv.Load(); err != nil {
		log.Println("No .env file found, using system environment")
	}
	cfg := &Config{
		Env:      getEnv("APP_ENV", "dev"),
		HTTPAddr: getEnv("HTTP_ADDR", "127.0.0.1:8080"),
		AppName:  getEnv("APP_NAME", "eugene-go-starter"),
	}
	log.Printf("[config] env=%s addr=%s app=%s", cfg.Env, cfg.HTTPAddr, cfg.AppName)
	return cfg
}

func getEnv(key, def string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return def
}
type JWT struct {
	Secret        string        // HS256 密钥
	Issuer        string        // 签发者
	Audience      string        // 受众
	AccessTTL     time.Duration // 访问令牌有效期
	RefreshTTL    time.Duration // 刷新令牌有效期（可选）
	AllowClockSkew time.Duration // 时钟偏差
}
func LoadJWT() JWT {
	secret := os.Getenv("JWT_SECRET")
	if secret == "" {
		// 开发环境兜底，生产一定要用环境变量
		secret = "CHANGE_ME_IN_PROD"
	}
	return JWT{
		Secret:         secret,
		Issuer:         getEnv("JWT_ISSUER", "eugene-go-starter"),
		Audience:       getEnv("JWT_AUDIENCE", "eugene-users"),
		AccessTTL:      parseDuration("JWT_ACCESS_TTL", 24*time.Hour),
		RefreshTTL:     parseDuration("JWT_REFRESH_TTL", 7*24*time.Hour),
		AllowClockSkew: parseDuration("JWT_CLOCK_SKEW", 2*time.Minute),
	}
}
func parseDuration(k string, def time.Duration) time.Duration {
	if v := os.Getenv(k); v != "" {
		if d, err := time.ParseDuration(v); err == nil {
			return d
		}
	}
	return def
}
