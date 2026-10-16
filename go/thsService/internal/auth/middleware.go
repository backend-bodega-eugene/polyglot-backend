package auth

import (
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"

	"ths-service/internal/response"
)

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
