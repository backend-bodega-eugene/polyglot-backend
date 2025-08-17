package router

import (
	"eugene-go-starter/internal/handler"
	"eugene-go-starter/internal/middleware"
	"eugene-go-starter/pkg/config"

	"github.com/gin-gonic/gin"
)

// func useMiddlewares(r *gin.Engine) {
// 	r.Use(middleware.RequestID())
// 	r.Use(middleware.Recovery())
// }

func RegisterPublic(r *gin.Engine, h *handler.UserHandler) {
	//r.GET("/health", handler.Health)
	r.POST("/login", h.Login)
	r.POST("/register", h.Register) // 可选
}
func New(cfg *config.Config, h *handler.UserHandler) *gin.Engine {
	if cfg.Env == "prod" {
		gin.SetMode(gin.ReleaseMode)
	}
	r := gin.New()

	r.Use(gin.Logger())
	r.Use(gin.Recovery())
	r.Use(middleware.RequestID())
	r.Use(middleware.TraceIDMiddleware())
	RegisterPublic(r, h)
	// 健康检查 & 基础信息
	// r.GET("/health", handler.Health)

	// // 版本示例
	// api := r.Group("/api/v1")
	// {
	// 	api.GET("/hello", func(c *gin.Context) { c.JSON(200, gin.H{"message": "hello, eugene"}) })
	// }

	return r
}
