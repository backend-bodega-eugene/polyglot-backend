package admin

import (
	"encoding/json"
	"errors"
	"io"
	"net/http"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"ths-service/internal/electricity"
	"ths-service/internal/response"
)

type ElectricityHandler struct {
	auth    *Handler
	service *electricity.Service
}

func RegisterElectricity(r *gin.Engine, h *Handler) {
	handler := &ElectricityHandler{h, electricity.NewService(h.db)}
	api := r.Group("/api/admin/electricity/profiles", h.RequireLogin, sameOrigin, func(c *gin.Context) { c.Header("Cache-Control", "no-store") })
	api.GET("", handler.List)
	api.GET("/:userID", handler.Get)
	api.POST("/:userID", handler.Create)
	api.PUT("/:userID", handler.Update)
	api.DELETE("/:userID", handler.Delete)
	api.POST("/:userID/generate", handler.Generate)
}
func (h *ElectricityHandler) fail(c *gin.Context, err error) {
	switch {
	case errors.Is(err, gorm.ErrRecordNotFound):
		response.Error(c, 404, response.CodeNotFound, "会员或用电资料不存在")
	case errors.Is(err, electricity.ErrConflict):
		response.Error(c, 409, response.CodeAccountExists, err.Error())
	case errors.Is(err, electricity.ErrValidation):
		response.Error(c, 400, response.CodeInvalidRequest, err.Error())
	default:
		h.auth.fail(c, err)
	}
}

// List 用电资料列表
// @Summary 分页查询会员模拟用电资料
// @Description 管理员Cookie鉴权，查询不触发生成，排除已删除会员。
// @Tags 后台 · 南网用电资料
// @Produce json
// @Param q query string false "会员ID、姓名、账号、户号或表号"
// @Param userId query int false "会员ID"
// @Param page query int false "页码" default(1)
// @Param pageSize query int false "条数" default(20)
// @Success 200 {object} electricity.ListResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 401 {object} response.ErrorEnvelope
// @Router /api/admin/electricity/profiles [get]
func (h *ElectricityHandler) List(c *gin.Context) {
	page, e1 := strconv.Atoi(c.DefaultQuery("page", "1"))
	size, e2 := strconv.Atoi(c.DefaultQuery("pageSize", "20"))
	q := strings.TrimSpace(c.Query("q"))
	uid := uint64(0)
	var err error
	if c.Query("userId") != "" {
		uid, err = strconv.ParseUint(c.Query("userId"), 10, 64)
		if uid == 0 {
			err = errors.New("invalid user")
		}
	}
	if e1 != nil || e2 != nil || err != nil || page < 1 || page > 1000000 || size < 1 || size > 100 || len(q) > 254 {
		response.Error(c, 400, response.CodeInvalidRequest, "查询或分页参数无效")
		return
	}
	data, err := h.service.List(c.Request.Context(), q, uid, page, size)
	if err != nil {
		h.fail(c, err)
		return
	}
	response.Success(c, data)
}

// Get 获取用电资料
// @Summary 获取指定会员的模拟用电资料
// @Description 管理员Cookie鉴权；不存在返回404，不自动生成。
// @Tags 后台 · 南网用电资料
// @Produce json
// @Param userID path int true "会员ID"
// @Success 200 {object} electricity.ProfileResponse
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Router /api/admin/electricity/profiles/{userID} [get]
func (h *ElectricityHandler) Get(c *gin.Context) {
	uid, ok := id(c)
	if !ok {
		return
	}
	data, err := h.service.Get(c.Request.Context(), uid)
	if err != nil {
		h.fail(c, err)
		return
	}
	response.Success(c, data)
}

// Create 新增模拟用电资料
// @Summary 为会员手动创建模拟用电资料
// @Description 管理员Cookie鉴权，独立表与会员一对一，已存在返回409。手机号/邮箱、账号密码不受影响。expectedVersion须为空。所有业务字段必填，户号11至32位数字，表号6至32位数字，邮编6位数字。接口采用camelCase字段名。
// @Tags 后台 · 南网用电资料
// @Accept json
// @Produce json
// @Param userID path int true "会员ID"
// @Param body body electricity.WriteRequest true "用电资料"
// @Success 200 {object} electricity.ProfileResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Failure 409 {object} response.ErrorEnvelope
// @Router /api/admin/electricity/profiles/{userID} [post]
func (h *ElectricityHandler) Create(c *gin.Context) { h.write(c, "create") }

// Update 修改模拟用电资料
// @Summary 修改会员的模拟用电资料
// @Description 管理员Cookie鉴权，expectedVersion传详情中的version，过期409；资料及修改前后审计同事务保存，App下次读取立即可见。
// @Tags 后台 · 南网用电资料
// @Accept json
// @Produce json
// @Param userID path int true "会员ID"
// @Param body body electricity.WriteRequest true "完整用电资料及expectedVersion"
// @Success 200 {object} electricity.ProfileResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Failure 409 {object} response.ErrorEnvelope
// @Router /api/admin/electricity/profiles/{userID} [put]
func (h *ElectricityHandler) Update(c *gin.Context) { h.write(c, "update") }

// Delete 删除模拟用电资料
// @Summary 删除会员的模拟用电资料
// @Description 管理员Cookie鉴权，需expectedVersion；仅删除独立资料，保留审计和会员账号。App再次查询会生成新的模拟资料。
// @Tags 后台 · 南网用电资料
// @Accept json
// @Produce json
// @Param userID path int true "会员ID"
// @Param body body electricity.WriteRequest true "仅需expectedVersion"
// @Success 200 {object} response.Envelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Failure 409 {object} response.ErrorEnvelope
// @Router /api/admin/electricity/profiles/{userID} [delete]
func (h *ElectricityHandler) Delete(c *gin.Context) { h.write(c, "delete") }

// Generate 生成模拟用电资料
// @Summary 为会员生成或重新生成模拟用电资料
// @Description 管理员Cookie鉴权，默认广州地区。不存在可传空对象；已有资料必须传expectedVersion，覆盖并保存新版本。只生成展示数据，不请求第三方服务。
// @Tags 后台 · 南网用电资料
// @Accept json
// @Produce json
// @Param userID path int true "会员ID"
// @Param body body electricity.WriteRequest true "已有记录传expectedVersion，不存在传空对象"
// @Success 200 {object} electricity.ProfileResponse
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Failure 409 {object} response.ErrorEnvelope
// @Router /api/admin/electricity/profiles/{userID}/generate [post]
func (h *ElectricityHandler) Generate(c *gin.Context) { h.write(c, "generate") }
func (h *ElectricityHandler) write(c *gin.Context, action string) {
	uid, ok := id(c)
	if !ok {
		return
	}
	var req electricity.WriteRequest
	c.Request.Body = http.MaxBytesReader(c.Writer, c.Request.Body, 16384)
	decoder := json.NewDecoder(c.Request.Body)
	decoder.DisallowUnknownFields()
	if !strings.HasPrefix(c.GetHeader("Content-Type"), "application/json") || decoder.Decode(&req) != nil || decoder.Decode(new(any)) != io.EOF {
		response.Error(c, 400, response.CodeInvalidRequest, "请提交有效的用电资料JSON，不接受会员账号等额外字段")
		return
	}
	a, _ := c.Get("administrator")
	data, err := h.service.Write(c.Request.Context(), a.(Administrator).ID, uid, req, action)
	if err != nil {
		h.fail(c, err)
		return
	}
	if action == "delete" {
		response.Success(c, nil)
		return
	}
	response.Success(c, data)
}
