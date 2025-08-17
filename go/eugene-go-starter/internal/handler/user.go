package handler

import (
	"github.com/gin-gonic/gin"

	"eugene-go-starter/internal/handler/dto"
	"eugene-go-starter/internal/service"
	"eugene-go-starter/pkg/response"
)

type UserHandler struct{ svc *service.UserService }

func NewUserHandler(s *service.UserService) *UserHandler { return &UserHandler{svc: s} }

// POST /login
func (h *UserHandler) Login(c *gin.Context) {
	var req dto.LoginReq
	if err := c.ShouldBindJSON(&req); err != nil {
		response.SetResultFail(c, response.CodeConflict)
		return
	}
	u, err := h.svc.Login(c, req.SiteID, req.Username, req.Password)
	switch err {
	case nil:
		response.OK(c, gin.H{"userId": u.UserID, "username": u.Username})
	case service.ErrDisabledUser:
		response.SetResultFail(c, response.CodeConflict)
	default:
		response.SetResultFail(c, response.CodeConflict)
	}
}

// POST /register  （可选：先跑通再决定要不要对外开放）
func (h *UserHandler) Register(c *gin.Context) {
	var req dto.LoginReq // 这里沿用相同字段：siteId/username/password
	if err := c.ShouldBindJSON(&req); err != nil {
		response.SetResultFail(c, response.CodeConflict)
		return
	}
	err := h.svc.Register(c, req.SiteID, req.Username, req.Password)
	if err == nil {
		response.OK(c, gin.H{"username": req.Username})
		return
	}
	if err == service.ErrUserExists {
		response.SetResultFail(c, response.CodeConflict)
		return
	}
	response.SetResultFail(c, response.CodeConflict)
}

// GET /health
func Health(c *gin.Context) { c.String(200, "ok") }
