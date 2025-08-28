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
	"time"

	_ "eugene-go-starter/docs" //
	"eugene-go-starter/internal/db"
	"eugene-go-starter/internal/router"
	"eugene-go-starter/pkg/config"
	"eugene-go-starter/pkg/logger"
	_ "eugene-go-starter/pkg/response"

	"github.com/jmoiron/sqlx"
	swaggerFiles "github.com/swaggo/files" // ✅ 起个别名更清晰
	ginSwagger "github.com/swaggo/gin-swagger"
)

func main() {
	cfg := config.Load()
	lg := logger.New(cfg)

	// init DB by DATABASE_TYPE
	var (
		cleanup func(context.Context) error
		err     error
		sqlxDB  *sqlx.DB
	)
	if cfg.DatabaseType == "gorm" {
		// still create sqlx for router to reuse underlying *sql.DB, but not required
		sqlxDB, cleanup, err = db.NewSQLXFromEnv()
		if err != nil {
			lg.Error("db init (sqlx for gorm bridge)", "err", err)
		}
		lg.Info("db type selected", "type", cfg.DatabaseType)
	} else {
		sqlxDB, cleanup, err = db.NewSQLXFromEnv()
		if err != nil {
			lg.Error("db init", "err", err)
		}
		lg.Info("db type selected", "type", cfg.DatabaseType)
	}
	defer cleanup(context.Background())

	// quick ping with timeout
	ctx, cancel := context.WithTimeout(context.Background(), 3*time.Second)
	defer cancel()
	if pingErr := sqlxDB.PingContext(ctx); pingErr != nil {
		lg.Error("db ping", "err", pingErr)
	} else {
		lg.Info("db ping ok")
	}

	r := router.New(cfg, sqlxDB) // 只用这一个引擎
	lg.Info("server start", "addr", cfg.HTTPAddr)
	//docs.SwaggerInfo.BasePath = "/"
	r.GET("/swagger/*any", ginSwagger.WrapHandler(swaggerFiles.Handler)) // ✅ 真正使用
	r.Static("/eugene", "./web")
	_ = r.Run(cfg.HTTPAddr)

}
