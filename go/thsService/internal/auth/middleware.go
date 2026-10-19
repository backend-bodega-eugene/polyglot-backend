package auth

import (
	"gorm.io/gorm"
	"log/slog"
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"

	"ths-service/internal/response"
)

// RequireActiveMember rejects deleted/disabled users even with an unexpired JWT.
func RequireActiveMember(db *gorm.DB) gin.HandlerFunc {
	return func(c *gin.Context) {
		var user User
		err := db.WithContext(c.Request.Context()).Select("id", "status").First(&user, c.GetUint64(UserIDContextKey)).Error
		if err != nil && err != gorm.ErrRecordNotFound {
			slog.Error("check member status", "error", err)
			response.Error(c, 500, response.CodeInternalError, "服务器内部错误")
			c.Abort()
			return
		}
		if err != nil || user.Status != UserStatusActive {
			response.Error(c, 401, response.CodeUnauthorized, "会员已停用或删除，请重新登录")
			c.Abort()
			return
		}
		c.Next()
	}
}

const UserIDContextKey = "userID"

func RequireAccessToken(tokens *TokenIssuer) gin.HandlerFunc {
	return func(c *gin.Context) {
		header := strings.TrimSpace(c.GetHeader("Authorization"))
		parts := strings.Fields(header)
		if len(parts) != 2 || !strings.EqualFold(parts[0], "Bearer") {
			response.Error(c, http.StatusUnauthorized, response.CodeUnauthorized, "请先登录")
			c.Abort()
			return
		}

		userID, err := tokens.Parse(parts[1])
		if err != nil {
			response.Error(c, http.StatusUnauthorized, response.CodeUnauthorized, "登录状态无效或已过期")
			c.Abort()
			return
		}

		c.Set(UserIDContextKey, userID)
		c.Next()
	}
}
