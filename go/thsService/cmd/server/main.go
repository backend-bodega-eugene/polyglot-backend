package main

import (
	"context"
	"errors"
	"flag"
	"fmt"
	"log/slog"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	_ "ths-service/docs"
	"ths-service/internal/config"
	"ths-service/internal/database"
	"ths-service/internal/router"
)

// @title thsService API
// @version 1.0
// @description thsService 模拟证券服务接口文档
// @BasePath /
// @securityDefinitions.apikey BearerAuth
// @in header
// @name Authorization
// @description 输入 Bearer 加一个空格，再输入登录返回的 accessToken
func main() {
	if err := run(); err != nil {
		slog.Error("server stopped", "error", err)
		os.Exit(1)
	}
}

func run() error {
	configPath := flag.String("config", "configs/config.yaml", "path to the YAML config file")
	flag.Parse()

	cfg, err := config.Load(*configPath)
	if err != nil {
		return fmt.Errorf("load config: %w", err)
	}
	if err := configureLogger(cfg.Log.Level); err != nil {
		return err
	}

	connection, err := database.Open(cfg.Database)
	if err != nil {
		return err
	}
	defer connection.SQL.Close()

	engine := router.New(connection, cfg)
	server := &http.Server{
		Addr:              cfg.Server.Address,
		Handler:           engine,
		ReadHeaderTimeout: time.Duration(cfg.Server.ReadHeaderTimeoutSeconds) * time.Second,
	}

	serverErrors := make(chan error, 1)
	go func() {
		err := server.ListenAndServe()
		if errors.Is(err, http.ErrServerClosed) {
			serverErrors <- nil
			return
		}
		serverErrors <- err
	}()

	slog.Info("server starting", "address", cfg.Server.Address)

	signalContext, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()

	select {
	case err := <-serverErrors:
		if err != nil {
			return fmt.Errorf("serve http: %w", err)
		}
		return nil
	case <-signalContext.Done():
	}

	slog.Info("server shutting down")
	shutdownContext, cancel := context.WithTimeout(
		context.Background(),
		time.Duration(cfg.Server.ShutdownTimeoutSeconds)*time.Second,
	)
	defer cancel()

	if err := server.Shutdown(shutdownContext); err != nil {
		return fmt.Errorf("shutdown http server: %w", err)
	}

	return <-serverErrors
}

func configureLogger(levelText string) error {
	var level slog.Level
	if err := level.UnmarshalText([]byte(levelText)); err != nil {
		return fmt.Errorf("invalid log.level %q: %w", levelText, err)
	}

	slog.SetDefault(slog.New(slog.NewJSONHandler(os.Stdout, &slog.HandlerOptions{Level: level})))
	return nil
}
