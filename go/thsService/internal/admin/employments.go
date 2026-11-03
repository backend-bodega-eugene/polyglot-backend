package admin

import (
	"encoding/json"
	"errors"
	"github.com/gin-gonic/gin"
	"io"
	"net/http"
	"strconv"
	"strings"
	"ths-service/internal/response"
	"ths-service/internal/tax"
)

func RegisterEmployments(r *gin.Engine, h *Handler) {
	handler := &TaxHandler{h, tax.NewService(h.db)}
	api := r.Group("/api/admin/tax/companies", h.RequireLogin, sameOrigin, func(c *gin.Context) { c.Header("Cache-Control", "no-store") })
	api.GET("", handler.EmploymentList)
	api.GET("/:userID", handler.EmploymentGet)
	api.POST("/:userID", handler.EmploymentCreate)
	api.PUT("/:userID", handler.EmploymentUpdate)
	api.DELETE("/:userID", handler.EmploymentDelete)
	api.POST("/:userID/generate", handler.EmploymentGenerate)
}

// EmploymentList 任职受雇单位列表
// @Summary 分页查询任职受雇单位
// @Description 管理员Cookie鉴权，仅查已有记录，不生成；排除已删除会员，每位会员一条单位记录。
// @Tags 后台 · 所得税任职单位
// @Produce json
// @Param q query string false "单位、单位编号、会员ID、姓名、手机号或邮箱"
// @Param userId query int false "会员ID"
// @Param page query int false "页码" default(1)
// @Param pageSize query int false "条数" default(20)
// @Success 200 {object} tax.EmploymentListResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 401 {object} response.ErrorEnvelope
// @Router /api/admin/tax/companies [get]
func (h *TaxHandler) EmploymentList(c *gin.Context) {
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
		response.Error(c, 400, response.CodeInvalidRequest, "查询或分页参数无效")
		return
	}
	data, err := h.service.ListEmployments(c.Request.Context(), q, uid, page, size)
	if err != nil {
		h.fail(c, err)
		return
	}
	response.Success(c, data)
}

// EmploymentGet 任职受雇单位详情
// @Summary 查询会员的任职受雇单位
// @Description 管理员Cookie鉴权，记录不存在404，不自动生成。
// @Tags 后台 · 所得税任职单位
// @Produce json
// @Param userID path int true "会员ID"
// @Success 200 {object} tax.EmploymentResponse
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Router /api/admin/tax/companies/{userID} [get]
func (h *TaxHandler) EmploymentGet(c *gin.Context) {
	uid, ok := id(c)
	if !ok {
		return
	}
	data, err := h.service.GetEmployment(c.Request.Context(), uid)
	if err != nil {
		h.fail(c, err)
		return
	}
	response.Success(c, data)
}

// EmploymentCreate 新建任职受雇单位
// @Summary 手动新建会员的任职受雇单位
// @Description 管理员Cookie鉴权，会员一对一，已有记录409。companyName、taxId、companyNum、post、startDate必填，endDate可空表示在职。日期YYYY-MM-DD，离职日期不早于任职日期。单位记录独立，不修改会员或个人所得税资料。
// @Tags 后台 · 所得税任职单位
// @Accept json
// @Produce json
// @Param userID path int true "会员ID"
// @Param body body tax.EmploymentWrite true "单位字段，新建不传expectedVersion"
// @Success 200 {object} tax.EmploymentResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Failure 409 {object} response.ErrorEnvelope
// @Router /api/admin/tax/companies/{userID} [post]
func (h *TaxHandler) EmploymentCreate(c *gin.Context) { h.employmentWrite(c, "create") }

// EmploymentUpdate 修改任职受雇单位
// @Summary 修改会员的任职受雇单位
// @Description 管理员Cookie鉴权，expectedVersion传详情version，冲突409；修改与审计同事务。
// @Tags 后台 · 所得税任职单位
// @Accept json
// @Produce json
// @Param userID path int true "会员ID"
// @Param body body tax.EmploymentWrite true "完整单位字段和expectedVersion"
// @Success 200 {object} tax.EmploymentResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Failure 409 {object} response.ErrorEnvelope
// @Router /api/admin/tax/companies/{userID} [put]
func (h *TaxHandler) EmploymentUpdate(c *gin.Context) { h.employmentWrite(c, "update") }

// EmploymentDelete 删除任职受雇单位
// @Summary 删除会员的任职受雇单位
// @Description 管理员Cookie鉴权，提交expectedVersion，只删除单位记录并保留审计、会员和税务资料；App再次读取时重新生成。
// @Tags 后台 · 所得税任职单位
// @Accept json
// @Produce json
// @Param userID path int true "会员ID"
// @Param body body tax.EmploymentWrite true "仅需expectedVersion"
// @Success 200 {object} response.Envelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Failure 409 {object} response.ErrorEnvelope
// @Router /api/admin/tax/companies/{userID} [delete]
func (h *TaxHandler) EmploymentDelete(c *gin.Context) { h.employmentWrite(c, "delete") }

// EmploymentGenerate 生成任职受雇单位
// @Summary 生成或重新生成一条模拟任职受雇单位
// @Description 管理员Cookie鉴权，没有记录传空对象，已有记录需expectedVersion，原子替换并保留审计；默认生成在职模拟单位。
// @Tags 后台 · 所得税任职单位
// @Accept json
// @Produce json
// @Param userID path int true "会员ID"
// @Param body body tax.EmploymentWrite true "已有记录传expectedVersion，不存在传空对象"
// @Success 200 {object} tax.EmploymentResponse
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Failure 409 {object} response.ErrorEnvelope
// @Router /api/admin/tax/companies/{userID}/generate [post]
func (h *TaxHandler) EmploymentGenerate(c *gin.Context) { h.employmentWrite(c, "generate") }
func (h *TaxHandler) employmentWrite(c *gin.Context, action string) {
	uid, ok := id(c)
	if !ok {
		return
	}
	var req tax.EmploymentWrite
	c.Request.Body = http.MaxBytesReader(c.Writer, c.Request.Body, 16384)
	decoder := json.NewDecoder(c.Request.Body)
	decoder.DisallowUnknownFields()
	if !strings.HasPrefix(c.GetHeader("Content-Type"), "application/json") || decoder.Decode(&req) != nil || decoder.Decode(new(any)) != io.EOF {
		response.Error(c, 400, response.CodeInvalidRequest, "请提交有效的任职单位JSON，不接受会员账号等额外字段")
		return
	}
	a, _ := c.Get("administrator")
	data, err := h.service.WriteEmployment(c.Request.Context(), a.(Administrator).ID, uid, req, action)
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
