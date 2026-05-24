package user

import (
	"net/http"

	"Myservice/service"

	"github.com/gin-gonic/gin"
)

type CreateUserRequest struct {
	Name string `json:"name"`
}

func CreateUser(c *gin.Context) {
	var req CreateUserRequest

	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	user := service.CreateUser(req.Name)
	c.JSON(http.StatusOK, user)
}

func GetUsers(c *gin.Context) {
	users := service.GetUsers()
	c.JSON(http.StatusOK, users)
}

func GetUser(c *gin.Context) {
	id := c.Param("id")
	user, ok := service.GetUser(id)
	if !ok {
		c.JSON(http.StatusNotFound, gin.H{"error": "not found this is user  opeim"})
		return
	}
	c.JSON(http.StatusOK, user)
}

func DeleteUser(c *gin.Context) {
	id := c.Param("id")
	service.DeleteUser(id)
	c.JSON(http.StatusOK, gin.H{"message": "deleted"})
}
