package response

import (
	"eugene-go-starter/internal/logger"
	"os"

	"github.com/bwmarrin/snowflake"
	"github.com/gin-gonic/gin"
)

var snowNode *snowflake.Node
var lang string
var lg *logger.Logger

func init() {
	snowNode, _ = snowflake.NewNode(1) // 可以加错误处理
	val := os.Getenv("language")
	if val == "" {
		lang = "en"
	} else {
		lang = val
	}
	lg = logger.NewLogger()
}

type Result struct {
	Code    int         `json:"code"`
	Msg     string      `json:"msg"`
	Data    interface{} `json:"data,omitempty"`
	TraceID string      `json:"traceId,omitempty"` // 链路ID（可从中间件注入）
}

func OK(c *gin.Context, data interface{}) {
	trade_id:= snowNode.Generate().String()
	c.JSON(200, Result{Code: 0, Msg: "success", Data: data, TraceID: trade_id})
	lg.Info("Response OK", "data", data, "traceId", trade_id)
}

func BadRequest(c *gin.Context, msg string) {
	trade_id:= snowNode.Generate().String()
	c.JSON(400, Result{Code: 400, Msg: msg, TraceID: trade_id})
	lg.Warn("Response bad", "data", nil, "traceId", trade_id)
}

func InternalError(c *gin.Context, msg string) {
		trade_id:= snowNode.Generate().String()
	c.JSON(500, Result{Code: 500, Msg: msg, TraceID: trade_id})
	lg.Error("Response error", "data", nil, "traceId",trade_id)
}
func SetResult(c *gin.Context, code int, msg string, data interface{}) {
	trade_id:= snowNode.Generate().String()
	c.JSON(200, Result{
		Code: code, Msg: msg, Data: data, TraceID: trade_id})
	lg.Info("Response OK", "data", data, "traceId", trade_id)

}
func SetResultSuccess(c *gin.Context, data interface{}) {
	trade_id:= snowNode.Generate().String()
	c.JSON(200, Result{Code: 0, Msg: "success", Data: data, TraceID: trade_id})
	lg.Info("Response OK", "data", data, "traceId", trade_id)

}
func SetResultFail(c *gin.Context, code int) {
	trade_id:= snowNode.Generate().String()
	c.JSON(200, Result{
		Code:    code,
		Msg:     codeMessages[lang][code],
		Data:    nil,
		TraceID: trade_id,
	})
	lg.Warn("Response fail", "data", nil, "traceId", trade_id)
}
