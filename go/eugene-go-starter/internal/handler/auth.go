// internal/handler/auth.go
package handler

import (
	"net/http"
	"strings"
	"time"

	"eugene-go-starter/internal/handler/dto"
	"eugene-go-starter/internal/jwtutil"
	"eugene-go-starter/internal/repo"
	"eugene-go-starter/pkg/response"

	"github.com/gin-gonic/gin"
	"golang.org/x/crypto/bcrypt"
)

// 你现有的 User 模型与查询请替换

// type UserRepo interface {
// 	FindByUsername(siteID uint64, username string) (*model.User, error)
// }

type Revoker interface {
	Revoke(jti string, ttl time.Duration) error
}

type AuthHandler struct {
	Users   repo.UserRepo
	JWT     *jwtutil.Service
	Revoker Revoker // 可为 nil
}

// Login godoc
// @Summary 登录
// @Tags Auth
// @Accept json
// @Produce json
// @Param body body dto.LoginReq true "登录参数"
// @Success 200 {object} LoginOK
// @Failure 400 {object} ErrResp
// @Failure 401 {object} ErrResp
// @Router /api/login [post]

func (h *AuthHandler) Login(c *gin.Context) {
	var in dto.LoginReq
	if err := c.ShouldBindJSON(&in); err != nil {
		response.BadRequest(c, "invalid body")
		return
	}

	// str, err := bcrypt.GenerateFromPassword([]byte("eugene"), 10)
	// if err == nil {
	// 	fmt.Println(string(str))
	// 	fmt.Println(len(string(str)))
	// 	//response.SetResultFail(c, 10008)
	// }
	// ✅ 传入真正的 context.Context，避免把 *gin.Context 当 ctx 用
	u, err := h.Users.FindBySiteAndUsername(c.Request.Context(), in.Username)
	if err != nil || u == nil || u.Status != 1 {
		response.SetResultFail(c, 10008)
		return
	}
	if err := bcrypt.CompareHashAndPassword([]byte(u.PasswordHash), []byte(in.Password)); err != nil {
		response.SetResultFail(c, 10008)
		return
	}
	token, exp, jti, err := h.JWT.GenerateAccessToken(u.UserID, u.SiteID, u.Username)
	if err != nil {
		response.InternalError(c, "issue token failed")
		return
	}
	response.OK(c, gin.H{
		"token":     token,
		"expiresAt": exp.Unix(),
		"jti":       jti, // 前端一般用不到，可不返回
	})
}

type RefreshDTO struct {
	Token string `json:"token" binding:"required"`
}

// Refresh godoc
// @Summary 刷新当前 token
// @Tags Auth
// @Accept json
// @Produce json
// @Param body body RefreshDTO true "刷新参数"
// @Success 200 {object} LoginOK
// @Failure 401 {object} ErrResp
// @Router /api/refresh [post]
func (h *AuthHandler) Refresh(c *gin.Context) {
	var in RefreshDTO
	if err := c.ShouldBindJSON(&in); err != nil {
		response.BadRequest(c, "invalid body")
		return
	}
	newToken, exp, jti, err := h.JWT.Refresh(in.Token)
	if err != nil {
		response.SetResultFail(c, 10008)
		return
	}
	response.OK(c, gin.H{
		"token":     newToken,
		"expiresAt": exp.Unix(),
		"jti":       jti,
	})
}

// 简单登出：把 jti 加入黑名单（需要 Revoker 实现，如内存或 Redis）
func (h *AuthHandler) Logout(c *gin.Context) {
	// 优先走中间件注入（如果该路由挂了 AuthRequired）
	var jti string
	var exp time.Time
	if v, ok := c.Get("claims"); ok {
		if cl, ok2 := v.(*jwtutil.Claims); ok2 && cl != nil {
			jti = cl.ID
			if cl.ExpiresAt != nil {
				exp = cl.ExpiresAt.Time
			}
		}
	}

	// 兜底：没挂中间件时，从 Authorization 解析一次
	if jti == "" {
		if h.JWT == nil {
			c.Status(http.StatusNoContent)
			return
		}
		auth := strings.TrimSpace(c.GetHeader("Authorization"))
		if !strings.HasPrefix(strings.ToLower(auth), "bearer ") {
			c.Status(http.StatusNoContent)
			return
		}
		token := strings.TrimSpace(auth[len("bearer "):])
		claims, err := h.JWT.Parse(token)
		if err != nil || claims == nil {
			c.Status(http.StatusNoContent)
			return
		}
		jti = claims.ID
		if claims.ExpiresAt != nil {
			exp = claims.ExpiresAt.Time
		}
	}

	// 没有 revoker 或没有 jti，则直接幂等返回
	if h.Revoker == nil || jti == "" {
		c.Status(http.StatusNoContent)
		return
	}

	// TTL = 剩余有效期（小于0则置0）
	ttl := time.Until(exp)
	if ttl < 0 {
		ttl = 0
	}
	// 加入黑名单；失败也按设计返回 500
	if err := h.Revoker.Revoke(jti, ttl); err != nil {
		response.InternalError(c, "logout failed")
		return
	}

	c.Status(http.StatusNoContent)
}

func (h *AuthHandler) UpdateUserPassword(c *gin.Context) {
	var in UpdatePasswordDTO
	if err := c.ShouldBindJSON(&in); err != nil {
		response.BadRequest(c, "invalid body")
		return
	}
	if in.OldPwd == in.NewPwd {
		response.BadRequest(c, "new password same as old")
		return
	}
	uidAny, exists := c.Get("uid")
	if !exists {
		response.SetResultFail(c, 10008)
		return
	}
	uid := uidAny.(uint64)

	if err := h.Users.UpdatePassword(c.Request.Context(), uid, in.OldPwd, in.NewPwd); err != nil {
		// 统一外部语义：用户不存在/旧密码错 -> 旧密码不正确
		switch err {
		case repo.ErrUserNotFound, repo.ErrOldPasswordIncorrect:
			response.SetResultFail(c, 10008)
			return
		case repo.ErrUpdateFailed:
			response.SetResultFail(c, 10007)
		case repo.ErrHashWrong:
			response.SetResultFail(c, 10006)
			return
		default:
			response.SetResultFail(c, 10005)
			return
		}
	}
	response.OK(c, "password updated")
}

type UpdatePasswordDTO struct {
	OldPwd string `json:"oldPwd" binding:"required"`
	NewPwd string `json:"newPwd" binding:"required"`
}

// 定义返回体（Swagger 用）
type LoginOK struct {
	Token     string `json:"token" example:"eyJhbGciOiJIUzI1NiIs..."`
	ExpiresAt int64  `json:"expiresAt" example:"1734567890"`
	JTI       string `json:"jti" example:"7a1f8d9c4e..."` // 可选
}

// 统一错误返回（Swagger 用）
type ErrResp struct {
	Code int    `json:"code" example:"10008"`
	Msg  string `json:"msg"  example:"wrong token"`
}
