package router

import (
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
	swaggerFiles "github.com/swaggo/files"
	ginSwagger "github.com/swaggo/gin-swagger"

	"ths-service/internal/auth"
	"ths-service/internal/config"
	"ths-service/internal/database"
	"ths-service/internal/market"
	"ths-service/internal/middleware"
	"ths-service/internal/portfolio"
	"ths-service/internal/response"
)

func New(connection *database.Connection, cfg config.Config) *gin.Engine {
	engine := gin.New()
	engine.HandleMethodNotAllowed = true
	if err := engine.SetTrustedProxies(cfg.Server.TrustedProxies); err != nil {
		panic(err)
	}
	engine.Use(middleware.RequestID(), middleware.Logger(), middleware.Recovery())

	if cfg.Swagger.Enabled {
		engine.GET("/swagger/*any", ginSwagger.WrapHandler(swaggerFiles.Handler))
	}

	api := engine.Group("/api/app")

	authRepository := auth.NewRepository(connection.GORM)
	tokenIssuer := auth.NewTokenIssuer(
		cfg.JWT.Secret,
		time.Duration(cfg.JWT.AccessTokenTTLSeconds)*time.Second,
	)
	authService := auth.NewService(authRepository, tokenIssuer, cfg.JWT.AccessTokenTTLSeconds)
	authHandler := auth.NewHandler(authService)
	api.POST(
		"/auth/register",
		middleware.IPRateLimit(1, time.Minute, "注册请求过于频繁，请一分钟后再试"),
		authHandler.Register,
	)
	api.POST(
		"/auth/login",
		middleware.IPRateLimit(5, time.Minute, "登录请求过于频繁，请稍后再试"),
		authHandler.Login,
	)

	marketRepository := market.NewRepository(connection.GORM)
	marketProvider := market.NewProvider(time.Duration(cfg.Market.RequestTimeoutSeconds) * time.Second)
	marketService := market.NewService(
		marketRepository,
		marketProvider,
		time.Duration(cfg.Market.CacheTTLSeconds)*time.Second,
	)
	marketHandler := market.NewHandler(marketService)
	protected := api.Group("")
	protected.Use(auth.RequireAccessToken(tokenIssuer))
	protected.GET("/market/quotes", marketHandler.List)
	portfolioService := portfolio.NewService(portfolio.NewRepository(connection.GORM), marketService)
	portfolioHandler := portfolio.NewHandler(portfolioService)
	// 已停用旧持仓入口，分类持仓统一使用 /market/holdings。
	// protected.GET("/portfolio/positions", portfolioHandler.Positions)
	protected.GET("/portfolio/trades", portfolioHandler.Trades)
	protected.GET("/market/holdings", portfolioHandler.Holdings)

	engine.NoRoute(func(c *gin.Context) {
		response.Error(c, http.StatusNotFound, response.CodeNotFound, "接口不存在")
	})
	engine.NoMethod(func(c *gin.Context) {
		response.Error(c, http.StatusMethodNotAllowed, response.CodeMethodNotAllowed, "请求方法不允许")
	})

	return engine
}
