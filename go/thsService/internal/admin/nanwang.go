package admin

import (
	"encoding/json"
	"errors"
	"io"
	"net/http"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"
	"ths-service/internal/nanwang"
	"ths-service/internal/response"
)

type NanwangHandler struct {
	auth    *Handler
	service *nanwang.Service
}
type NanwangRecordResponse struct {
	Code    int            `json:"code"`
	Message string         `json:"message"`
	Data    nanwang.Record `json:"data"`
}
type NanwangListResponse struct {
	Code    int          `json:"code"`
	Message string       `json:"message"`
	Data    nanwang.List `json:"data"`
}
type NanwangSchemasResponse struct {
	Code    int              `json:"code"`
	Message string           `json:"message"`
	Data    []nanwang.Schema `json:"data"`
}

func RegisterNanwang(r *gin.Engine, h *Handler) {
	handler := &NanwangHandler{h, nanwang.NewService(h.db)}
	api := r.Group("/api/admin/nanwang", h.RequireLogin, sameOrigin, func(c *gin.Context) { c.Header("Cache-Control", "no-store") })
	api.GET("/schemas", handler.Schemas)
	api.GET("/:kind", handler.List)
	api.GET("/:kind/:recordID", handler.Get)
	api.POST("/:kind", handler.Create)
	api.PUT("/:kind/:recordID", handler.Update)
	api.DELETE("/:kind/:recordID", handler.Delete)
}
func (h *NanwangHandler) fail(c *gin.Context, err error) {
	switch {
	case errors.Is(err, nanwang.ErrValidation):
		response.Error(c, 400, response.CodeInvalidRequest, err.Error())
	case errors.Is(err, nanwang.ErrConflict):
		response.Error(c, 409, response.CodeAccountExists, err.Error())
	case nanwang.IsNotFound(err):
		response.Error(c, 404, response.CodeNotFound, "会员或南网记录不存在")
	default:
		h.auth.fail(c, err)
	}
}

// Schemas 南网业务类型
// @Summary 查询南网业务类型及表单字段
// @Tags 后台 · 南网业务
// @Produce json
// @Success 200 {object} NanwangSchemasResponse
// @Router /api/admin/nanwang/schemas [get]
func (h *NanwangHandler) Schemas(c *gin.Context) { response.Success(c, nanwang.Schemas) }

// List 南网业务列表
// @Summary 分页查询南网业务记录，不自动生成
// @Description 管理员Cookie鉴权。公共资讯和营业厅userId为0，其余按会员隔离。kind为news/recommended/bound/arrears/points/halls/messages/charging。
// @Tags 后台 · 南网业务
// @Produce json
// @Param kind path string true "业务类型"
// @Param userId query int false "会员ID"
// @Param q query string false "业务数据关键词"
// @Param page query int false "页码" default(1)
// @Param pageSize query int false "每页条数" default(20)
// @Success 200 {object} NanwangListResponse
// @Router /api/admin/nanwang/{kind} [get]
func (h *NanwangHandler) List(c *gin.Context) {
	page, e1 := strconv.Atoi(c.DefaultQuery("page", "1"))
	size, e2 := strconv.Atoi(c.DefaultQuery("pageSize", "20"))
	uid := uint64(0)
	var err error
	if c.Query("userId") != "" {
		uid, err = strconv.ParseUint(c.Query("userId"), 10, 64)
	}
	q := strings.TrimSpace(c.Query("q"))
	if e1 != nil || e2 != nil || err != nil || page < 1 || page > 1000000 || size < 1 || size > 100 || len(q) > 254 {
		response.Error(c, 400, response.CodeInvalidRequest, "分页或查询参数无效")
		return
	}
	data, err := h.service.List(c.Request.Context(), c.Param("kind"), uid, q, page, size)
	if err != nil {
		h.fail(c, err)
		return
	}
	response.Success(c, data)
}
func recordID(c *gin.Context) (uint64, bool) {
	id, e := strconv.ParseUint(c.Param("recordID"), 10, 64)
	if e != nil || id == 0 {
		response.Error(c, 400, response.CodeInvalidRequest, "记录编号无效")
		return 0, false
	}
	return id, true
}

// Get 南网记录详情
// @Summary 查询南网业务记录详情
// @Tags 后台 · 南网业务
// @Produce json
// @Param kind path string true "业务类型"
// @Param recordID path int true "记录ID"
// @Success 200 {object} NanwangRecordResponse
// @Router /api/admin/nanwang/{kind}/{recordID} [get]
func (h *NanwangHandler) Get(c *gin.Context) {
	id, ok := recordID(c)
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

// Create 新增南网记录
// @Summary 新增模拟南网业务记录
// @Description 管理员Cookie鉴权。payload字段见schemas，公共记录userId=0，会员记录须有效会员ID。charging每会员只允许一条。新建不传expectedVersion。写入及审计同事务。
// @Tags 后台 · 南网业务
// @Accept json
// @Produce json
// @Param kind path string true "业务类型"
// @Param body body nanwang.Write true "业务记录"
// @Success 200 {object} NanwangRecordResponse
// @Router /api/admin/nanwang/{kind} [post]
func (h *NanwangHandler) Create(c *gin.Context) { h.write(c, "create") }

// Update 修改南网记录
// @Summary 修改模拟南网业务记录
// @Description 需详情version作为expectedVersion，不能更换所属会员。冲突409，字段无效400。
// @Tags 后台 · 南网业务
// @Accept json
// @Produce json
// @Param kind path string true "业务类型"
// @Param recordID path int true "记录ID"
// @Param body body nanwang.Write true "完整payload及expectedVersion"
// @Success 200 {object} NanwangRecordResponse
// @Router /api/admin/nanwang/{kind}/{recordID} [put]
func (h *NanwangHandler) Update(c *gin.Context) { h.write(c, "update") }

// Delete 删除南网记录
// @Summary 删除模拟南网业务记录并保留审计
// @Description 需expectedVersion。某类数据整组删空后App再次查询自动生成，部分删除不补齐。不删除会员或用电资料。
// @Tags 后台 · 南网业务
// @Accept json
// @Produce json
// @Param kind path string true "业务类型"
// @Param recordID path int true "记录ID"
// @Param body body nanwang.Write true "仅需expectedVersion"
// @Success 200 {object} response.Envelope
// @Router /api/admin/nanwang/{kind}/{recordID} [delete]
func (h *NanwangHandler) Delete(c *gin.Context) { h.write(c, "delete") }
func (h *NanwangHandler) write(c *gin.Context, action string) {
	id := uint64(0)
	if action != "create" {
		var ok bool
		id, ok = recordID(c)
		if !ok {
			return
		}
	}
	var req nanwang.Write
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
