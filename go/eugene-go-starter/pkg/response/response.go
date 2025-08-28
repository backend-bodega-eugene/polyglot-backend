package response

import (
	"eugene-go-starter/pkg/logger"
	"os"

	"github.com/bwmarrin/snowflake"
	"github.com/gin-gonic/gin"
)

var snowNode *snowflake.Node
var lang string
var lg *logger.Logger

func init() {
	snowNode, _ = snowflake.NewNode(1) // 可以加错误处理

	val := os.Getenv("LANGUAGE")
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

func getTraceID(c *gin.Context) string {
	traceID, exists := c.Get("traceId")
	if !exists {
		return ""
	}
	if traceStr, ok := traceID.(string); ok {
		return traceStr
	}
	return ""
}
func OK(c *gin.Context, data interface{}) {
	c.JSON(200, Result{Code: 0, Msg: "success", Data: data, TraceID: getTraceID(c)})
	lg.Info("Response OK", "data", data, "traceId", getTraceID(c))
}

func BadRequest(c *gin.Context, msg string) {

	c.JSON(400, Result{Code: 400, Msg: msg, TraceID: getTraceID(c)})
	lg.Warn("Response bad", "data", nil, "traceId", getTraceID(c))
}

func InternalErrors(c *gin.Context, msg string) {

	c.JSON(500, Result{Code: 500, Msg: msg, TraceID: getTraceID(c)})
	lg.Error("Response error", "data", nil, "traceId", getTraceID(c))
}
func SetResult(c *gin.Context, code int, msg string, data interface{}) {

	c.JSON(200, Result{
		Code: code, Msg: msg, Data: data, TraceID: getTraceID(c)})
	lg.Info("Response OK", "data", data, "traceId", getTraceID(c))

}
func SetResultSuccess(c *gin.Context, data interface{}) {

	c.JSON(200, Result{Code: 0, Msg: "success", Data: data, TraceID: getTraceID(c)})
	lg.Info("Response OK", "data", data, "traceId", getTraceID(c))

}
func SetResultFail(c *gin.Context, code int) {
	// codeMessages = make(map[string]map[int]string)
	// codeMessages["ch"] = make(map[int]string)
	c.JSON(200, Result{
		Code:    code,
		Msg:     GetMsg(code, lang),
		Data:    nil,
		TraceID: getTraceID(c),
	})
	// fmt.Printf("code=%v type=%T\n", code, code)
	// fmt.Printf("addr=%p len=%d\n", codeMessages, len(codeMessages["zh"]))
	// lg.Warn("Response fail", "data", nil, "traceId", getTraceID(c))
}
