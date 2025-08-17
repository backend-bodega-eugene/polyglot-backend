package logger

import (
	"eugene-go-starter/pkg/config"
	"fmt"
	"io"
	"log/slog"
	"os"
)

type Logger = slog.Logger

func New(cfg *config.Config) *Logger {
	level := slog.LevelInfo
	if cfg.Env == "dev" {
		level = slog.LevelDebug
	}
	return slog.New(slog.NewJSONHandler(os.Stdout, &slog.HandlerOptions{Level: level}))
}
func NewLogger() *Logger {
	// 打开或创建日志文件
	if err := os.MkdirAll("logs", 0755); err != nil {
		//log.Fatalf("failed to create log directory: %v", err)
		fmt.Println("warn: open log file failed:", err) // 标准输出，避免自依赖
	}

	file, err := os.OpenFile("logs/app.log", os.O_CREATE|os.O_WRONLY|os.O_APPEND, 0666)
	if err != nil {
		fmt.Println("failed to open log file", err)
	}

	// 同时输出到控制台和文件
	multiWriter := io.MultiWriter(os.Stdout, file)

	// 创建 JSON 日志处理器
	handler := slog.NewJSONHandler(multiWriter, &slog.HandlerOptions{
		Level: slog.LevelInfo,
	})

	return slog.New(handler)
}
