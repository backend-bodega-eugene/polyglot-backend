// handler/permission.go
package handler

import (
	"net/http"
	"strconv"

	"eugene-go-starter/internal/middleware"
	"eugene-go-starter/internal/repo"

	"github.com/gin-gonic/gin"
)

type PermissionHandler struct {
	PermRepo repo.UserMenuRepo
}

func NewPermissionHandler(p repo.UserMenuRepo) *PermissionHandler {
	return &PermissionHandler{PermRepo: p}
}

// 已有查询：返回 menuId 列表给前端回显（也可直接用你已有的 ListByUser）
func (h *PermissionHandler) ListUserMenuIDs(c *gin.Context) {
	userId := c.Query("userId")
	if userId == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "missing userId"})
		return
	}
	ums, err := h.PermRepo.FindByUserId(c.Request.Context(), userId)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error":  err.Error(), // 先直接回真实错误，方便排查
			"userId": userId,
		})
		return
	}

	ids := make([]uint64, 0, len(ums))
	for _, um := range ums {
		ids = append(ids, um.MenuId)
	}
	c.JSON(http.StatusOK, ids)
}

type saveReq struct {
	UserID  string   `json:"userId"`  // 和你 saveUserMenus 的签名保持一致（string）
	MenuIDs []uint64 `json:"menuIds"` // 前端只传叶子
}

func (h *PermissionHandler) SaveUserMenus(c *gin.Context) {
	var req saveReq
	if err := c.BindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid body"})
		return
	}
	if req.UserID == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "userId required"})
		return
	}
	//（可选）校验用户存在
	if _, err := strconv.ParseUint(req.UserID, 10, 64); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid userId"})
		return
	}
	if err := h.PermRepo.SaveUserMenus(c.Request.Context(), req.UserID, req.MenuIDs); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "save failed"})
		return
	}
	c.JSON(http.StatusOK, gin.H{"ok": true, "count": len(req.MenuIDs)})
	userid, _ := strconv.ParseUint(req.UserID, 10, 64)
	middleware.InvalidateACL(userid)
}
