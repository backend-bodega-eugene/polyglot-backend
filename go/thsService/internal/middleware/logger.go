package middleware

import (
	"log/slog"
	"time"

	"github.com/gin-gonic/gin"
)

func Logger() gin.HandlerFunc {
	return func(c *gin.Context) {
		startedAt := time.Now()
		c.Next()

		slog.Info("http request",
			"requestId", c.GetString("requestId"),
			"method", c.Request.Method,
			"path", c.Request.URL.Path,
			"status", c.Writer.Status(),
			"durationMs", time.Since(startedAt).Milliseconds(),
			"clientIp", c.ClientIP(),
		)
	}
}
