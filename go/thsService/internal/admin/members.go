package admin

import (
	"encoding/json"
	"errors"
	"io"
	"net/http"
	"strconv"
	"strings"
	"time"
	"unicode/utf8"

	"github.com/gin-gonic/gin"
	driver "github.com/go-sql-driver/mysql"
	"golang.org/x/crypto/bcrypt"
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
	"ths-service/internal/auth"
	"ths-service/internal/response"
)

type MemberFields struct {
	// 会员姓名，2至64个字符。
	RealName string `json:"realName"`
	// 登录手机号，与邮箱至少填写一项。
	Phone string `json:"phone"`
	// 登录邮箱，与手机号至少填写一项。
	Email string `json:"email"`
	// 会员状态：1正常，2停用。
	Status uint8 `json:"status"`
}
type MemberCreate struct {
	MemberFields
	Password string `json:"password"`
}
type MemberPassword struct {
	Password string `json:"password"`
}
type MemberView struct {
	ID          uint64     `json:"id"`
	RealName    string     `json:"realName"`
	Phone       *string    `json:"phone"`
	Email       *string    `json:"email"`
	Status      uint8      `json:"status"`
	LastLoginAt *time.Time `json:"lastLoginAt"`
	CreatedAt   time.Time  `json:"createdAt"`
	UpdatedAt   time.Time  `json:"updatedAt"`
}
type MemberPage struct {
	Items    []MemberView `json:"items"`
	Total    int64        `json:"total"`
	Page     int          `json:"page"`
	PageSize int          `json:"pageSize"`
}
type MemberPageResponse struct {
	Code      int        `json:"code"`
	Message   string     `json:"message"`
	Data      MemberPage `json:"data"`
	RequestID string     `json:"requestId"`
}
type MemberResponse struct {
	Code      int        `json:"code"`
	Message   string     `json:"message"`
	Data      MemberView `json:"data"`
	RequestID string     `json:"requestId"`
}
type MemberAudit struct {
	ID        uint64
	AdminID   uint64
	UserID    uint64
	Action    string
	CreatedAt time.Time
}

func (MemberAudit) TableName() string { return "admin_member_audits" }
func memberView(u auth.User) MemberView {
	return MemberView{u.ID, u.RealName, u.Phone, u.Email, u.Status, u.LastLoginAt, u.CreatedAt, u.UpdatedAt}
}
func RegisterMembers(r *gin.Engine, h *Handler) {
	deleted := r.Group("/api/admin/deleted-members", h.RequireLogin, sameOrigin, func(c *gin.Context) { c.Header("Cache-Control", "no-store") })
	deleted.GET("", h.DeletedMemberList)
	deleted.POST("/:userID/restore", h.MemberRestore)
	api := r.Group("/api/admin/members", h.RequireLogin, sameOrigin, func(c *gin.Context) { c.Header("Cache-Control", "no-store") })
	api.GET("", h.MemberList)
	api.GET("/:userID", h.MemberGet)
	api.POST("", h.MemberCreate)
	api.PUT("/:userID", h.MemberUpdate)
	api.PUT("/:userID/password", h.MemberChangePassword)
	api.DELETE("/:userID", h.MemberDelete)
}
func (h *Handler) memberFail(c *gin.Context, err error) {
	var mysqlError *driver.MySQLError
	switch {
	case errors.Is(err, gorm.ErrRecordNotFound):
		response.Error(c, 404, response.CodeNotFound, "会员不存在或已删除")
	case errors.As(err, &mysqlError) && mysqlError.Number == 1062:
		response.Error(c, 409, response.CodeAccountExists, "手机号或邮箱已被使用（含已删除会员）")
	default:
		h.fail(c, err)
	}
}
func memberBind(c *gin.Context, dst any) bool {
	c.Request.Body = http.MaxBytesReader(c.Writer, c.Request.Body, 16384)
	decoder := json.NewDecoder(c.Request.Body)
	decoder.DisallowUnknownFields()
	if !strings.HasPrefix(c.GetHeader("Content-Type"), "application/json") || decoder.Decode(dst) != nil || decoder.Decode(new(any)) != io.EOF {
		response.Error(c, 400, response.CodeInvalidRequest, "请求字段无效；普通编辑不接受密码字段")
		return false
	}
	return true
}
func validateMember(fields MemberFields) (auth.User, string) {
	user := auth.User{RealName: strings.TrimSpace(fields.RealName), Status: fields.Status}
	if n := utf8.RuneCountInString(user.RealName); n < 2 || n > 64 {
		return user, "姓名须为2至64个字符"
	}
	if user.Status != 1 && user.Status != 2 {
		return user, "会员状态无效"
	}
	if strings.TrimSpace(fields.Phone) != "" {
		phone, _, err := auth.NormalizeMemberAccount(fields.Phone)
		if err != nil || phone == nil {
			return user, "请输入有效的中国大陆手机号"
		}
		user.Phone = phone
	}
	if strings.TrimSpace(fields.Email) != "" {
		_, email, err := auth.NormalizeMemberAccount(fields.Email)
		if err != nil || email == nil {
			return user, "请输入有效邮箱"
		}
		user.Email = email
	}
	if user.Phone == nil && user.Email == nil {
		return user, "手机号和邮箱至少填写一项"
	}
	return user, ""
}
func (h *Handler) memberAudit(c *gin.Context, tx *gorm.DB, userID uint64, action string) error {
	a, _ := c.Get("administrator")
	return tx.Create(&MemberAudit{AdminID: a.(Administrator).ID, UserID: userID, Action: action}).Error
}

// MemberList 会员列表
// @Summary 分页查询会员
// @Description 管理员Cookie鉴权，默认排除逻辑删除的会员，不返回密码或哈希。
// @Tags 后台 · 会员管理
// @Produce json
// @Param q query string false "ID、姓名、手机号或邮箱"
// @Param status query int false "1正常，2停用"
// @Param page query int false "页码" default(1)
// @Param pageSize query int false "每页条数" default(20)
// @Success 200 {object} MemberPageResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 401 {object} response.ErrorEnvelope
// @Router /api/admin/members [get]
func (h *Handler) MemberList(c *gin.Context) {
	page, e1 := strconv.Atoi(c.DefaultQuery("page", "1"))
	size, e2 := strconv.Atoi(c.DefaultQuery("pageSize", "20"))
	q := strings.TrimSpace(c.Query("q"))
	status := c.Query("status")
	if e1 != nil || e2 != nil || page < 1 || page > 1000000 || size < 1 || size > 100 || len(q) > 254 || (status != "" && status != "1" && status != "2") {
		response.Error(c, 400, response.CodeInvalidRequest, "查询或分页参数无效")
		return
	}
	db := h.db.WithContext(c.Request.Context()).Model(&auth.User{})
	if q != "" {
		db = db.Where("CAST(id AS CHAR) = ? OR real_name LIKE ? OR phone LIKE ? OR email LIKE ?", q, "%"+q+"%", "%"+q+"%", "%"+q+"%")
	}
	if status != "" {
		db = db.Where("status = ?", status)
	}
	result := MemberPage{Items: []MemberView{}, Page: page, PageSize: size}
	if err := db.Count(&result.Total).Error; err != nil {
		h.memberFail(c, err)
		return
	}
	users := []auth.User{}
	if err := db.Select("id", "real_name", "phone", "email", "status", "last_login_at", "created_at", "updated_at").Order("id DESC").Offset((page - 1) * size).Limit(size).Find(&users).Error; err != nil {
		h.memberFail(c, err)
		return
	}
	for _, u := range users {
		result.Items = append(result.Items, memberView(u))
	}
	response.Success(c, result)
}

// MemberGet 会员详情
// @Summary 查询会员详情
// @Description 管理员Cookie鉴权，已删除返回404，不返回密码或哈希。
// @Tags 后台 · 会员管理
// @Produce json
// @Param userID path int true "会员ID"
// @Success 200 {object} MemberResponse
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Router /api/admin/members/{userID} [get]
func (h *Handler) MemberGet(c *gin.Context) {
	uid, ok := id(c)
	if !ok {
		return
	}
	var u auth.User
	if err := h.db.WithContext(c.Request.Context()).First(&u, uid).Error; err != nil {
		h.memberFail(c, err)
		return
	}
	response.Success(c, memberView(u))
}

// MemberCreate 新增会员
// @Summary 新增会员
// @Description 管理员Cookie鉴权；手机号、邮箱至少一项，姓名2至64字符。密码必填，至少6个字符、最多72字节，不限制字符类型，bcrypt哈希保存。状态1正常2停用。已删除会员的手机号/邮箱仍保留唯一占用。
// @Tags 后台 · 会员管理
// @Accept json
// @Produce json
// @Param body body MemberCreate true "会员资料及初始密码"
// @Success 200 {object} MemberResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 409 {object} response.ErrorEnvelope
// @Router /api/admin/members [post]
func (h *Handler) MemberCreate(c *gin.Context) {
	var req MemberCreate
	if !memberBind(c, &req) {
		return
	}
	u, msg := validateMember(req.MemberFields)
	if msg != "" {
		response.Error(c, 400, response.CodeInvalidRequest, msg)
		return
	}
	if err := auth.ValidateMemberPassword(req.Password); err != nil {
		response.Error(c, 400, response.CodeInvalidRequest, err.Error())
		return
	}
	hash, err := bcrypt.GenerateFromPassword([]byte(req.Password), bcrypt.DefaultCost)
	if err != nil {
		h.fail(c, err)
		return
	}
	u.PasswordHash = string(hash)
	err = h.db.WithContext(c.Request.Context()).Transaction(func(tx *gorm.DB) error {
		if err := tx.Create(&u).Error; err != nil {
			return err
		}
		return h.memberAudit(c, tx, u.ID, "create")
	})
	if err != nil {
		h.memberFail(c, err)
		return
	}
	response.Success(c, memberView(u))
}

// MemberUpdate 修改会员资料
// @Summary 修改会员资料，不修改密码
// @Description 管理员Cookie鉴权。仅接受姓名、手机号、邮箱、状态，提交password等未知字段返回400。密码须使用独立接口修改。
// @Tags 后台 · 会员管理
// @Accept json
// @Produce json
// @Param userID path int true "会员ID"
// @Param body body MemberFields true "会员资料，不包含密码"
// @Success 200 {object} MemberResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Failure 409 {object} response.ErrorEnvelope
// @Router /api/admin/members/{userID} [put]
func (h *Handler) MemberUpdate(c *gin.Context) {
	uid, ok := id(c)
	if !ok {
		return
	}
	var req MemberFields
	if !memberBind(c, &req) {
		return
	}
	u, msg := validateMember(req)
	if msg != "" {
		response.Error(c, 400, response.CodeInvalidRequest, msg)
		return
	}
	var saved auth.User
	err := h.db.WithContext(c.Request.Context()).Transaction(func(tx *gorm.DB) error {
		if err := tx.Clauses(clause.Locking{Strength: "UPDATE"}).First(&saved, uid).Error; err != nil {
			return err
		}
		if err := tx.Model(&saved).Updates(map[string]any{"real_name": u.RealName, "phone": u.Phone, "email": u.Email, "status": u.Status}).Error; err != nil {
			return err
		}
		if err := h.memberAudit(c, tx, uid, "update"); err != nil {
			return err
		}
		return tx.First(&saved, uid).Error
	})
	if err != nil {
		h.memberFail(c, err)
		return
	}
	response.Success(c, memberView(saved))
}

// MemberChangePassword 单独修改会员密码
// @Summary 修改会员密码
// @Description 管理员Cookie鉴权。至少6个字符且最多72字节，不限制字符类型，与新增相同，bcrypt保存；不返回密码或哈希。
// @Tags 后台 · 会员管理
// @Accept json
// @Produce json
// @Param userID path int true "会员ID"
// @Param body body MemberPassword true "新密码"
// @Success 200 {object} response.Envelope
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Router /api/admin/members/{userID}/password [put]
func (h *Handler) MemberChangePassword(c *gin.Context) {
	uid, ok := id(c)
	if !ok {
		return
	}
	var req MemberPassword
	if !memberBind(c, &req) {
		return
	}
	if err := auth.ValidateMemberPassword(req.Password); err != nil {
		response.Error(c, 400, response.CodeInvalidRequest, err.Error())
		return
	}
	hash, err := bcrypt.GenerateFromPassword([]byte(req.Password), bcrypt.DefaultCost)
	if err != nil {
		h.fail(c, err)
		return
	}
	err = h.db.WithContext(c.Request.Context()).Transaction(func(tx *gorm.DB) error {
		var u auth.User
		if err := tx.Clauses(clause.Locking{Strength: "UPDATE"}).First(&u, uid).Error; err != nil {
			return err
		}
		if err := tx.Model(&u).Update("password_hash", string(hash)).Error; err != nil {
			return err
		}
		return h.memberAudit(c, tx, uid, "change_password")
	})
	if err != nil {
		h.memberFail(c, err)
		return
	}
	response.Success(c, nil)
}

// MemberDelete 逻辑删除会员
// @Summary 逻辑删除会员
// @Description 管理员Cookie鉴权。设置deleted_at并停用；保留会员、持仓与交易数据，不物理删除。已登录会员也不能继续访问App受保护接口。恢复请使用独立的已删除会员接口。
// @Tags 后台 · 会员管理
// @Produce json
// @Param userID path int true "会员ID"
// @Success 200 {object} response.Envelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Router /api/admin/members/{userID} [delete]
func (h *Handler) MemberDelete(c *gin.Context) {
	uid, ok := id(c)
	if !ok {
		return
	}
	err := h.db.WithContext(c.Request.Context()).Transaction(func(tx *gorm.DB) error {
		var u auth.User
		if err := tx.Clauses(clause.Locking{Strength: "UPDATE"}).First(&u, uid).Error; err != nil {
			return err
		}
		if err := tx.Model(&u).Update("status", 2).Error; err != nil {
			return err
		}
		if err := tx.Delete(&u).Error; err != nil {
			return err
		}
		return h.memberAudit(c, tx, uid, "delete")
	})
	if err != nil {
		h.memberFail(c, err)
		return
	}
	response.Success(c, nil)
}
