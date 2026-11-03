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
	"ths-service/internal/alipay"
	"ths-service/internal/response"
)

type AlipayHandler struct {
	auth    *Handler
	service *alipay.Service
}
type AlipayRecordResponse struct {
	Code    int           `json:"code"`
	Message string        `json:"message"`
	Data    alipay.Record `json:"data"`
}
type AlipayListResponse struct {
	Code    int         `json:"code"`
	Message string      `json:"message"`
	Data    alipay.List `json:"data"`
}
type AlipaySchemasResponse struct {
	Code    int             `json:"code"`
	Message string          `json:"message"`
	Data    []alipay.Schema `json:"data"`
}

func RegisterAlipay(r *gin.Engine, h *Handler) {
	handler := &AlipayHandler{h, alipay.NewService(h.db)}
	api := r.Group("/api/admin/alipay", h.RequireLogin, sameOrigin, func(c *gin.Context) { c.Header("Cache-Control", "no-store") })
	api.GET("/schemas", handler.Schemas)
	api.POST("/records/generate", handler.Generate)
	api.POST("/records/generate-preview", handler.PreviewGeneration)
	api.GET("/:kind", handler.List)
	api.GET("/:kind/:recordID", handler.Get)
	api.POST("/:kind", handler.Create)
	api.PUT("/:kind/:recordID", handler.Update)
	api.DELETE("/:kind/:recordID", handler.Delete)
}

type AlipayGenerationResponse struct {
	Code    int                        `json:"code"`
	Message string                     `json:"message"`
	Data    alipay.ContributionPreview `json:"data"`
}

// Generate 批量生成五险缴费记录
// @Summary 按月份范围批量生成会员五险缴费记录
// @Description 管理员Cookie鉴权。五险逐项按基数乘费率计算并四舍五入到分，再汇总单位和个人金额。所提交规则应用整个区间，历史政策变化请分段提交。基数须按当地年度上下限确认；不含公积金，深户养老地方补充请计入单位费率。同会员已有月份一律跳过，不覆盖任何单位的记录；最多120个月，不生成未来月份。记录和审计同事务保存，App累计信息自动汇总。
// @Tags 后台 · 支付宝模拟业务
// @Accept json
// @Produce json
// @Param body body alipay.GenerateContributions true "会员、起止月份、月薪、单位和五险规则"
// @Success 200 {object} AlipayGenerationResponse
// @Router /api/admin/alipay/records/generate [post]
func (h *AlipayHandler) Generate(c *gin.Context) { h.generate(c, false) }

// PreviewGeneration 预览五险计算
// @Summary 预览五险逐项金额及待生成、跳过月份数量
// @Description 参数与批量生成相同，不写入缴费记录。并发编辑可能使实际生成数量与预览不同。
// @Tags 后台 · 支付宝模拟业务
// @Accept json
// @Produce json
// @Param body body alipay.GenerateContributions true "生成参数"
// @Success 200 {object} AlipayGenerationResponse
// @Router /api/admin/alipay/records/generate-preview [post]
func (h *AlipayHandler) PreviewGeneration(c *gin.Context) { h.generate(c, true) }
func (h *AlipayHandler) generate(c *gin.Context, preview bool) {
	var req alipay.GenerateContributions
	c.Request.Body = http.MaxBytesReader(c.Writer, c.Request.Body, 16384)
	d := json.NewDecoder(c.Request.Body)
	d.DisallowUnknownFields()
	if !strings.HasPrefix(c.GetHeader("Content-Type"), "application/json") || d.Decode(&req) != nil || d.Decode(new(any)) != io.EOF {
		response.Error(c, 400, response.CodeInvalidRequest, "请提交有效生成参数JSON")
		return
	}
	a, _ := c.Get("administrator")
	data, err := h.service.GenerateInsurance(c.Request.Context(), a.(Administrator).ID, req, preview)
	if err != nil {
		h.fail(c, err)
		return
	}
	response.Success(c, data)
}
func (h *AlipayHandler) fail(c *gin.Context, err error) {
	switch {
	case errors.Is(err, alipay.ErrValidation):
		response.Error(c, 400, response.CodeInvalidRequest, err.Error())
	case errors.Is(err, alipay.ErrConflict):
		response.Error(c, 409, response.CodeAccountExists, err.Error())
	case errors.Is(err, gorm.ErrRecordNotFound):
		response.Error(c, 404, response.CodeNotFound, "会员或支付宝模拟业务记录不存在")
	default:
		h.auth.fail(c, err)
	}
}

// Schemas 支付宝模拟业务类型
// @Summary 查询支付宝模拟业务类型及表单字段
// @Tags 后台 · 支付宝模拟业务
// @Produce json
// @Success 200 {object} AlipaySchemasResponse
// @Router /api/admin/alipay/schemas [get]
func (h *AlipayHandler) Schemas(c *gin.Context) { response.Success(c, alipay.Schemas) }

// List 支付宝模拟业务列表
// @Summary 分页查询支付宝模拟业务记录，不自动生成
// @Description 管理员Cookie鉴权。支持会员ID、年度和关键词查询，不生成记录。kind为profile/insurance/records/accounts/electric/water，全部按会员保存。
// @Tags 后台 · 支付宝模拟业务
// @Produce json
// @Param kind path string true "业务类型"
// @Param userId query int false "会员ID"
// @Param year query int false "年度"
// @Param q query string false "业务数据关键词"
// @Param page query int false "页码" default(1)
// @Param pageSize query int false "每页条数" default(20)
// @Success 200 {object} AlipayListResponse
// @Router /api/admin/alipay/{kind} [get]
func (h *AlipayHandler) List(c *gin.Context) {
	page, e1 := strconv.Atoi(c.DefaultQuery("page", "1"))
	size, e2 := strconv.Atoi(c.DefaultQuery("pageSize", "20"))
	uid := uint64(0)
	var err error
	if c.Query("userId") != "" {
		uid, err = strconv.ParseUint(c.Query("userId"), 10, 64)
	}
	year, yearErr := strconv.Atoi(c.DefaultQuery("year", "0"))
	q := strings.TrimSpace(c.Query("q"))
	if e1 != nil || e2 != nil || err != nil || yearErr != nil || (year != 0 && !alipay.ValidYear(year)) || page < 1 || page > 1000000 || size < 1 || size > 100 || len(q) > 254 {
		response.Error(c, 400, response.CodeInvalidRequest, "分页或查询参数无效")
		return
	}
	data, err := h.service.List(c.Request.Context(), c.Param("kind"), uid, year, q, page, size)
	if err != nil {
		h.fail(c, err)
		return
	}
	response.Success(c, data)
}
func alipayRecordID(c *gin.Context) (uint64, bool) {
	id, e := strconv.ParseUint(c.Param("recordID"), 10, 64)
	if e != nil || id == 0 {
		response.Error(c, 400, response.CodeInvalidRequest, "记录编号无效")
		return 0, false
	}
	return id, true
}

// Get 支付宝模拟业务记录详情
// @Summary 查询支付宝模拟业务记录详情
// @Tags 后台 · 支付宝模拟业务
// @Produce json
// @Param kind path string true "业务类型"
// @Param recordID path int true "记录ID"
// @Success 200 {object} AlipayRecordResponse
// @Router /api/admin/alipay/{kind}/{recordID} [get]
func (h *AlipayHandler) Get(c *gin.Context) {
	id, ok := alipayRecordID(c)
	if !ok {
		return
	}
	data, err := h.service.Get(c.Request.Context(), c.Param("kind"), id)
	if err != nil {
		h.fail(c, err)
		return
	}
	response.Success(c, data)
}

// Create 新增支付宝模拟业务记录
// @Summary 新增模拟支付宝模拟业务记录
// @Description 管理员Cookie鉴权。payload字段见schemas，全部记录须有效userId，records需要year，同年月同单位唯一；其他每会员一条。新建不传expectedVersion。写入及审计同事务。
// @Tags 后台 · 支付宝模拟业务
// @Accept json
// @Produce json
// @Param kind path string true "业务类型"
// @Param body body alipay.Write true "业务记录"
// @Success 200 {object} AlipayRecordResponse
// @Router /api/admin/alipay/{kind} [post]
func (h *AlipayHandler) Create(c *gin.Context) { h.write(c, "create") }

// Update 修改支付宝模拟业务记录
// @Summary 修改模拟支付宝模拟业务记录
// @Description 需详情version作为expectedVersion，不能更换所属会员。冲突409，字段无效400。
// @Tags 后台 · 支付宝模拟业务
// @Accept json
// @Produce json
// @Param kind path string true "业务类型"
// @Param recordID path int true "记录ID"
// @Param body body alipay.Write true "完整payload及expectedVersion"
// @Success 200 {object} AlipayRecordResponse
// @Router /api/admin/alipay/{kind}/{recordID} [put]
func (h *AlipayHandler) Update(c *gin.Context) { h.write(c, "update") }

// Delete 删除支付宝模拟业务记录
// @Summary 删除模拟支付宝模拟业务记录并保留审计
// @Description 需expectedVersion。某类数据整组删空后App再次查询自动生成，部分删除不补齐。不删除会员或用电资料。
// @Tags 后台 · 支付宝模拟业务
// @Accept json
// @Produce json
// @Param kind path string true "业务类型"
// @Param recordID path int true "记录ID"
// @Param body body alipay.Write true "仅需expectedVersion"
// @Success 200 {object} response.Envelope
// @Router /api/admin/alipay/{kind}/{recordID} [delete]
func (h *AlipayHandler) Delete(c *gin.Context) { h.write(c, "delete") }
func (h *AlipayHandler) write(c *gin.Context, action string) {
	id := uint64(0)
	if action != "create" {
		var ok bool
		id, ok = alipayRecordID(c)
		if !ok {
			return
		}
	}
	var req alipay.Write
	c.Request.Body = http.MaxBytesReader(c.Writer, c.Request.Body, 65536)
	d := json.NewDecoder(c.Request.Body)
	d.UseNumber()
	d.DisallowUnknownFields()
	if !strings.HasPrefix(c.GetHeader("Content-Type"), "application/json") || d.Decode(&req) != nil || d.Decode(new(any)) != io.EOF {
		response.Error(c, 400, response.CodeInvalidRequest, "请提交有效业务JSON")
		return
	}
	a, _ := c.Get("administrator")
	data, err := h.service.Write(c.Request.Context(), a.(Administrator).ID, c.Param("kind"), id, req, action)
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
