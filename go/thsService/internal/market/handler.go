package market

import (
	"errors"
	"log/slog"
	"net/http"

	"github.com/gin-gonic/gin"

	"ths-service/internal/response"
)

type Handler struct{ service *Service }

func NewHandler(service *Service) *Handler { return &Handler{service: service} }

// List 行情分类列表
// @Summary 获取分类行情列表
// @Description 返回指定分类下配置的行情；数据可能来自公开行情、历史缓存或模拟降级数据
// @Tags 行情
// @Produce json
// @Param category query string true "行情分类" Enums(global,a_stock,hk_stock,us_stock,etf,future,gold,convertible_bond,other)
// @Security BearerAuth
// @Success 200 {object} QuoteListResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 500 {object} response.ErrorEnvelope
// @Router /api/app/market/quotes [get]
func (h *Handler) List(c *gin.Context) {
	data, err := h.service.List(c.Request.Context(), c.Query("category"))
	if err == nil {
		response.Success(c, data)
		return
	}
	if errors.Is(err, ErrInvalidCategory) {
		response.Error(c, http.StatusBadRequest, response.CodeInvalidRequest, "行情分类无效")
		return
	}
	if errors.Is(err, ErrNoInstruments) {
		response.Error(c, http.StatusNotFound, response.CodeNotFound, "该分类暂无行情")
		return
	}
	slog.Error("list market quotes", "requestId", c.GetString("requestId"), "error", err)
	response.Error(c, http.StatusInternalServerError, response.CodeInternalError, "服务器内部错误")
}
