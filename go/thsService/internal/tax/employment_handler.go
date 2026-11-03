package tax

import (
	"errors"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"log/slog"
	"ths-service/internal/auth"
	_ "ths-service/internal/response"
)

type MyEmploymentResponse struct {
	Code      int              `json:"code"`
	Message   string           `json:"message"`
	Data      []EmploymentView `json:"data"`
	RequestID string           `json:"requestId"`
}
type EmploymentResponse struct {
	Code      int            `json:"code"`
	Message   string         `json:"message"`
	Data      EmploymentView `json:"data"`
	RequestID string         `json:"requestId"`
}
type EmploymentListResponse struct {
	Code      int            `json:"code"`
	Message   string         `json:"message"`
	Data      EmploymentList `json:"data"`
	RequestID string         `json:"requestId"`
}

// MyEmployment 本人任职受雇单位
// @Summary 获取本人任职受雇单位，缺失时生成保存
// @Description POST查询，沿用会员Bearer Token，不传用户ID，仅本人可读。成功code=1/msg/data；每会员一条记录，data为单元素数组，字段company_name、tax_id、company_num、post、start_date、end_date。有记录读库，缺失生成保存；end_date为空表示在职，日期YYYY-MM-DD。删除后App再次查询重新生成。
// @Tags App端 · 所得税任职单位
// @Produce json
// @Security BearerAuth
// @Success 200 {object} AppEmploymentResponse
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Failure 500 {object} response.ErrorEnvelope
// @Router /api/app/tax/companies [post]
// @Router /v1/tax-user/company/list [post]
func (h *Handler) MyEmployment(c *gin.Context) {
	c.Header("Cache-Control", "no-store")
	data, err := h.service.MyEmployment(c.Request.Context(), c.GetUint64(auth.UserIDContextKey))
	if errors.Is(err, gorm.ErrRecordNotFound) {
		appFailure(c, 404, "会员不存在或已停用")
		return
	}
	if err != nil {
		slog.Error("tax employment", "requestId", c.GetString("requestId"), "error", err)
		appFailure(c, 500, "服务器内部错误")
		return
	}
	c.JSON(200, AppEmploymentResponse{1, "success", appEmployments(data)})
}
