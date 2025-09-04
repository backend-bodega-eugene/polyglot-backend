package handler

import (
	"net/http"
	"strconv"

	"eugene-go-starter/internal/repo"
	"eugene-go-starter/internal/service"

	"github.com/gin-gonic/gin"
)

type UserHandler struct {
	Svc *service.UserService
}

func NewUserHandler(s *service.UserService) *UserHandler {
	return &UserHandler{Svc: s}
}

// 统一响应结构
type Resp struct {
	Code int         `json:"code"`
	Msg  string      `json:"msg"`
	Data interface{} `json:"data,omitempty"`
}

// 注册路由
func (h *UserHandler) RegisterRoutes(r *gin.RouterGroup) {
	g := r.Group("/users")
	g.GET("", h.ListUsers)         // GET /api/users?page=1&pageSize=10&keyword=xx
	g.GET("/:id", h.GetUser)       // GET /api/users/1
	g.PUT("/:id", h.UpdateUser)    // PUT /api/users/1
	g.DELETE("/:id", h.DeleteUser) // DELETE /api/users/1
	g.POST("", h.AddUser)          // POST /api/users
}

// 列表
func (h *UserHandler) ListUsers(c *gin.Context) {
	page, _ := strconv.Atoi(c.DefaultQuery("page", "1"))
	size, _ := strconv.Atoi(c.DefaultQuery("pageSize", "10"))
	kw := c.Query("keyword")

	users, err := h.Svc.ListUsers(c.Request.Context(), page, size, kw)
	if err != nil {
		h.fail(c, err)
		return
	}

	c.JSON(http.StatusOK, Resp{
		Code: 0,
		Msg:  "success",
		Data: gin.H{
			"list":  users,
			"total": len(users), // 目前仓储没返回 total，这里用 len(users) 占位
		},
	})
}

// 详情
func (h *UserHandler) GetUser(c *gin.Context) {
	id, _ := strconv.ParseUint(c.Param("id"), 10, 64)
	user, err := h.Svc.GetUser(c.Request.Context(), id)
	if err != nil {
		h.fail(c, err)
		return
	}
	c.JSON(http.StatusOK, Resp{Code: 0, Msg: "success", Data: user})
}

// 更新
func (h *UserHandler) UpdateUser(c *gin.Context) {
	id, _ := strconv.ParseUint(c.Param("id"), 10, 64)
	var in service.UpdateUserReq
	if err := c.ShouldBindJSON(&in); err != nil {
		c.JSON(http.StatusBadRequest, Resp{Code: 400, Msg: "invalid json"})
		return
	}
	in.UserID = id
	if err := h.Svc.UpdateUser(c.Request.Context(), in); err != nil {
		h.fail(c, err)
		return
	}
	c.JSON(http.StatusOK, Resp{Code: 0, Msg: "success"})
}

// 删除
func (h *UserHandler) DeleteUser(c *gin.Context) {
	id, _ := strconv.ParseUint(c.Param("id"), 10, 64)
	if err := h.Svc.DeleteUser(c.Request.Context(), id); err != nil {
		h.fail(c, err)
		return
	}
	c.JSON(http.StatusOK, Resp{Code: 0, Msg: "success"})
}

// 错误统一映射
func (h *UserHandler) fail(c *gin.Context, err error) {
	switch {
	case err == repo.ErrNotFound:
		c.JSON(http.StatusNotFound, Resp{Code: 404, Msg: "not found"})
	case err == repo.ErrConflict:
		c.JSON(http.StatusConflict, Resp{Code: 409, Msg: "conflict"})
	case err == repo.ErrInvalidPassword:
		c.JSON(http.StatusBadRequest, Resp{Code: 400, Msg: "invalid password"})
	default:
		c.JSON(http.StatusInternalServerError, Resp{Code: 500, Msg: "internal error"})
	}
}
func (h *UserHandler) AddUser(c *gin.Context) {
	var in service.CreateUserReq
	if err := c.ShouldBindJSON(&in); err != nil {
		c.JSON(http.StatusBadRequest, Resp{Code: 400, Msg: "invalid json"})
		return
	}
	if err := h.Svc.AddUser(c.Request.Context(), in); err != nil {
		h.fail(c, err)
		return
	}
	c.JSON(http.StatusOK, Resp{Code: 0, Msg: "success"})
}
