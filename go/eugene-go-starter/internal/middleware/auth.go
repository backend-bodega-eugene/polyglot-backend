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
	const bearerPrefix = "bearer "

	return func(c *gin.Context) {
		// 基础保护
		if opt.JWT == nil {
			response.InternalError(c, "jwt service not initialized",nil)
			c.Abort()
			return
		}

		// 取并校验 Authorization
		auth := strings.TrimSpace(c.GetHeader("Authorization"))
		al := strings.ToLower(auth)
		if !strings.HasPrefix(al, bearerPrefix) {
			response.SetResultFail(c, 10002) // 未授权/缺少token
			c.Abort()
			return
		}
		token := strings.TrimSpace(auth[len(bearerPrefix):])
		if token == "" {
			response.SetResultFail(c, 10002)
			c.Abort()
			return
		}

		// 解析并校验 JWT（过期/签名/nbf 等交给 jwtutil.Service）
		claims, err := opt.JWT.Parse(token)
		if err != nil || claims == nil {
			response.SetResultFail(c, 10008) // token 无效
			c.Abort()
			return
		}

		// 黑名单（按 JTI）检查
		if opt.Revoker != nil && claims.ID != "" {
			if revoked, err := opt.Revoker.IsRevoked(claims.ID); err != nil {
				response.InternalError(c, "revoker error",err)
				c.Abort()
				return
			} else if revoked {
				response.SetResultFail(c, 10008) // token 已吊销
				c.Abort()
				return
			}
		}

		// 注入上下文（下游可直接取用）
		c.Set("uid", claims.UserID)
		c.Set("uname", claims.Username)
		c.Set("jti", claims.ID)        // 方便 logout 直接用（可选）
		c.Set("claims", claims)        // 需要更多字段时更方便（可选）

		c.Next()
	}
}

// 可选：需要某角色（你后面要加角色就启用）
// func RequireRole(role string) gin.HandlerFunc {
// 	return func(c *gin.Context) {
// 		if r, ok := c.Get("role"); !ok || r.(string) != role {
// 			response.SetResultFail(c, 10008)
// 			c.Abort()
// 			return
// 		}
// 		c.Next()
// 	}
// }
