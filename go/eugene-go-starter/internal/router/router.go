package router

import (
	"eugene-go-starter/internal/handler"
	"eugene-go-starter/internal/jwtutil"
	"eugene-go-starter/internal/middleware"
	"eugene-go-starter/internal/repo"
	"eugene-go-starter/pkg/config"

	"github.com/gin-gonic/gin"
	"github.com/jmoiron/sqlx"
)

// func useMiddlewares(r *gin.Engine) {
// 	r.Use(middleware.RequestID())
// 	r.Use(middleware.Recovery())
// }

//	func RegisterPublic(r *gin.Engine, h *handler.UserHandler) {
//		//r.GET("/health", handler.Health)
//		r.POST("/login", h.Login)
//		r.POST("/register", h.Register) // 可选
//		r.POST("/api/refresh", auth.Refresh)
//	}
func New(cfg *config.Config, db *sqlx.DB) *gin.Engine {
	jwtCfg := config.LoadJWT()
	jwtSvc := jwtutil.New(jwtCfg)

	auth := &handler.AuthHandler{
		Users:   repo.NewUserRepoSQLX(db), // 替换
		JWT:     jwtSvc,
		Revoker: nil, // 有 Redis 再接
	}
	MenuHandler := &handler.MenuHandler{
		Repo: repo.NewMenuRepoSQLX(db),
	}
	// user:=&handler.UserHandler{
	// 	Svc: handler.NewUserService(repo.NewUserRepoSQLX(db))
	// 	}
	if cfg.Env == "prod" {
		gin.SetMode(gin.ReleaseMode)
	}
	r := gin.New()

	r.Use(gin.Logger())
	r.Use(gin.Recovery())
	r.Use(middleware.RequestID())
	r.Use(middleware.TraceIDMiddleware())
	r.POST("/login", auth.Login)
	//r.POST("/register", auth.Register) // 可选
	r.POST("/api/refresh", auth.Refresh)
	//RegisterPublic(r, h)
	rAuth := r.Group("/api")
	rAuth.Use(middleware.AuthRequired(middleware.AuthOptions{
		JWT:     jwtSvc,
		Revoker: nil,
	}))
	{
		rAuth.GET("/menus", MenuHandler.GetMyMenus)
		// rAuth.GET("/me", func(c *gin.Context) {
		// 	uid, _ := c.Get("uid")
		// 	uname, _ := c.Get("uname")
		// 	response.OK(c, gin.H{
		// 		"userId":   uid,
		// 		"username": uname,
		// 	})
		// })
		// // 需要管理员
		// rAuth.GET("/admin/only", middleware.RequireRole("admin"), func(c *gin.Context) {
		// 	response.OK(c, gin.H{"ok": true})
		// })
		// }
		// 健康检查 & 基础信息
		// r.GET("/health", handler.Health)

		// // 版本示例
		// api := r.Group("/api/v1")
		// {
		// 	api.GET("/hello", func(c *gin.Context) { c.JSON(200, gin.H{"message": "hello, eugene"}) })
		// }

		return r
	}
}
