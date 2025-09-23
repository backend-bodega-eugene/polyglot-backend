package router

import (
	"context"
	"eugene-go-starter/internal/db"
	"eugene-go-starter/internal/handler"
	"eugene-go-starter/internal/jwtutil"
	"eugene-go-starter/internal/middleware"
	"eugene-go-starter/internal/repo/gorms"
	"eugene-go-starter/internal/repo/mongos"
	"eugene-go-starter/internal/repo/sqlxs"
	"eugene-go-starter/internal/service"
	"eugene-go-starter/pkg/config"
	"eugene-go-starter/pkg/logger"
	"eugene-go-starter/pkg/response"
	"os"
	"sync" // 引入工具类

	"github.com/gin-gonic/gin"
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
var (
	cleanupfunc func(context.Context) error
	once        sync.Once
)

func Cleanup(ctx context.Context) {
	once.Do(func() {
		if cleanupfunc != nil {
			_ = cleanupfunc(ctx)
		}
	})
}
func New(cfg *config.Config, lg *logger.Logger) *gin.Engine {
	jwtCfg := config.LoadJWT()
	jwtSvc := jwtutil.New(jwtCfg)
	database_type := os.Getenv("DATABASE_TYPE") //GORM=GORM ,SQLX=SQLX
	if database_type == "" {
		database_type = "GORM"

	}
	var auth *handler.AuthHandler
	var user *handler.UserHandler
	var MenuHandler *handler.MenuHandler
	var PermissionHandler *handler.PermissionHandler
	// 选择数据库实现
	switch database_type {
	case "SQLX":
		sqlxDB, cleanup, err := db.NewSQLXFromEnv()
		if err != nil {
			lg.Error("db init", "err", err)
		}
		cleanupfunc = cleanup
		auth = &handler.AuthHandler{
			Users:   sqlxs.NewUserRepoSQLX(sqlxDB), // 替换
			JWT:     jwtSvc,
			Revoker: nil, // 有 Redis 再接
		}
		userService := service.NewUserService(sqlxs.NewUserRepoSQLX(sqlxDB))
		user = &handler.UserHandler{
			Svc: userService,
		}
		MenuHandler = &handler.MenuHandler{
			Repo: sqlxs.NewMenuRepoSQLX(sqlxDB),
		}
		PermissionHandler = &handler.PermissionHandler{
			PermRepo: sqlxs.NewPermissionMySQL(sqlxDB),
		}
	case "GORM":
		gormDB, cleanup, err := db.NewGORMFromEnv()
		if err != nil {
			lg.Error("db init", "err", err)
		}
		cleanupfunc = cleanup
		auth = &handler.AuthHandler{
			Users:   gorms.NewUserRepoGORM(gormDB), // 替换
			JWT:     jwtSvc,
			Revoker: nil, // 有 Redis 再接
		}
		userService := service.NewUserService(gorms.NewUserRepoGORM(gormDB))
		user = &handler.UserHandler{
			Svc: userService,
		}
		MenuHandler = &handler.MenuHandler{
			Repo: gorms.NewMenuRepoGORM(gormDB),
		}
		PermissionHandler = &handler.PermissionHandler{
			PermRepo: gorms.NewPermissionMySQLGORM(gormDB),
		}
	case "MONGO":
		mongodb, cleanup, err := db.NewMongoFromEnv()
		if err != nil {
			lg.Error("db init", "err", err)
		}
		cleanupfunc = cleanup
		auth = &handler.AuthHandler{
			Users:   mongos.NewUserRepoMongo(mongodb.DB), // 替换
			JWT:     jwtSvc,
			Revoker: nil, // 有 Redis 再接
		}

		userService := service.NewUserService(mongos.NewUserRepoMongo(mongodb.DB))
		user = &handler.UserHandler{
			Svc: userService,
		}
		MenuHandler = &handler.MenuHandler{
			Repo: mongos.NewMenuRepoMongo(mongodb.DB),
		}
		PermissionHandler = &handler.PermissionHandler{
			PermRepo: mongos.NewPermissionMongo(mongodb.DB),
		}

	default:
		// 默认使用 GORM
		gormDB, cleanup, err := db.NewGORMFromEnv()
		if err != nil {
			lg.Error("db init", "err", err)
		}
		cleanupfunc = cleanup
		auth = &handler.AuthHandler{
			Users:   gorms.NewUserRepoGORM(gormDB), // 替换
			JWT:     jwtSvc,
			Revoker: nil, // 有 Redis 再接
		}
		userService := service.NewUserService(gorms.NewUserRepoGORM(gormDB))
		user = &handler.UserHandler{
			Svc: userService,
		}
		MenuHandler = &handler.MenuHandler{
			Repo: gorms.NewMenuRepoGORM(gormDB),
		}
		PermissionHandler = &handler.PermissionHandler{
			PermRepo: gorms.NewPermissionMySQLGORM(gormDB),
		}
	}
	if cfg.Env != "prod" {
		gin.SetMode(gin.ReleaseMode)
	}
	r := gin.New()
	r.Use(gin.Logger())
	r.Use(gin.Recovery())
	r.Use(middleware.RequestID())
	r.Use(middleware.TraceIDMiddleware())
	// Public API group (no auth)
	rPublic := r.Group("/api")
	rPublic.POST("/login", auth.Login)
	rPublic.POST("/refresh", auth.Refresh)
	rAuth := r.Group("/api")
	rAuth.Use(middleware.AuthRequired(middleware.AuthOptions{
		JWT:     jwtSvc,
		Revoker: nil,
	}))
	user.RegisterRoutes(rAuth)
	user.RegisterRoutes(rAuth)
	perm := rAuth.Group("/permissions")
	{
		perm.GET("/user-menus", PermissionHandler.ListUserMenuIDs) // 返回[]uint64
		perm.POST("/user-menus", PermissionHandler.SaveUserMenus)  // { userId, menuIds }
	}
	rAuth.GET("/menus", MenuHandler.GetMyMenus)    // 你原来的
	rAuth.GET("/menus/tree", MenuHandler.GetTree)  // 新：管理页树
	rAuth.GET("/menus/:id", MenuHandler.GetOne)    // 新：详情
	rAuth.POST("/menus", MenuHandler.Create)       // 新：新增
	rAuth.PUT("/menus/:id", MenuHandler.Update)    // 新：修改
	rAuth.DELETE("/menus/:id", MenuHandler.Delete) // 新：删除
	rAuth.PUT("/me/password", auth.UpdateUserPassword)
	rAuth.GET("/me", func(c *gin.Context) {
		uid, _ := c.Get("uid")
		uname, _ := c.Get("uname")
		response.OK(c, gin.H{
			"userId":   uid,
			"username": uname,
		})
	})
	return r
}

// hub := websocket.NewHub()
// go hub.Run()

// rAuth.GET("/ws", func(c *gin.Context) {
// 	hub.HandleConnections(c.Writer, c.Request)
// })

// rAuth.GET("/", func(c *gin.Context) {
// 	c.String(http.StatusOK, "WebSocket server running! ws://localhost:8080/ws")
// })

// hub := websocket.NewHub()
// go hub.Run()

// rAuth.GET("/ws", func(c *gin.Context) {
//     hub.HandleConnections(c.Writer, c.Request)
// })

// rAuth.GET("/", func(c *gin.Context) {
//     c.String(http.StatusOK, "WebSocket server running! ws://localhost:8080/ws")
// })
// if cfg.Env == "prod" {
// 	gin.SetMode(gin.ReleaseMode)
// }

// permOpt := middleware.PermOptions{
// 	Repo:      MenuHandler.Repo, // 你已有的 ListByUser 实现
// 	TTL:       30 * time.Second, // 可选缓存
// 	Whitelist: nil,
// }
// r.Use(middleware.ACLGuard(permOpt))

//r.POST("/api/register", auth.Register) // 可选
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
