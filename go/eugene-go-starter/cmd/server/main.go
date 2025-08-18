// @title Eugene API
// @version 1.0
// @description 登录鉴权最小可用文档
// @host localhost:9321
// @BasePath /
// @schemes http
// @securityDefinitions.apikey BearerAuth
// @in header
// @name Authorization
package main

import (
	"context"

	_ "eugene-go-starter/docs" //
	"eugene-go-starter/internal/db"
	"eugene-go-starter/internal/router"
	"eugene-go-starter/pkg/config"
	"eugene-go-starter/pkg/logger"
	_ "eugene-go-starter/pkg/response"

	swaggerFiles "github.com/swaggo/files" // ✅ 起个别名更清晰
	ginSwagger "github.com/swaggo/gin-swagger"
)

func main() {
	cfg := config.Load()
	lg := logger.New(cfg)

	sqlxDB, cleanup, err := db.NewSQLXFromEnv()
	if err != nil {
		lg.Error("db init", "err", err)
	}
	defer cleanup(context.Background())

	// userRepo := repo.NewUserRepoSQLX(sqlxDB)
	// userSvc := service.NewUserService(userRepo)
	// userH := handler.NewUserHandler(userSvc)

	r := router.New(cfg, sqlxDB) // 只用这一个引擎
	lg.Info("server start", "addr", cfg.HTTPAddr)
	//docs.SwaggerInfo.BasePath = "/"
	r.GET("/swagger/*any", ginSwagger.WrapHandler(swaggerFiles.Handler)) // ✅ 真正使用
	_ = r.Run(cfg.HTTPAddr)

}
