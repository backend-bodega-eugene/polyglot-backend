// internal/middleware/auth.go
package middleware

import (
	"strings"

	"eugene-go-starter/internal/jwtutil"
	"eugene-go-starter/pkg/response"

	"github.com/gin-gonic/gin"
)

type Revoker interface {
	// IsRevoked 返回该 jti 是否已吊销
	IsRevoked(jti string) (bool, error)
}

type AuthOptions struct {
	JWT     *jwtutil.Service
	Revoker Revoker // 可传空实现
}

func AuthRequired(opt AuthOptions) gin.HandlerFunc {
	return func(c *gin.Context) {
		auth := c.GetHeader("Authorization")
		if !strings.HasPrefix(strings.ToLower(auth), "bearer ") {
			response.SetResultFail(c, 10002)
			c.Abort()
			return
		}
		token := strings.TrimSpace(auth[len("Bearer "):])
		claims, err := opt.JWT.Parse(token)
		if err != nil {
			response.SetResultFail(c, 10008)
			c.Abort()
			return
		}
		if opt.Revoker != nil {
			if revoked, err := opt.Revoker.IsRevoked(claims.ID); err != nil {
				response.InternalError(c, "revoker error")
				c.Abort()
				return
			} else if revoked {
				response.SetResultFail(c, 10008)
				c.Abort()
				return
			}
		}

		// 注入上下文
		c.Set("uid", claims.UserID)
		//c.Set("sid", claims.SiteID)
		//c.Set("role", claims.Role)
		c.Set("uname", claims.Username)
		c.Next()
	}
}

// 可选：需要某角色
func RequireRole(role string) gin.HandlerFunc {
	return func(c *gin.Context) {
		if r, ok := c.Get("role"); !ok || r.(string) != role {
			response.SetResultFail(c, 10008)
			c.Abort()
			return
		}
		c.Next()
	}
}
