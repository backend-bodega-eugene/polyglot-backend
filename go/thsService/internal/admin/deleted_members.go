package admin

import (
	"strconv"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
	"ths-service/internal/auth"
	"ths-service/internal/response"
)

type DeletedMemberView struct {
	MemberView
	DeletedAt time.Time `json:"deletedAt"`
}
type DeletedMemberPage struct {
	Items    []DeletedMemberView `json:"items"`
	Total    int64               `json:"total"`
	Page     int                 `json:"page"`
	PageSize int                 `json:"pageSize"`
}
type DeletedMemberPageResponse struct {
	Code    int               `json:"code"`
	Message string            `json:"message"`
	Data    DeletedMemberPage `json:"data"`
}

// DeletedMemberList 已删除会员
// @Summary 分页查询已逻辑删除的会员
// @Description 管理员Cookie鉴权。只查询deleted_at不为空的会员，包含删除时间，不返回密码或哈希，不提供物理删除功能。
// @Tags 后台 · 会员管理
// @Produce json
// @Param q query string false "ID、姓名、手机号或邮箱"
// @Param page query int false "页码" default(1)
// @Param pageSize query int false "每页条数" default(20)
// @Success 200 {object} DeletedMemberPageResponse
// @Router /api/admin/deleted-members [get]
func (h *Handler) DeletedMemberList(c *gin.Context) {
	page, e1 := strconv.Atoi(c.DefaultQuery("page", "1"))
	size, e2 := strconv.Atoi(c.DefaultQuery("pageSize", "20"))
	q := strings.TrimSpace(c.Query("q"))
	if e1 != nil || e2 != nil || page < 1 || page > 1000000 || size < 1 || size > 100 || len(q) > 254 {
		response.Error(c, 400, response.CodeInvalidRequest, "查询或分页参数无效")
		return
	}
	db := h.db.WithContext(c.Request.Context()).Unscoped().Model(&auth.User{}).Where("deleted_at IS NOT NULL")
	if q != "" {
		db = db.Where("(CAST(id AS CHAR) = ? OR real_name LIKE ? OR phone LIKE ? OR email LIKE ?)", q, "%"+q+"%", "%"+q+"%", "%"+q+"%")
	}
	result := DeletedMemberPage{Items: []DeletedMemberView{}, Page: page, PageSize: size}
	if err := db.Count(&result.Total).Error; err != nil {
		h.memberFail(c, err)
		return
	}
	var users []auth.User
	if err := db.Select("id", "real_name", "phone", "email", "status", "last_login_at", "created_at", "updated_at", "deleted_at").Order("deleted_at DESC, id DESC").Offset((page - 1) * size).Limit(size).Find(&users).Error; err != nil {
		h.memberFail(c, err)
		return
	}
	for _, u := range users {
		result.Items = append(result.Items, DeletedMemberView{memberView(u), u.DeletedAt.Time})
	}
	response.Success(c, result)
}

// MemberRestore 恢复会员
// @Summary 恢复已逻辑删除的会员并设为正常状态
// @Description 管理员Cookie与同源校验。仅恢复已删除会员，清空deleted_at并设置status=1；原ID、密码、账号及全部关联数据保持不变。恢复与审计同事务。不存在或已恢复返回404，不重复记录恢复操作。无需请求体。不提供物理删除。
// @Tags 后台 · 会员管理
// @Produce json
// @Param userID path int true "已删除会员ID"
// @Success 200 {object} MemberResponse
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Router /api/admin/deleted-members/{userID}/restore [post]
func (h *Handler) MemberRestore(c *gin.Context) {
	uid, ok := id(c)
	if !ok {
		return
	}
	var user auth.User
	err := h.db.WithContext(c.Request.Context()).Transaction(func(tx *gorm.DB) error {
		if err := tx.Unscoped().Clauses(clause.Locking{Strength: "UPDATE"}).Where("deleted_at IS NOT NULL").First(&user, uid).Error; err != nil {
			return err
		}
		if err := tx.Unscoped().Model(&user).Updates(map[string]any{"deleted_at": nil, "status": auth.UserStatusActive}).Error; err != nil {
			return err
		}
		if err := h.memberAudit(c, tx, uid, "restore"); err != nil {
			return err
		}
		return tx.First(&user, uid).Error
	})
	if err != nil {
		h.memberFail(c, err)
		return
	}
	response.Success(c, memberView(user))
}
