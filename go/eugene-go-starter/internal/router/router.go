package router

import (
	"net/http"

	"eugene-go-starter/internal/config"
	"eugene-go-starter/internal/handler"
	"eugene-go-starter/internal/middleware"

	"github.com/gin-gonic/gin"
)

func New(cfg *config.Config, _ any) http.Handler {
	if cfg.Env == "prod" {
		gin.SetMode(gin.ReleaseMode)
	}
	r := gin.New()

	r.Use(gin.Logger())
	r.Use(gin.Recovery())
	r.Use(middleware.RequestID())
	r.Use(middleware.TraceIDMiddleware())
	// 健康检查 & 基础信息
	r.GET("/health", handler.Health)

	// 版本示例
	api := r.Group("/api/v1")
	{
		api.GET("/hello", func(c *gin.Context) { c.JSON(200, gin.H{"message": "hello, eugene"}) })
	}

	return r
}
