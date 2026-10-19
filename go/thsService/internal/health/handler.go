package health

import (
	"context"
	"database/sql"
	"net/http"
	"time"

	"github.com/gin-gonic/gin"

	"ths-service/internal/response"
)

type Handler struct {
	db *sql.DB
}

type Data struct {
	Status string `json:"status" example:"ok"`
}

type SuccessResponse struct {
	Code      int    `json:"code" example:"0"`
	Message   string `json:"message" example:"ok"`
	Data      Data   `json:"data"`
	RequestID string `json:"requestId" example:"f5e76095d32f49199f5f7f897f5733a2"`
}

func NewHandler(db *sql.DB) *Handler {
	return &Handler{db: db}
}

// Live 服务存活检查
// @Summary 服务存活检查
// @Description 确认 HTTP 服务进程正在运行
// @Tags 后台 · 服务监控
// @Produce json
// @Success 200 {object} SuccessResponse
// 路由已停用。
func (h *Handler) Live(c *gin.Context) {
	response.Success(c, Data{Status: "ok"})
}

// Ready 服务就绪检查
// @Summary 服务就绪检查
// @Description 确认服务及 MySQL 数据库可以正常访问
// @Tags 后台 · 服务监控
// @Produce json
// @Success 200 {object} SuccessResponse
// @Failure 503 {object} response.ErrorEnvelope
// @Router /health/ready [get]
func (h *Handler) Ready(c *gin.Context) {
	ctx, cancel := context.WithTimeout(c.Request.Context(), 2*time.Second)
	defer cancel()

	if err := h.db.PingContext(ctx); err != nil {
		response.Error(c, http.StatusServiceUnavailable, response.CodeServiceNotReady, "服务尚未就绪")
		return
	}

	response.Success(c, Data{Status: "ok"})
}
