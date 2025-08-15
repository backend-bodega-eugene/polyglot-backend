package middleware

import (
	"eugene-go-starter/pkg/response"
	"log"
	"net/http"
	"runtime/debug"

	"github.com/bwmarrin/snowflake"
	"github.com/gin-gonic/gin"
)

func Recovery() gin.HandlerFunc {
	return func(c *gin.Context) {
		defer func() {
			if r := recover(); r != nil {
				log.Printf("panic: %v\n%s", r, debug.Stack())
				response.InternalError(c, "internal server error")
				c.AbortWithStatus(http.StatusInternalServerError)
			}
		}()
		c.Next()
	}
}
func init() {
    var err error
    trace, err = snowflake.NewNode(0) 
    if err != nil {
        panic(err)
    }
}
var trace *snowflake.Node

func TraceIDMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		traceID := trace.Generate().String()
		c.Set("traceId", traceID)
		c.Writer.Header().Set("X-Trace-Id", traceID) // 方便前后端对齐
		c.Next() //必须要调用这一步,才会继续请求过程
	}

}
