package middleware

import (
	"log/slog"
	"net/http"
	"runtime/debug"

	"github.com/gin-gonic/gin"

	"ths-service/internal/response"
)

func Recovery() gin.HandlerFunc {
	return func(c *gin.Context) {
		defer func() {
			if recovered := recover(); recovered != nil {
				slog.Error("panic recovered",
					"requestId", c.GetString("requestId"),
					"error", recovered,
					"stack", string(debug.Stack()),
				)
				response.Error(c, http.StatusInternalServerError, response.CodeInternalError, "服务器内部错误")
				c.Abort()
			}
		}()

		c.Next()
	}
}
