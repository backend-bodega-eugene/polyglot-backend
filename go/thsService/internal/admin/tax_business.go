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
	"ths-service/internal/taxbusiness"
)

type TaxBusinessHandler struct {
	auth    *Handler
	service *taxbusiness.Service
}
type TaxBusinessRecordResponse struct {
	Code    int                `json:"code"`
	Message string             `json:"message"`
	Data    taxbusiness.Record `json:"data"`
}
type TaxBusinessListResponse struct {
	Code    int              `json:"code"`
	Message string           `json:"message"`
	Data    taxbusiness.List `json:"data"`
}
type TaxBusinessSchemasResponse struct {
	Code    int                  `json:"code"`
	Message string               `json:"message"`
	Data    []taxbusiness.Schema `json:"data"`
}

func RegisterTaxBusiness(r *gin.Engine, h *Handler) {
	handler := &TaxBusinessHandler{h, taxbusiness.NewService(h.db)}
	api := r.Group("/api/admin/tax-business", h.RequireLogin, sameOrigin, func(c *gin.Context) { c.Header("Cache-Control", "no-store") })
	api.GET("/schemas", handler.Schemas)
	api.GET("/calculation", handler.Calculation)
	api.GET("/:kind", handler.List)
	api.GET("/:kind/:recordID", handler.Get)
	api.POST("/:kind", handler.Create)
	api.PUT("/:kind/:recordID", handler.Update)
	api.DELETE("/:kind/:recordID", handler.Delete)
}
func (h *TaxBusinessHandler) fail(c *gin.Context, err error) {
	switch {
	case errors.Is(err, taxbusiness.ErrValidation):
		response.Error(c, 400, response.CodeInvalidRequest, err.Error())
	case errors.Is(err, taxbusiness.ErrConflict):
		response.Error(c, 409, response.CodeAccountExists, err.Error())
	case errors.Is(err, gorm.ErrRecordNotFound):
		response.Error(c, 404, response.CodeNotFound, "会员或所得税业务记录不存在")
	default:
		h.auth.fail(c, err)
	}
}

// Schemas 所得税业务类型
// @Summary 查询所得税业务类型及表单字段
// @Tags 后台 · 所得税业务
// @Produce json
// @Success 200 {object} TaxBusinessSchemasResponse
// @Router /api/admin/tax-business/schemas [get]
func (h *TaxBusinessHandler) Schemas(c *gin.Context) { response.Success(c, taxbusiness.Schemas) }

// List 所得税业务列表
// @Summary 分页查询所得税业务记录，不自动生成
// @Description 管理员Cookie鉴权。支持会员ID、年度和关键词查询，不生成记录。kind为monthly/applications/openrecords/messages，全部按会员保存。
// @Tags 后台 · 所得税业务
// @Produce json
// @Param kind path string true "业务类型"
// @Param userId query int false "会员ID"
// @Param year query int false "年度"
// @Param q query string false "业务数据关键词"
// @Param page query int false "页码" default(1)
// @Param pageSize query int false "每页条数" default(20)
// @Success 200 {object} TaxBusinessListResponse
// @Router /api/admin/tax-business/{kind} [get]
func (h *TaxBusinessHandler) List(c *gin.Context) {
	page, e1 := strconv.Atoi(c.DefaultQuery("page", "1"))
	size, e2 := strconv.Atoi(c.DefaultQuery("pageSize", "20"))
	uid := uint64(0)
	var err error
	if c.Query("userId") != "" {
		uid, err = strconv.ParseUint(c.Query("userId"), 10, 64)
	}
	year, yearErr := strconv.Atoi(c.DefaultQuery("year", "0"))
	q := strings.TrimSpace(c.Query("q"))
	if e1 != nil || e2 != nil || err != nil || yearErr != nil || (year != 0 && !taxbusiness.ValidYear(year)) || page < 1 || page > 1000000 || size < 1 || size > 100 || len(q) > 254 {
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
func taxBusinessRecordID(c *gin.Context) (uint64, bool) {
	id, e := strconv.ParseUint(c.Param("recordID"), 10, 64)
	if e != nil || id == 0 {
		response.Error(c, 400, response.CodeInvalidRequest, "记录编号无效")
		return 0, false
	}
	return id, true
}

// Get 所得税业务记录详情
// @Summary 查询所得税业务记录详情
// @Tags 后台 · 所得税业务
// @Produce json
// @Param kind path string true "业务类型"
// @Param recordID path int true "记录ID"
// @Success 200 {object} TaxBusinessRecordResponse
// @Router /api/admin/tax-business/{kind}/{recordID} [get]
func (h *TaxBusinessHandler) Get(c *gin.Context) {
	id, ok := taxBusinessRecordID(c)
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

// Create 新增所得税业务记录
// @Summary 新增模拟所得税业务记录
// @Description 管理员Cookie鉴权。payload字段见schemas，全部记录须有效userId，monthly/applications需要year。applications每会员每年一条，月度同所属期同所得小类唯一。新建不传expectedVersion。写入及审计同事务。
// @Tags 后台 · 所得税业务
// @Accept json
// @Produce json
// @Param kind path string true "业务类型"
// @Param body body taxbusiness.Write true "业务记录"
// @Success 200 {object} TaxBusinessRecordResponse
// @Router /api/admin/tax-business/{kind} [post]
func (h *TaxBusinessHandler) Create(c *gin.Context) { h.write(c, "create") }

// Update 修改所得税业务记录
// @Summary 修改模拟所得税业务记录
// @Description 需详情version作为expectedVersion，不能更换所属会员。冲突409，字段无效400。
// @Tags 后台 · 所得税业务
// @Accept json
// @Produce json
// @Param kind path string true "业务类型"
// @Param recordID path int true "记录ID"
// @Param body body taxbusiness.Write true "完整payload及expectedVersion"
// @Success 200 {object} TaxBusinessRecordResponse
// @Router /api/admin/tax-business/{kind}/{recordID} [put]
func (h *TaxBusinessHandler) Update(c *gin.Context) { h.write(c, "update") }

// Delete 删除所得税业务记录
// @Summary 删除模拟所得税业务记录并保留审计
// @Description 需expectedVersion。某类数据整组删空后App再次查询自动生成，部分删除不补齐。不删除会员或用电资料。
// @Tags 后台 · 所得税业务
// @Accept json
// @Produce json
// @Param kind path string true "业务类型"
// @Param recordID path int true "记录ID"
// @Param body body taxbusiness.Write true "仅需expectedVersion"
// @Success 200 {object} response.Envelope
// @Router /api/admin/tax-business/{kind}/{recordID} [delete]
func (h *TaxBusinessHandler) Delete(c *gin.Context) { h.write(c, "delete") }
func (h *TaxBusinessHandler) write(c *gin.Context, action string) {
	id := uint64(0)
	if action != "create" {
		var ok bool
		id, ok = taxBusinessRecordID(c)
		if !ok {
			return
		}
	}
	var req taxbusiness.Write
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

// Calculation 后台预览计算明细
// @Summary 从已保存月度记录预览计算明细，不自动生成
// @Description 修改monthly记录及扣除字段管理此计算结果，合计不独立保存。
// @Tags 后台 · 所得税业务
// @Produce json
// @Param userId query int true "会员ID"
// @Param date query string true "YYYY-MM"
// @Param type query string false "所得小类，默认正常工资薪金"
// @Success 200 {object} response.Envelope
// @Router /api/admin/tax-business/calculation [get]
func (h *TaxBusinessHandler) Calculation(c *gin.Context) {
	uid, e := strconv.ParseUint(c.Query("userId"), 10, 64)
	if e != nil || uid == 0 {
		response.Error(c, 400, response.CodeInvalidRequest, "会员ID无效")
		return
	}
	data, e := h.service.Calculation(c.Request.Context(), uid, c.Query("date"), c.DefaultQuery("type", "正常工资薪金"), false)
	if e != nil {
		h.fail(c, e)
		return
	}
	response.Success(c, data)
}
