package response

import "github.com/gin-gonic/gin"

type Result struct {
	Code int         `json:"code"`
	Msg  string      `json:"msg"`
	Data interface{} `json:"data,omitempty"`
}

func OK(c *gin.Context, data interface{}) {
	c.JSON(200, Result{Code:0, Msg:"success", Data:data})
}

func BadRequest(c *gin.Context, msg string) {
	c.JSON(400, Result{Code:400, Msg: msg})
}

func InternalError(c *gin.Context, msg string) {
	c.JSON(500, Result{Code:500, Msg: msg})
}