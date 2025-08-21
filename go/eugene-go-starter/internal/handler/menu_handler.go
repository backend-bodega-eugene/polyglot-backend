// handler/menu_handler.go
package handler

import (
	"eugene-go-starter/internal/model"
	"eugene-go-starter/internal/repo"
	"eugene-go-starter/internal/service"
	"net/http"

	"github.com/gin-gonic/gin"
)

type MenuHandler struct{ Repo repo.MenuRepo }

func (h *MenuHandler) GetMyMenus(c *gin.Context) {
	// u := c.MustGet("user").(struct{
	// 	UserID int64
	// 	Username string
	// })
	uid, _ := c.Get("uid")
	uname, _ := c.Get("uname")
	var items []model.Menu
	var err error

	if uname == "eugene" { // 超管
		items, err = h.Repo.ListAll(c)
	} else {
		items, err = h.Repo.ListByUser(c, uid.(int64))
	}
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"message": err.Error()})
		return
	}
	c.JSON(http.StatusOK, service.BuildMenuTree(items))
}
