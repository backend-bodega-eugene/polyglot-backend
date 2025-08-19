// internal/handler/auth.go
package handler

import (
	"net/http"
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
	u, err := h.Users.FindBySiteAndUsername(c, in.Username)
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

// 简单登出：把 jti 加入黑名单（需要 Revoker 实现，如 Redis）
func (h *AuthHandler) Logout(c *gin.Context) {
	jtiAny, ok := c.Get("jti") // 如果你在中间件里也想 Set("jti", claims.ID) 就能拿到
	if !ok || h.Revoker == nil {
		c.Status(http.StatusNoContent)
		return
	}
	// 黑名单 TTL 设为剩余有效期：这里简单给 AccessTTL
	if err := h.Revoker.Revoke(jtiAny.(string), time.Hour*24); err != nil {
		response.InternalError(c, "logout failed")
		return
	}
	c.Status(http.StatusNoContent)
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
