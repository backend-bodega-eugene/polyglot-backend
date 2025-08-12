package router

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"eugene-go-starter/internal/config"
	"eugene-go-starter/internal/middleware"
	"eugene-go-starter/internal/handler"
)

func New(cfg *config.Config, _ any) http.Handler {
	if cfg.Env == "prod" { gin.SetMode(gin.ReleaseMode) }
	r := gin.New()

	r.Use(gin.Logger())
	r.Use(gin.Recovery())
	r.Use(middleware.RequestID())

	// 健康检查 & 基础信息
	r.GET("/health", handler.Health)

	// 版本示例
	api := r.Group("/api/v1")
	{
		api.GET("/hello", func(c *gin.Context) { c.JSON(200, gin.H{"message":"hello, eugene"}) })
	}

	return r
}