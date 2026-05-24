package router

import (
	"Myservice/handler/user"

	"github.com/gin-gonic/gin"
)

func SetupRouter() *gin.Engine {
	r := gin.Default()

	userGroup := r.Group("/users")
	{
		userGroup.POST("", user.CreateUser)
		userGroup.GET("", user.GetUsers)
		userGroup.GET("/:id", user.GetUser)
		userGroup.DELETE("/:id", user.DeleteUser)
	}
	openIm := r.Group("/openim")
	{
		openIm.GET("/xxx/:id", user.GetUser)
	}
	return r
}
