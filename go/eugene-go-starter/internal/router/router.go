package router

import (
	"eugene-go-starter/internal/handler"
	"eugene-go-starter/internal/jwtutil"
	"eugene-go-starter/internal/middleware"
	"eugene-go-starter/internal/repo"
	"eugene-go-starter/internal/service"
	"eugene-go-starter/pkg/config"
	"eugene-go-starter/pkg/response"

	"github.com/gin-gonic/gin"
	"github.com/jmoiron/sqlx"
	"gorm.io/driver/mysql"
	"gorm.io/gorm"

	"eugene-go-starter/internal/db"
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
func New(cfg *config.Config, dbx *sqlx.DB) *gin.Engine {
	jwtCfg := config.LoadJWT()
	jwtSvc := jwtutil.New(jwtCfg)

	// dynamic select repo by DATABASE_TYPE
	var userRepo repo.UserRepo
	var menuRepo repo.MenuRepo
	var permRepo repo.UserMenuRepo
	var gormDB *gorm.DB
	if cfg.DatabaseType == "gorm" {
		gormDB, _ = gorm.Open(mysql.New(mysql.Config{Conn: dbx.DB}), &gorm.Config{})
		userRepo = repo.NewUserRepoGorm(gormDB)
		menuRepo = repo.NewMenuRepoGorm(gormDB)
		permRepo = repo.NewPermissionGorm(gormDB)
	} else if cfg.DatabaseType == "mongo" {
		mongoDB, _, _ := db.NewMongoFromEnv()
		userRepo = repo.NewUserRepoMongo(mongoDB)
		menuRepo = repo.NewMenuRepoMongo(mongoDB)
		permRepo = repo.NewPermissionMongo(mongoDB)
	} else {
		userRepo = repo.NewUserRepoSQLX(dbx)
		menuRepo = repo.NewMenuRepoSQLX(dbx)
		permRepo = repo.NewPermissionMySQL(dbx)
	}

	auth := &handler.AuthHandler{
		Users:   userRepo,
		JWT:     jwtSvc,
		Revoker: nil, // 有 Redis 再接
	}
	MenuHandler := &handler.MenuHandler{
		Repo: menuRepo,
	}
	userService := service.NewUserService(userRepo)
	user := &handler.UserHandler{
		Svc: userService,
	}
	if cfg.Env == "prod" {
		gin.SetMode(gin.ReleaseMode)
	}
	PermissionHandler := &handler.PermissionHandler{
		PermRepo: permRepo,
	}
	gin.SetMode(gin.DebugMode)
	r := gin.New()

	r.Use(gin.Logger())
	// Panic 兜底（带堆栈 + 请求详情）
	r.Use(middleware.RecoveryWithStack())
	r.Use(gin.Recovery())
	r.Use(middleware.RequestID())
	r.Use(middleware.TraceIDMiddleware())
	// permOpt := middleware.PermOptions{
	// 	Repo:      MenuHandler.Repo, // 你已有的 ListByUser 实现
	// 	TTL:       30 * time.Second, // 可选缓存
	// 	Whitelist: nil,
	// }
	// r.Use(middleware.ACLGuard(permOpt))
	r.POST("/api/login", auth.Login)
	//r.POST("/api/register", auth.Register) // 可选
	r.POST("/api/refresh", auth.Refresh)
	user.RegisterRoutes(r)
	perm := r.Group("/api/permissions")
	{
		perm.GET("/user-menus", PermissionHandler.ListUserMenuIDs) // 返回[]uint64
		perm.POST("/user-menus", PermissionHandler.SaveUserMenus)  // { userId, menuIds }
	}

	rAuth := r.Group("/api")
	rAuth.Use(middleware.AuthRequired(middleware.AuthOptions{
		JWT:     jwtSvc,
		Revoker: nil,
	}))
	{
		//h := &handler.MenuHandler{Repo: menusRepo}
		rAuth.GET("/menus", MenuHandler.GetMyMenus)    // 你原来的
		rAuth.GET("/menus/tree", MenuHandler.GetTree)  // 新：管理页树
		rAuth.GET("/menus/:id", MenuHandler.GetOne)    // 新：详情
		rAuth.POST("/menus", MenuHandler.Create)       // 新：新增
		rAuth.PUT("/menus/:id", MenuHandler.Update)    // 新：修改
		rAuth.DELETE("/menus/:id", MenuHandler.Delete) // 新：删除
		//rAuth.GET("/menus", MenuHandler.GetMyMenus)
		rAuth.PUT("/me/password", auth.UpdateUserPassword)
		rAuth.GET("/me", func(c *gin.Context) {
			uid, _ := c.Get("uid")
			uname, _ := c.Get("uname")
			response.OK(c, gin.H{
				"userId":   uid,
				"username": uname,
			})
		})
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
