package config

import (
	"log"
	"os"
	"github.com/joho/godotenv"
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
