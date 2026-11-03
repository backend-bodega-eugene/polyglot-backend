package tax

import (
	"errors"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"log/slog"
	"ths-service/internal/auth"
	_ "ths-service/internal/response"
)

type Handler struct{ service *Service }

func NewHandler(s *Service) *Handler { return &Handler{service: s} }

type ProfileResponse struct {
	Code      int    `json:"code"`
	Message   string `json:"message"`
	Data      View   `json:"data"`
	RequestID string `json:"requestId"`
}
type ListResponse struct {
	Code      int    `json:"code"`
	Message   string `json:"message"`
	Data      List   `json:"data"`
	RequestID string `json:"requestId"`
}

// Mine 获取本人模拟所得税资料
// @Summary 获取本人所得税资料，缺失时自动生成保存
// @Description POST查询，使用现有会员Bearer Token，不传用户ID，App只能读本人。成功code=1/msg/data，使用card_type、id_card、tax_number、tax_back_money等文档下划线字段。资料独立存表，与会员一对一；有记录读库，缺失生成保存。tax_back_money为2019至2023五项逗号分隔金额，其他年份展示第五项。资料不修改会员账号，删除后App再次读取重新生成。
// @Tags App端 · 所得税个人资料
// @Produce json
// @Security BearerAuth
// @Success 200 {object} AppProfileResponse
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Failure 500 {object} response.ErrorEnvelope
// @Router /api/app/tax/profile [post]
// @Router /v1/tax-user/info [post]
func (h *Handler) Mine(c *gin.Context) {
	c.Header("Cache-Control", "no-store")
	data, err := h.service.Mine(c.Request.Context(), c.GetUint64(auth.UserIDContextKey))
	if errors.Is(err, gorm.ErrRecordNotFound) {
		appFailure(c, 404, "会员不存在或已停用")
		return
	}
	if err != nil {
		slog.Error("tax profile", "requestId", c.GetString("requestId"), "error", err)
		appFailure(c, 500, "服务器内部错误")
		return
	}
	c.JSON(200, AppProfileResponse{1, "success", appProfile(data)})
}
