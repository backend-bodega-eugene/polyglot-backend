package auth

import (
	"errors"
	"log/slog"
	"net/http"

	"github.com/gin-gonic/gin"

	"ths-service/internal/response"
	"ths-service/internal/validation"
)

type Handler struct {
	service *Service
}

func NewHandler(service *Service) *Handler {
	return &Handler{service: service}
}

// Register 用户注册
// @Summary 用户注册
// @Description 使用中国大陆手机号或邮箱注册，真实姓名必填；同一 IP 一分钟只能请求一次
// @Tags 认证
// @Accept json
// @Produce json
// @Param request body RegisterRequest true "注册参数"
// @Success 201 {object} RegisterResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 409 {object} response.ErrorEnvelope
// @Failure 429 {object} response.ErrorEnvelope
// @Failure 500 {object} response.ErrorEnvelope
// @Router /api/app/auth/register [post]
func (h *Handler) Register(c *gin.Context) {
	var request RegisterRequest
	if err := c.ShouldBindJSON(&request); err != nil {
		response.Error(c, http.StatusBadRequest, response.CodeInvalidRequest, validation.Message(err))
		return
	}

	data, err := h.service.Register(request)
	if err == nil {
		response.SuccessWithStatus(c, http.StatusCreated, data)
		return
	}

	switch {
	case errors.Is(err, ErrInvalidAccount):
		response.Error(c, http.StatusBadRequest, response.CodeInvalidRequest, "账户名必须是中国大陆手机号或邮箱")
	case errors.Is(err, ErrInvalidRealName):
		response.Error(c, http.StatusBadRequest, response.CodeInvalidRequest, "真实姓名长度必须为 2 到 64 个字符")
	case errors.Is(err, ErrInvalidPassword):
		response.Error(c, http.StatusBadRequest, response.CodeInvalidRequest, "密码长度必须为 8 到 72 个字节")
	case errors.Is(err, ErrAccountExists):
		response.Error(c, http.StatusConflict, response.CodeAccountExists, "该手机号或邮箱已经注册")
	default:
		slog.Error("register user", "requestId", c.GetString("requestId"), "error", err)
		response.Error(c, http.StatusInternalServerError, response.CodeInternalError, "服务器内部错误")
	}
}

// Login 用户登录
// @Summary 用户登录
// @Description 使用已注册的中国大陆手机号或邮箱登录
// @Tags 认证
// @Accept json
// @Produce json
// @Param request body LoginRequest true "登录参数"
// @Success 200 {object} LoginResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 403 {object} response.ErrorEnvelope
// @Failure 429 {object} response.ErrorEnvelope
// @Failure 500 {object} response.ErrorEnvelope
// @Router /api/app/auth/login [post]
func (h *Handler) Login(c *gin.Context) {
	var request LoginRequest
	if err := c.ShouldBindJSON(&request); err != nil {
		response.Error(c, http.StatusBadRequest, response.CodeInvalidRequest, validation.Message(err))
		return
	}

	data, err := h.service.Login(request)
	if err == nil {
		response.Success(c, data)
		return
	}

	switch {
	case errors.Is(err, ErrInvalidCredentials):
		response.Error(c, http.StatusUnauthorized, response.CodeUnauthorized, "账户或密码错误")
	case errors.Is(err, ErrUserDisabled):
		response.Error(c, http.StatusForbidden, response.CodeUserDisabled, "账号已禁用")
	default:
		slog.Error("login user", "requestId", c.GetString("requestId"), "error", err)
		response.Error(c, http.StatusInternalServerError, response.CodeInternalError, "服务器内部错误")
	}
}
