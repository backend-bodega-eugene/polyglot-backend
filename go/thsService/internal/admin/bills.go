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

type BillsHandler struct {
	auth    *Handler
	service *electricity.Service
}

func RegisterBills(r *gin.Engine, h *Handler) {
	handler := &BillsHandler{h, electricity.NewService(h.db)}
	api := r.Group("/api/admin/electricity/bills", h.RequireLogin, sameOrigin, func(c *gin.Context) { c.Header("Cache-Control", "no-store") })
	api.GET("", handler.List)
	api.GET("/:userID/:billID", handler.Get)
	api.POST("/:userID", handler.Create)
	api.PUT("/:userID/:billID", handler.Update)
	api.DELETE("/:userID/:billID", handler.Delete)
	api.POST("/:userID/generate", handler.Generate)
}
func (h *BillsHandler) fail(c *gin.Context, err error) {
	switch {
	case errors.Is(err, gorm.ErrRecordNotFound):
		response.Error(c, 404, response.CodeNotFound, "会员或账单不存在")
	case errors.Is(err, electricity.ErrBillConflict):
		response.Error(c, 409, response.CodeAccountExists, err.Error())
	case errors.Is(err, electricity.ErrValidation):
		response.Error(c, 400, response.CodeInvalidRequest, err.Error())
	default:
		h.auth.fail(c, err)
	}
}
func billID(c *gin.Context) (uint64, bool) {
	v, err := strconv.ParseUint(c.Param("billID"), 10, 64)
	if err != nil || v == 0 {
		response.Error(c, 400, response.CodeInvalidRequest, "账单ID无效")
		return 0, false
	}
	return v, true
}

// List 电费账单列表
// @Summary 分页查询模拟电费账单
// @Description 管理员Cookie鉴权，只查询已有记录。指定userId时返回该会员全部账单的setVersion，供重新生成时防止并发覆盖；无userId时setVersion为空。
// @Tags 后台 · 南网电费账单
// @Produce json
// @Param userId query int false "会员ID"
// @Param code query string false "账期YYYYMM"
// @Param q query string false "会员ID、姓名、手机号或邮箱"
// @Param page query int false "页码" default(1)
// @Param pageSize query int false "条数" default(20)
// @Success 200 {object} electricity.BillListResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Router /api/admin/electricity/bills [get]
func (h *BillsHandler) List(c *gin.Context) {
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
	data, err := h.service.ListBills(c.Request.Context(), uid, c.Query("code"), q, page, size)
	if err != nil {
		h.fail(c, err)
		return
	}
	response.Success(c, data)
}

// Get 账单详情
// @Summary 获取会员的单条电费账单
// @Tags 后台 · 南网电费账单
// @Produce json
// @Param userID path int true "会员ID"
// @Param billID path int true "账单ID"
// @Success 200 {object} electricity.BillResponse
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Router /api/admin/electricity/bills/{userID}/{billID} [get]
func (h *BillsHandler) Get(c *gin.Context) {
	uid, ok := id(c)
	if !ok {
		return
	}
	bid, ok := billID(c)
	if !ok {
		return
	}
	data, err := h.service.GetBill(c.Request.Context(), uid, bid)
	if err != nil {
		h.fail(c, err)
		return
	}
	response.Success(c, data)
}

// Create 新增账单
// @Summary 为会员新增一条模拟电费账单
// @Description 管理员Cookie鉴权，会员一对多，同一账期只能一条，重复409。code为YYYYMM；起止日期YYYY-MM-DD；amount/totalUsage为非负十进制字符串，最多9位整数2位小数；detail可选JSON对象，最多8KB。计费信息写入detail.billing：tiers为三档电量/单价/金额，due_date缴费截止日，late_fee_start_date违约金起始日，late_fee违约金金额；完整字段见响应billing模型。各金额可独立录入，不校验合计或乘积。billMonth由code派生。
// @Tags 后台 · 南网电费账单
// @Accept json
// @Produce json
// @Param userID path int true "会员ID"
// @Param body body electricity.BillWrite true "账单字段，不传expectedVersion"
// @Success 200 {object} electricity.BillResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 409 {object} response.ErrorEnvelope
// @Router /api/admin/electricity/bills/{userID} [post]
func (h *BillsHandler) Create(c *gin.Context) { h.write(c, "create") }

// Update 修改账单
// @Summary 修改一条模拟电费账单
// @Description 管理员Cookie鉴权，expectedVersion传该账单version，过期409；修改与审计同事务。不改变会员账号或用电资料。结构化计费信息写入detail.billing，读接口返回billing对象；不校验各档金额合计、乘积、日期先后或缴费状态关系，仅检查格式及字段范围。
// @Tags 后台 · 南网电费账单
// @Accept json
// @Produce json
// @Param userID path int true "会员ID"
// @Param billID path int true "账单ID"
// @Param body body electricity.BillWrite true "完整账单字段及expectedVersion"
// @Success 200 {object} electricity.BillResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Failure 409 {object} response.ErrorEnvelope
// @Router /api/admin/electricity/bills/{userID}/{billID} [put]
func (h *BillsHandler) Update(c *gin.Context) { h.write(c, "update") }

// Delete 删除账单
// @Summary 删除一条模拟电费账单
// @Description 管理员Cookie鉴权，需该账单expectedVersion。只删除账单，保留审计；仍有记录时App不补齐，全部为空时App下次生成20条。
// @Tags 后台 · 南网电费账单
// @Accept json
// @Produce json
// @Param userID path int true "会员ID"
// @Param billID path int true "账单ID"
// @Param body body electricity.BillWrite true "仅需expectedVersion"
// @Success 200 {object} response.Envelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Failure 409 {object} response.ErrorEnvelope
// @Router /api/admin/electricity/bills/{userID}/{billID} [delete]
func (h *BillsHandler) Delete(c *gin.Context) { h.write(c, "delete") }

// Generate 生成电费账单
// @Summary 为会员生成或重新生成20条模拟电费账单
// @Description 管理员Cookie鉴权，覆盖该会员全部账单。先按userId查询列表，expectedVersion传列表setVersion；已有数据版本不符409，没有账单传空对象。生成最近20个已结束月份，按深圳居民夏季/非夏季三档电价填充计费信息和缴费日期，默认已缴清，违约金为0。模板值可手动修改，所有操作和审计同事务。
// @Tags 后台 · 南网电费账单
// @Accept json
// @Produce json
// @Param userID path int true "会员ID"
// @Param body body electricity.BillWrite true "expectedVersion为该会员列表setVersion"
// @Success 200 {object} electricity.BillsResponse
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 404 {object} response.ErrorEnvelope
// @Failure 409 {object} response.ErrorEnvelope
// @Router /api/admin/electricity/bills/{userID}/generate [post]
func (h *BillsHandler) Generate(c *gin.Context) { h.write(c, "generate") }
func (h *BillsHandler) write(c *gin.Context, action string) {
	uid, ok := id(c)
	if !ok {
		return
	}
	bid := uint64(0)
	if action == "update" || action == "delete" {
		bid, ok = billID(c)
		if !ok {
			return
		}
	}
	var req electricity.BillWrite
	c.Request.Body = http.MaxBytesReader(c.Writer, c.Request.Body, 16384)
	decoder := json.NewDecoder(c.Request.Body)
	decoder.DisallowUnknownFields()
	if !strings.HasPrefix(c.GetHeader("Content-Type"), "application/json") || decoder.Decode(&req) != nil || decoder.Decode(new(any)) != io.EOF {
		response.Error(c, 400, response.CodeInvalidRequest, "请提交有效账单JSON，字段不能超过16KB")
		return
	}
	admin, _ := c.Get("administrator")
	data, err := h.service.WriteBill(c.Request.Context(), admin.(Administrator).ID, uid, bid, req, action)
	if err != nil {
		h.fail(c, err)
		return
	}
	response.Success(c, data)
}
