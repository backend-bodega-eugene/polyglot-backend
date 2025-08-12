package handler

import (
	"time"
	"github.com/gin-gonic/gin"
	"eugene-go-starter/pkg/response"
)

func Health(c *gin.Context) {
	response.OK(c, gin.H{
		"status":  "ok",
		"time":    time.Now().UTC().Format(time.RFC3339),
	})
}