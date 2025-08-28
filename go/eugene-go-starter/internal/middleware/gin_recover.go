// internal/middleware/recovery.go
package middleware

import (
	"bytes"
	"net/http"
	"runtime/debug"

	"github.com/gin-gonic/gin"
)

func RecoveryWithStack() gin.HandlerFunc {
	return gin.CustomRecovery(func(c *gin.Context, recovered any) {
		var buf bytes.Buffer
		req := c.Request
		buf.WriteString("[PANIC] ")
		buf.WriteString(req.Method + " " + req.URL.String() + "\n")
		buf.WriteString("User-Agent: " + req.UserAgent() + "\n")
		buf.WriteString("RemoteAddr: " + req.RemoteAddr + "\n\n")
		buf.Write(debug.Stack())

		// 控制台直接看堆栈
		println(buf.String())

		// 给前端返回“可读”错误（dev 才带细节）
		if gin.Mode() == gin.DebugMode {
			c.AbortWithStatusJSON(http.StatusInternalServerError, gin.H{
				"code":    500,
				"message": "internal error (dev)",
				"detail":  recovered, // panic 内容
			})
		} else {
			c.AbortWithStatusJSON(http.StatusInternalServerError, gin.H{
				"code":    500,
				"message": "internal server error",
			})
		}
	})
}
