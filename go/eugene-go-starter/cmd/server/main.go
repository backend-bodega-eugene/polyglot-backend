package main

import (
	"context"

	"eugene-go-starter/internal/db"
	"eugene-go-starter/internal/handler"
	"eugene-go-starter/internal/repo"
	"eugene-go-starter/internal/router"
	"eugene-go-starter/internal/service"
	"eugene-go-starter/pkg/config"
	"eugene-go-starter/pkg/logger"
	_ "eugene-go-starter/pkg/response"
)

func main() {
	cfg := config.Load()
	lg := logger.New(cfg)

	sqlxDB, cleanup, err := db.NewSQLXFromEnv()
	if err != nil {
		lg.Error("db init", "err", err)
	}
	defer cleanup(context.Background())

	userRepo := repo.NewUserRepoSQLX(sqlxDB)
	userSvc := service.NewUserService(userRepo)
	userH := handler.NewUserHandler(userSvc)

	r := router.New(cfg, userH) // 只用这一个引擎
	lg.Info("server start", "addr", cfg.HTTPAddr)
	_ = r.Run(cfg.HTTPAddr)
}
