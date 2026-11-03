package electricity

import (
	"errors"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"log/slog"
	"ths-service/internal/auth"
	"ths-service/internal/response"
)

type BillsResponse struct {
	Code      int        `json:"code"`
	Message   string     `json:"message"`
	Data      []BillView `json:"data"`
	RequestID string     `json:"requestId"`
}
type BillResponse struct {
	Code      int      `json:"code"`
	Message   string   `json:"message"`
	Data      BillView `json:"data"`
	RequestID string   `json:"requestId"`
}
type BillListResponse struct {
	Code      int      `json:"code"`
	Message   string   `json:"message"`
	Data      BillList `json:"data"`
	RequestID string   `json:"requestId"`
}

// MyBills 获取本人模拟电费账单
// @Summary 获取本人模拟电费账单，缺失时生成20条
// @Description 沿用会员Bearer Token，不传用户ID，仅返回本人。没有记录时生成最近20个已结束月份账单并保存，有记录直接返回数据库结果，账期倒序，不补齐。code/bill_month为YYYYMM，amount和total_usage为JSON数字，detail为对象。billing包含三档电量、单价、金额和缴费/违约金字段，数字为十进制字符串，日期YYYY-MM-DD；历史缺失日期为空，tiers为空数组，不自动重算历史金额。手动金额按保存值返回，不校验合计。删除全部后下次查询重新生成。不请求外部服务，不涉及缴费。
// @Tags App端 · 南网资料与账单
// @Produce json
// @Security BearerAuth
// @Success 200 {object} AppBillsResponse
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Failure 500 {object} response.ErrorEnvelope
// @Router /api/app/electricity/bills [get]
func (h *Handler) MyBills(c *gin.Context) {
	c.Header("Cache-Control", "no-store")
	data, err := h.service.MyBills(c.Request.Context(), c.GetUint64(auth.UserIDContextKey))
	if errors.Is(err, gorm.ErrRecordNotFound) {
		response.Error(c, 404, response.CodeNotFound, "会员不存在或已停用")
		return
	}
	if err != nil {
		slog.Error("electricity bills", "requestId", c.GetString("requestId"), "error", err)
		response.Error(c, 500, response.CodeInternalError, "服务器内部错误")
		return
	}
	c.JSON(200, AppBillsResponse{0, "success", appBills(data)})
}
