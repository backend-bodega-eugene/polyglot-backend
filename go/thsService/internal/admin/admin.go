package admin

import (
	"bytes"
	"crypto/sha256"
	"embed"
	"errors"
	"fmt"
	"io/fs"
	"log/slog"
	"net/http"
	"net/url"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
	"golang.org/x/crypto/bcrypt"
	"gorm.io/gorm"
	"ths-service/internal/auth"
	"ths-service/internal/response"
)

//go:embed web
var pages embed.FS

const cookieName = "ths_admin_session"

type Administrator struct {
	ID           uint64     `json:"id"`
	Username     string     `json:"username"`
	PasswordHash string     `json:"-"`
	IsSuperAdmin bool       `json:"isSuperAdmin"`
	Status       uint8      `json:"-"`
	LastLoginAt  *time.Time `json:"-"`
	CreatedAt    time.Time  `json:"-"`
	UpdatedAt    time.Time  `json:"-"`
}

func (Administrator) TableName() string { return "administrators" }

type LoginRequest struct {
	Username string `json:"username" binding:"required,max=64"`
	Password string `json:"password" binding:"required,max=72"`
}
type LoginData struct {
	Admin     Administrator `json:"admin"`
	ExpiresIn int           `json:"expiresIn"`
}
type LoginResponse struct {
	Code      int       `json:"code"`
	Message   string    `json:"message"`
	Data      LoginData `json:"data"`
	RequestID string    `json:"requestId"`
}
type MeResponse struct {
	Code      int           `json:"code"`
	Message   string        `json:"message"`
	Data      Administrator `json:"data"`
	RequestID string        `json:"requestId"`
}

type Handler struct {
	db     *gorm.DB
	tokens *auth.TokenIssuer
	ttl    int
	guard  *loginGuard
}

func NewHandler(db *gorm.DB, secret string, ttl int) *Handler {
	// Domain-separated signing key prevents member and administrator tokens being interchanged.
	key := sha256.Sum256([]byte("ths-service/admin/session/v1\x00" + secret))
	return &Handler{db: db, tokens: auth.NewTokenIssuer(fmt.Sprintf("%x", key), time.Duration(ttl)*time.Second), ttl: ttl, guard: newLoginGuard(db)}
}

var dummyHash = func() []byte {
	h, e := bcrypt.GenerateFromPassword([]byte("admin-dummy-password"), bcrypt.DefaultCost)
	if e != nil {
		panic(e)
	}
	return h
}()

func Register(engine *gin.Engine, db *gorm.DB, secret string, ttl int) {
	h := NewHandler(db, secret, ttl)
	RegisterAPK(engine, h)
	RegisterMembers(engine, h)
	RegisterPDFAssets(engine, h)
	RegisterPDFReports(engine, h)
	RegisterElectricity(engine, h)
	RegisterBills(engine, h)
	RegisterTax(engine, h)
	RegisterNanwang(engine, h)
	RegisterTaxBusiness(engine, h)
	RegisterAlipay(engine, h)
	api := engine.Group("/api/admin")
	api.Use(func(c *gin.Context) { c.Header("Cache-Control", "no-store") })
	api.POST("/auth/login", sameOrigin, h.LoginIPGuard, h.Login)
	api.POST("/auth/logout", sameOrigin, h.Logout)
	api.GET("/auth/me", h.RequireLogin, h.Me)
	files, _ := fs.Sub(pages, "web")
	servePage := func(c *gin.Context) {
		name := strings.TrimPrefix(c.Param("filepath"), "/")
		if name == "" {
			name = "index.html"
		}
		content, err := fs.ReadFile(files, name)
		if err != nil {
			c.Status(http.StatusNotFound)
			return
		}
		c.Header("Cache-Control", "no-store")
		http.ServeContent(c.Writer, c.Request, name, time.Time{}, bytes.NewReader(content))
	}
	engine.GET("/admin/*filepath", servePage)
	engine.HEAD("/admin/*filepath", servePage)
}

func sameOrigin(c *gin.Context) {
	if origin := c.GetHeader("Origin"); origin != "" {
		u, err := url.Parse(origin)
		if err != nil || !strings.EqualFold(u.Host, c.Request.Host) || (u.Scheme != "http" && u.Scheme != "https") {
			response.Error(c, 403, response.CodeUnauthorized, "请求来源无效")
			c.Abort()
			return
		}
	}
	c.Next()
}

// Login 管理员登录
// @Summary 管理员登录
// @Description 同IP滚动一分钟最多5次登录请求，第6次冻结10分钟；同管理员滚动一分钟3次失败冻结10分钟，跨IP累计且成功后清零。冻结期间不延长冻结，不影响已登录会话。429返回Retry-After秒数。计数落库，重启后保留。成功设置HttpOnly会话Cookie。
// @Tags 后台 · 管理员认证
// @Accept json
// @Produce json
// @Param body body LoginRequest true "管理员账号密码"
// @Success 200 {object} LoginResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 429 {object} response.ErrorEnvelope
// @Failure 500 {object} response.ErrorEnvelope
// @Router /api/admin/auth/login [post]
func (h *Handler) Login(c *gin.Context) {
	var req LoginRequest
	c.Request.Body = http.MaxBytesReader(c.Writer, c.Request.Body, 4096)
	if !strings.HasPrefix(c.GetHeader("Content-Type"), "application/json") || c.ShouldBindJSON(&req) != nil {
		response.Error(c, 400, response.CodeInvalidRequest, "请输入有效的用户名和密码")
		return
	}
	var a Administrator
	err := h.db.WithContext(c.Request.Context()).Where("username = ?", strings.TrimSpace(req.Username)).First(&a).Error
	if err != nil && !errors.Is(err, gorm.ErrRecordNotFound) {
		h.fail(c, err)
		return
	}
	hash := []byte(a.PasswordHash)
	if err != nil {
		hash = dummyHash
	}
	valid := false
	accountKey := "name:" + strings.ToLower(strings.TrimSpace(req.Username))
	if err == nil {
		accountKey = fmt.Sprintf("id:%d", a.ID)
	}
	retry, guardErr := h.guard.gate(c.Request.Context(), "account", accountKey, func() bool {
		valid = bcrypt.CompareHashAndPassword(hash, []byte(req.Password)) == nil && err == nil && a.Status == 1
		return valid
	})
	if guardErr != nil {
		h.fail(c, guardErr)
		return
	}
	if retry > 0 {
		loginBlocked(c, retry)
		return
	}
	if err != nil || !valid || a.Status != 1 {
		response.Error(c, 401, response.CodeUnauthorized, "用户名或密码错误")
		return
	}
	token, err := h.tokens.Issue(a.ID)
	if err != nil {
		h.fail(c, err)
		return
	}
	if err = h.db.WithContext(c.Request.Context()).Model(&Administrator{}).Where("id = ?", a.ID).Update("last_login_at", time.Now()).Error; err != nil {
		h.fail(c, err)
		return
	}
	h.setCookie(c, token, h.ttl)
	response.Success(c, LoginData{Admin: a, ExpiresIn: h.ttl})
}
func (h *Handler) setCookie(c *gin.Context, token string, age int) {
	c.SetSameSite(http.SameSiteStrictMode)
	c.SetCookie(cookieName, token, age, "/api/admin", "", c.Request.TLS != nil || c.GetHeader("X-Forwarded-Proto") == "https", true)
}
func (h *Handler) RequireLogin(c *gin.Context) {
	token, _ := c.Cookie(cookieName)
	id, err := h.tokens.Parse(token)
	if err != nil {
		response.Error(c, 401, response.CodeUnauthorized, "请先登录管理员账号")
		c.Abort()
		return
	}
	var a Administrator
	err = h.db.WithContext(c.Request.Context()).First(&a, id).Error
	if err != nil && !errors.Is(err, gorm.ErrRecordNotFound) {
		h.fail(c, err)
		c.Abort()
		return
	}
	if err != nil || a.Status != 1 {
		response.Error(c, 401, response.CodeUnauthorized, "管理员登录已失效")
		c.Abort()
		return
	}
	c.Set("administrator", a)
	c.Next()
}

// Me 获取当前管理员
// @Summary 获取当前管理员
// @Description 使用管理员登录接口设置的会话Cookie验证身份。
// @Tags 后台 · 管理员认证
// @Produce json
// @Success 200 {object} MeResponse
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 500 {object} response.ErrorEnvelope
// @Router /api/admin/auth/me [get]
func (h *Handler) Me(c *gin.Context) { a, _ := c.Get("administrator"); response.Success(c, a) }

// Logout 管理员退出登录
// @Summary 管理员退出登录
// @Tags 后台 · 管理员认证
// @Produce json
// @Success 200 {object} response.Envelope
// @Router /api/admin/auth/logout [post]
func (h *Handler) Logout(c *gin.Context) { h.setCookie(c, "", -1); response.Success(c, nil) }
func (h *Handler) fail(c *gin.Context, err error) {
	slog.Error("admin request", "requestId", c.GetString("requestId"), "error", err)
	response.Error(c, 500, response.CodeInternalError, "服务器内部错误")
}
