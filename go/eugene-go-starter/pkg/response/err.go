// pkg/response/err.go
package response

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

func InternalError(c *gin.Context, msg string, err error) {
	if gin.Mode() == gin.DebugMode {
		c.JSON(http.StatusInternalServerError, gin.H{
			"code":    500,
			"message": msg,
			"error":   err.Error(), // 关键：把真实 err 打回前端（仅 dev）
		})
	} else {
		c.JSON(http.StatusInternalServerError, gin.H{
			"code":    500,
			"message": msg,
		})
	}
}
