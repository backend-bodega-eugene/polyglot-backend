package electricity

import (
	"errors"
	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"log/slog"
	"ths-service/internal/auth"
	"ths-service/internal/response"
)

type Handler struct{ service *Service }

func NewHandler(service *Service) *Handler { return &Handler{service: service} }

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

// Mine 获取我的模拟用电资料
// @Summary 获取我的模拟用电资料，缺失时生成并保存
// @Description 沿用公共会员Bearer Token，从Token识别本人，不传用户ID。成功code=0/msg/data；返回id、username、display_name、address、user_id_number、supplier、meter_reader、meter_no、postal_code。id为会员ID，username取登录手机号或邮箱，其余为独立用电资料。与会员一对一，有记录读库，缺失生成保存；删除后App再次读取重建。不提供App修改接口。
// @Tags App端 · 南网资料与账单
// @Produce json
// @Security BearerAuth
// @Success 200 {object} AppProfileResponse
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Failure 500 {object} response.ErrorEnvelope
// @Router /api/app/electricity/profile [get]
func (h *Handler) Mine(c *gin.Context) {
	c.Header("Cache-Control", "no-store")
	data, err := h.service.Mine(c.Request.Context(), c.GetUint64(auth.UserIDContextKey))
	if errors.Is(err, gorm.ErrRecordNotFound) {
		response.Error(c, 404, response.CodeNotFound, "会员不存在或已停用")
		return
	}
	if err != nil {
		slog.Error("electricity profile", "requestId", c.GetString("requestId"), "error", err)
		response.Error(c, 500, response.CodeInternalError, "服务器内部错误")
		return
	}
	c.JSON(200, AppProfileResponse{0, "success", appProfile(data)})
}
