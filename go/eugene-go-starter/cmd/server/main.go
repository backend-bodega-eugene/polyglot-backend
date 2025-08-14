package main

import (
	"context"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"eugene-go-starter/internal/config"
	"eugene-go-starter/internal/logger"
	"eugene-go-starter/internal/router"
)

func main() {
	cfg := config.Load()
	lg := logger.New(cfg)

	r := router.New(cfg, lg)

	srv := &http.Server{
		Addr:              cfg.HTTPAddr,
		Handler:           r,
		ReadHeaderTimeout: 10 * time.Second,
	}

	go func() {
		lg.Info("server starting", "addr", cfg.HTTPAddr)
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			lg.Error("server error", "err", err)
		}
	}()

	// 等待退出信号
	stop := make(chan os.Signal, 1)
	signal.Notify(stop, os.Interrupt, syscall.SIGTERM, syscall.SIGINT)
	<-stop

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	if err := srv.Shutdown(ctx); err != nil {
		lg.Error("graceful shutdown failed", "err", err)
	} else {
		lg.Info("server stopped")
	}
}
