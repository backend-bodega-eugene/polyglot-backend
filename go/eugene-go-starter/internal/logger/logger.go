package logger

import (
	"log/slog"
	"os"
	"eugene-go-starter/internal/config"
)

type Logger = slog.Logger

func New(cfg *config.Config) *Logger {
	level := slog.LevelInfo
	if cfg.Env == "dev" { level = slog.LevelDebug }
	return slog.New(slog.NewJSONHandler(os.Stdout, &slog.HandlerOptions{Level: level}))
}