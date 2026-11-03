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
	"ths-service/internal/response"
	"ths-service/internal/tax"
)

type TaxHandler struct {
	auth    *Handler
	service *tax.Service
}

func RegisterTax(r *gin.Engine, h *Handler) {
	RegisterEmployments(r, h)
	handler := &TaxHandler{h, tax.NewService(h.db)}
	api := r.Group("/api/admin/tax/profiles", h.RequireLogin, sameOrigin, func(c *gin.Context) { c.Header("Cache-Control", "no-store") })
	api.GET("", handler.List)
	api.GET("/:userID", handler.Get)
	api.POST("/:userID", handler.Create)
	api.PUT("/:userID", handler.Update)
	api.DELETE("/:userID", handler.Delete)
	api.POST("/:userID/generate", handler.Generate)
}
func (h *TaxHandler) fail(c *gin.Context, err error) {
	switch {
	case errors.Is(err, gorm.ErrRecordNotFound):
		response.Error(c, 404, response.CodeNotFound, "会员或所得税记录不存在")
	case errors.Is(err, tax.ErrConflict), errors.Is(err, tax.ErrEmploymentConflict):
		response.Error(c, 409, response.CodeAccountExists, err.Error())
	case errors.Is(err, tax.ErrValidation):
		response.Error(c, 400, response.CodeInvalidRequest, err.Error())
	default:
		h.auth.fail(c, err)
	}
}

// List 所得税资料列表
// @Summary 分页查询模拟所得税资料
// @Description 管理员Cookie鉴权。只读已有记录，不触发生成，排除已删除会员。
// @Tags 后台 · 所得税个人资料
// @Produce json
// @Param q query string false "会员ID、姓名、登录手机号或邮箱"
// @Param userId query int false "会员ID"
// @Param page query int false "页码" default(1)
// @Param pageSize query int false "每页条数" default(20)
// @Success 200 {object} tax.ListResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 401 {object} response.ErrorEnvelope
// @Router /api/admin/tax/profiles [get]
func (h *TaxHandler) List(c *gin.Context) {
	page, e1 := strconv.Atoi(c.DefaultQuery("page", "1"))
	size, e2 := strconv.Atoi(c.DefaultQuery("pageSize", "20"))
	uid := uint64(0)
	var err error
	if c.Query("userId") != "" {
		uid, err = strconv.ParseUint(c.Query("userId"), 10, 64)
		if uid == 0 {
			err = errors.New("invalid user")
		}
	}
	q := strings.TrimSpace(c.Query("q"))
	if e1 != nil || e2 != nil || err != nil || page < 1 || page > 1000000 || size < 1 || size > 100 || len(q) > 254 {
		response.Error(c, 400, response.CodeInvalidRequest, "分页或查询参数无效")
		return
	}
	data, err := h.service.List(c.Request.Context(), q, uid, page, size)
	if err != nil {
		h.fail(c, err)
		return
	}
	response.Success(c, data)
}

// Get 所得税资料详情
// @Summary 查询会员的所得税资料
// @Description 管理员Cookie鉴权，不存在返回404，不自动生成。
// @Tags 后台 · 所得税个人资料
// @Produce json
// @Param userID path int true "会员ID"
// @Success 200 {object} tax.ProfileResponse
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Router /api/admin/tax/profiles/{userID} [get]
func (h *TaxHandler) Get(c *gin.Context) {
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

// Create 新建所得税资料
// @Summary 手动创建模拟所得税资料
// @Description 管理员Cookie鉴权，已存在409。手机号、邮箱可空，其他业务字段必填。taxBackMoney为2019至2023共五项非负金额，逗号分隔，最多9位整数和2位小数；数据库分别用DECIMAL存储。独立资料不修改会员账号或密码。证件、银行卡仅作模拟展示字符串，不校验真实证件或银行账户。
// @Tags 后台 · 所得税个人资料
// @Accept json
// @Produce json
// @Param userID path int true "会员ID"
// @Param body body tax.WriteRequest true "所得税资料，新建不传expectedVersion"
// @Success 200 {object} tax.ProfileResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Failure 409 {object} response.ErrorEnvelope
// @Router /api/admin/tax/profiles/{userID} [post]
func (h *TaxHandler) Create(c *gin.Context) { h.write(c, "create") }

// Update 修改所得税资料
// @Summary 修改模拟所得税资料
// @Description 管理员Cookie鉴权，expectedVersion传详情version，冲突409；修改与前后快照审计同事务保存。
// @Tags 后台 · 所得税个人资料
// @Accept json
// @Produce json
// @Param userID path int true "会员ID"
// @Param body body tax.WriteRequest true "完整资料及expectedVersion"
// @Success 200 {object} tax.ProfileResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Failure 409 {object} response.ErrorEnvelope
// @Router /api/admin/tax/profiles/{userID} [put]
func (h *TaxHandler) Update(c *gin.Context) { h.write(c, "update") }

// Delete 删除所得税资料
// @Summary 删除模拟所得税资料
// @Description 管理员Cookie鉴权，需expectedVersion，只删除独立资料并保留审计与会员账号。App下次读取会重新生成。
// @Tags 后台 · 所得税个人资料
// @Accept json
// @Produce json
// @Param userID path int true "会员ID"
// @Param body body tax.WriteRequest true "仅需expectedVersion"
// @Success 200 {object} response.Envelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Failure 409 {object} response.ErrorEnvelope
// @Router /api/admin/tax/profiles/{userID} [delete]
func (h *TaxHandler) Delete(c *gin.Context) { h.write(c, "delete") }

// Generate 生成所得税资料
// @Summary 为会员生成或重新生成模拟所得税资料
// @Description 管理员Cookie鉴权，没有资料可传空对象，已有资料须expectedVersion；生成覆盖与审计同事务。只处理模拟展示资料。
// @Tags 后台 · 所得税个人资料
// @Accept json
// @Produce json
// @Param userID path int true "会员ID"
// @Param body body tax.WriteRequest true "已有记录传expectedVersion，不存在传空对象"
// @Success 200 {object} tax.ProfileResponse
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Failure 409 {object} response.ErrorEnvelope
// @Router /api/admin/tax/profiles/{userID}/generate [post]
func (h *TaxHandler) Generate(c *gin.Context) { h.write(c, "generate") }
func (h *TaxHandler) write(c *gin.Context, action string) {
	uid, ok := id(c)
	if !ok {
		return
	}
	var req tax.WriteRequest
	c.Request.Body = http.MaxBytesReader(c.Writer, c.Request.Body, 16384)
	decoder := json.NewDecoder(c.Request.Body)
	decoder.DisallowUnknownFields()
	if !strings.HasPrefix(c.GetHeader("Content-Type"), "application/json") || decoder.Decode(&req) != nil || decoder.Decode(new(any)) != io.EOF {
		response.Error(c, 400, response.CodeInvalidRequest, "请提交有效的所得税资料JSON，不接受账号密码等额外字段")
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
