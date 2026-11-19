package admin

import (
	"errors"
	"net/http"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"ths-service/internal/portfolio"
	"ths-service/internal/response"
)

type HoldingsHandler struct {
	auth    *Handler
	service *portfolio.Service
}
type HoldingListResponse struct {
	Code      int                        `json:"code"`
	Data      portfolio.AdminHoldingList `json:"data"`
	Message   string                     `json:"message"`
	RequestID string                     `json:"requestId"`
}
type MemberChoice struct {
	ID       uint64  `json:"id"`
	RealName string  `json:"realName"`
	Phone    *string `json:"phone"`
	Email    *string `json:"email"`
}
type MembersResponse struct {
	Code      int            `json:"code"`
	Data      []MemberChoice `json:"data"`
	Message   string         `json:"message"`
	RequestID string         `json:"requestId"`
}

func RegisterHoldings(r *gin.Engine, h *Handler, s *portfolio.Service) {
	handler := &HoldingsHandler{h, s}
	api := r.Group("/api/admin", h.RequireLogin, sameOrigin, func(c *gin.Context) { c.Header("Cache-Control", "no-store") })
	api.GET("/holding-members", handler.Members)
	api.GET("/holdings", handler.List)
	api.GET("/holdings/:userID/:category", handler.Get)
	api.POST("/holdings/:userID/:category", handler.Create)
	api.PUT("/holdings/:userID/:category", handler.Update)
	api.DELETE("/holdings/:userID/:category", handler.Delete)
	api.POST("/holdings/:userID/:category/generate", handler.Generate)
}
func (h *HoldingsHandler) fail(c *gin.Context, err error) {
	switch {
	case errors.Is(err, portfolio.ErrHoldingConflict):
		response.Error(c, 409, response.CodeAccountExists, err.Error())
	case errors.Is(err, gorm.ErrRecordNotFound):
		response.Error(c, 404, response.CodeNotFound, "会员或分类持仓不存在")
	case errors.Is(err, portfolio.ErrHoldingValidation), errors.Is(err, portfolio.ErrInvalidAssets), errors.Is(err, portfolio.ErrInsufficientQuotes):
		response.Error(c, 400, response.CodeInvalidRequest, err.Error())
	default:
		h.auth.fail(c, err)
	}
}
func id(c *gin.Context) (uint64, bool) {
	v, err := strconv.ParseUint(c.Param("userID"), 10, 64)
	if err != nil || v == 0 {
		response.Error(c, 400, response.CodeInvalidRequest, "会员ID无效")
		return 0, false
	}
	return v, true
}

// Members 查询持仓所属会员
// @Summary 查询持仓所属会员
// @Description 管理员Cookie鉴权，支持按ID、姓名、手机号、邮箱查询，最多20条。
// @Tags 后台 · 持仓管理
// @Produce json
// @Param q query string false "ID、姓名、手机号或邮箱"
// @Success 200 {object} MembersResponse
// @Failure 401 {object} response.ErrorEnvelope
// @Router /api/admin/holding-members [get]
func (h *HoldingsHandler) Members(c *gin.Context) {
	q := strings.TrimSpace(c.Query("q"))
	if len(q) > 100 {
		response.Error(c, 400, response.CodeInvalidRequest, "搜索内容过长")
		return
	}
	db := h.auth.db.WithContext(c.Request.Context()).Table("users").Select("id,real_name,phone,email").Where("deleted_at IS NULL")
	if q != "" {
		db = db.Where("CAST(id AS CHAR) = ? OR real_name LIKE ? OR phone LIKE ? OR email LIKE ?", q, "%"+q+"%", "%"+q+"%", "%"+q+"%")
	}
	members := []MemberChoice{}
	if err := db.Order("id DESC").Limit(20).Scan(&members).Error; err != nil {
		h.fail(c, err)
		return
	}
	response.Success(c, members)
}

// List 分类持仓列表
// @Summary 分页查询会员分类持仓
// @Description 管理员Cookie鉴权，仅查询已有数据，不触发自动生成。
// @Tags 后台 · 持仓管理
// @Produce json
// @Param userId query int false "会员ID"
// @Param category query string false "分类"
// @Param page query int false "页码" default(1)
// @Param pageSize query int false "每页条数" default(20)
// @Success 200 {object} HoldingListResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 401 {object} response.ErrorEnvelope
// @Router /api/admin/holdings [get]
func (h *HoldingsHandler) List(c *gin.Context) {
	page, e1 := strconv.Atoi(c.DefaultQuery("page", "1"))
	size, e2 := strconv.Atoi(c.DefaultQuery("pageSize", "20"))
	uid := uint64(0)
	var e3 error
	if c.Query("userId") != "" {
		uid, e3 = strconv.ParseUint(c.Query("userId"), 10, 64)
		if uid == 0 {
			e3 = errors.New("invalid ID")
		}
	}
	if e1 != nil || e2 != nil || e3 != nil || page < 1 || page > 1000000 || size < 1 || size > 100 {
		response.Error(c, 400, response.CodeInvalidRequest, "分页或会员ID无效")
		return
	}
	data, err := h.service.AdminList(c.Request.Context(), uid, c.Query("category"), page, size)
	if err != nil {
		h.fail(c, err)
		return
	}
	response.Success(c, data)
}

// Get 分类持仓详情
// @Summary 查询分类持仓及完整交易记录
// @Description 管理员Cookie鉴权，不存在时返回404，不自动生成。
// @Tags 后台 · 持仓管理
// @Produce json
// @Param userID path int true "会员ID"
// @Param category path string true "分类"
// @Success 200 {object} portfolio.HoldingsResponse
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Router /api/admin/holdings/{userID}/{category} [get]
func (h *HoldingsHandler) Get(c *gin.Context) {
	uid, ok := id(c)
	if !ok {
		return
	}
	data, err := h.service.AdminGet(c.Request.Context(), uid, c.Param("category"))
	if err != nil {
		h.fail(c, err)
		return
	}
	response.Success(c, data)
}

// Create 新建分类持仓
// @Summary 手动新建分类持仓
// @Description 管理员Cookie鉴权。expectedGeneration须为空。items允许0至50个持仓，每个1至500条交易；基于交易重算成本、盈亏和账户汇总，持仓数量须等于交易净数量。金额和价格使用十进制字符串；成交为原币，账户和收益为CNY。全量保存，失败回滚。
// @Tags 后台 · 持仓管理
// @Accept json
// @Produce json
// @Param userID path int true "会员ID"
// @Param category path string true "分类"
// @Param body body portfolio.AdminHoldingWrite true "完整持仓及交易"
// @Success 200 {object} portfolio.HoldingsResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Failure 409 {object} response.ErrorEnvelope
// @Router /api/admin/holdings/{userID}/{category} [post]
func (h *HoldingsHandler) Create(c *gin.Context) { h.write(c, "create") }

// Update 修改分类持仓
// @Summary 修改分类持仓、股票及交易记录
// @Description 管理员Cookie鉴权。expectedGeneration必须是详情的generation，过期返回409。items为完整替换；删除股票或交易即从items中移除。手动编辑不限制自动生成的8至12只、每只40至50条，仍校验净数量、卖超和总资产。保存及变更前后审计同一事务完成。
// @Tags 后台 · 持仓管理
// @Accept json
// @Produce json
// @Param userID path int true "会员ID"
// @Param category path string true "分类"
// @Param body body portfolio.AdminHoldingWrite true "完整持仓及交易"
// @Success 200 {object} portfolio.HoldingsResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Failure 409 {object} response.ErrorEnvelope
// @Router /api/admin/holdings/{userID}/{category} [put]
func (h *HoldingsHandler) Update(c *gin.Context) { h.write(c, "update") }

// Delete 删除分类持仓
// @Summary 删除会员当前分类的全部持仓和交易
// @Description 管理员Cookie鉴权，传expectedGeneration防止误删他人修改。删除记录审计保留；App再次读取该分类会按原规则重新生成，如需保持空仓请保存items为空数组。
// @Tags 后台 · 持仓管理
// @Accept json
// @Produce json
// @Param userID path int true "会员ID"
// @Param category path string true "分类"
// @Param body body portfolio.AdminHoldingWrite true "仅需expectedGeneration"
// @Success 200 {object} response.Envelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 409 {object} response.ErrorEnvelope
// @Router /api/admin/holdings/{userID}/{category} [delete]
func (h *HoldingsHandler) Delete(c *gin.Context) { h.write(c, "delete") }

// Generate 生成分类持仓
// @Summary 自动生成或重新生成分类持仓
// @Description 管理员Cookie鉴权，指定totalAssets。positionCount可选，默认10，范围1至50；tradesPerPosition可选，默认30，范围3至500。按指定数量从分类候选池生成，不足时失败保留旧数据。App自动生成规则不变。已有记录需expectedGeneration，替换及审计原子完成。
// @Tags 后台 · 持仓管理
// @Accept json
// @Produce json
// @Param userID path int true "会员ID"
// @Param category path string true "分类"
// @Param body body portfolio.AdminHoldingWrite true "totalAssets及expectedGeneration"
// @Success 200 {object} portfolio.HoldingsResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 409 {object} response.ErrorEnvelope
// @Router /api/admin/holdings/{userID}/{category}/generate [post]
func (h *HoldingsHandler) Generate(c *gin.Context) { h.write(c, "generate") }
func (h *HoldingsHandler) write(c *gin.Context, action string) {
	uid, ok := id(c)
	if !ok {
		return
	}
	c.Request.Body = http.MaxBytesReader(c.Writer, c.Request.Body, 8<<20)
	var req portfolio.AdminHoldingWrite
	if !strings.HasPrefix(c.GetHeader("Content-Type"), "application/json") || c.ShouldBindJSON(&req) != nil {
		response.Error(c, 400, response.CodeInvalidRequest, "请求数据无效或超过8MB")
		return
	}
	if action == "create" && req.ExpectedGeneration != "" || (action == "update" || action == "delete") && req.ExpectedGeneration == "" {
		response.Error(c, 400, response.CodeInvalidRequest, "版本参数无效，请刷新后重试")
		return
	}
	a, _ := c.Get("administrator")
	dbAction := action
	if action == "create" || action == "update" {
		dbAction = "save"
	}
	data, err := h.service.AdminWrite(c.Request.Context(), a.(Administrator).ID, uid, c.Param("category"), req, dbAction)
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
