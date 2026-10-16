package index

import (
	"github.com/gin-gonic/gin"

	"ths-service/internal/response"
)

type Data struct {
	Message string `json:"message" example:"成功访问接口"`
}

type SuccessResponse struct {
	Code      int    `json:"code" example:"0"`
	Message   string `json:"message" example:"ok"`
	Data      Data   `json:"data"`
	RequestID string `json:"requestId" example:"f5e76095d32f49199f5f7f897f5733a2"`
}

// Handler 默认入口
// @Summary 访问默认接口
// @Description 验证 API 服务已正常启动
// @Tags 基础
// @Produce json
// @Success 200 {object} SuccessResponse
// 路由已停用。
func Handler(c *gin.Context) {
	response.Success(c, Data{Message: "成功访问接口"})
}
