package response

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

const successCode = 0

const (
	CodeInvalidRequest   = 40001
	CodeUnauthorized     = 40101
	CodeUserDisabled     = 40301
	CodeForbidden        = 40302
	CodeAccountExists    = 40901
	CodeNotFound         = 40401
	CodeMethodNotAllowed = 40501
	CodeTooManyRequests  = 42901
	CodeInternalError    = 50001
	CodeServiceNotReady  = 50301
)

type Envelope struct {
	Code      int    `json:"code"`
	Message   string `json:"message"`
	Data      any    `json:"data"`
	RequestID string `json:"requestId"`
}

type ErrorEnvelope struct {
	Code      int    `json:"code" example:"40001"`
	Message   string `json:"message" example:"请求参数错误"`
	Data      any    `json:"data" swaggertype:"object"`
	RequestID string `json:"requestId" example:"f5e76095d32f49199f5f7f897f5733a2"`
}

func Success(c *gin.Context, data any) {
	SuccessWithStatus(c, http.StatusOK, data)
}

func SuccessWithStatus(c *gin.Context, status int, data any) {
	c.JSON(status, Envelope{
		Code:      successCode,
		Message:   "ok",
		Data:      data,
		RequestID: c.GetString("requestId"),
	})
}

func Error(c *gin.Context, status int, code int, message string) {
	c.JSON(status, Envelope{
		Code:      code,
		Message:   message,
		Data:      nil,
		RequestID: c.GetString("requestId"),
	})
}
