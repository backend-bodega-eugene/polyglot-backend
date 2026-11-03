package taxbusiness

import (
	"encoding/json"
	"errors"
	"io"
	"log/slog"
	"net/http"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"ths-service/internal/auth"
)

type YearRequest struct {
	Year json.RawMessage `json:"year" swaggertype:"string" example:"2024"`
}
type ApplyRequest struct {
	Date json.RawMessage `json:"date" swaggertype:"string" example:"2024"`
}
type CalculateRequest struct {
	Date string `json:"date" example:"2024-08"`
	Type string `json:"type" example:"正常工资薪金"`
}
type ScanRequest struct {
	YZM string `json:"yzm"`
}
type MessageRequest struct {
	Page  int `json:"page" example:"1"`
	Limit int `json:"limit" example:"10"`
}
type EmptyRequest struct{}
type AppResponse struct {
	Code int    `json:"code" example:"1"`
	Msg  string `json:"msg"`
	Data any    `json:"data"`
}
type AnnualResponse struct {
	Code int    `json:"code" example:"1"`
	Msg  string `json:"msg"`
	Data Annual `json:"data"`
}
type MessagesResponse struct {
	Code  int              `json:"code" example:"1"`
	Msg   string           `json:"msg"`
	Data  []map[string]any `json:"data"`
	Count int              `json:"count"`
}
type AppHandler struct{ service *Service }

func NewHandler(s *Service) *AppHandler { return &AppHandler{s} }
func decode(c *gin.Context, v any) bool {
	c.Header("Cache-Control", "no-store")
	c.Request.Body = http.MaxBytesReader(c.Writer, c.Request.Body, 8192)
	d := json.NewDecoder(c.Request.Body)
	d.DisallowUnknownFields()
	if !strings.HasPrefix(c.GetHeader("Content-Type"), "application/json") || d.Decode(v) != nil || d.Decode(new(any)) != io.EOF {
		appFail(c, invalid("请提交有效JSON对象"))
		return false
	}
	return true
}
func yearValue(raw json.RawMessage) (int, error) {
	var s string
	if len(raw) > 0 && raw[0] == '"' {
		if json.Unmarshal(raw, &s) != nil {
			return 0, invalid("年度无效")
		}
	} else {
		s = string(raw)
	}
	n, e := strconv.Atoi(s)
	if e != nil || !ValidYear(n) {
		return 0, invalid("年度须为2019年至当前年份，可传数字或字符串")
	}
	return n, nil
}
func appFail(c *gin.Context, e error) {
	status := 200
	msg := e.Error()
	if !errors.Is(e, ErrValidation) && !errors.Is(e, gorm.ErrRecordNotFound) {
		status = 500
		msg = "服务器内部错误"
		slog.Error("tax business", "requestId", c.GetString("requestId"), "error", e)
	} else if errors.Is(e, gorm.ErrRecordNotFound) {
		msg = "记录不存在"
	}
	c.JSON(status, AppResponse{0, msg, gin.H{}})
}

// TaxYear 年度明细
// @Summary 查询本人年度收入纳税明细，缺失生成保存
// @Description year支持数字或字符串，2019至当前年份。历史年份生成12个月，当前年份生成截至当前月；已有记录不补齐。金额为字符串，全年合计由数据库月度记录汇总。全部数据仅模拟展示。
// @Tags App端 · 所得税业务
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param body body YearRequest true "纳税年度"
// @Success 200 {object} AnnualContract
// @Router /v1/tax-user/taxYear/list [post]
func (h *AppHandler) TaxYear(c *gin.Context) {
	var req YearRequest
	if !decode(c, &req) {
		return
	}
	year, e := yearValue(req.Year)
	if e != nil {
		appFail(c, e)
		return
	}
	data, e := h.service.Annual(c.Request.Context(), c.GetUint64(auth.UserIDContextKey), year)
	if e != nil {
		appFail(c, e)
		return
	}
	c.JSON(200, AnnualResponse{1, "success", data})
}

// ApplyInfo 年度汇算
// @Summary 查询本人年度汇算申报
// @Description date为年份，数字或字符串。月度明细和汇算状态缺失分别生成保存。total_income/total_tax根据月度记录汇总，refund_amount/status由后台维护。三个金额按文档为数字，不执行真实税务申报或计算。
// @Tags App端 · 所得税业务
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param body body ApplyRequest true "年份date"
// @Success 200 {object} ApplicationResponse
// @Router /v1/tax-user/applyInfo [post]
func (h *AppHandler) ApplyInfo(c *gin.Context) {
	var req ApplyRequest
	if !decode(c, &req) {
		return
	}
	year, e := yearValue(req.Date)
	if e != nil {
		appFail(c, e)
		return
	}
	data, e := h.service.Application(c.Request.Context(), c.GetUint64(auth.UserIDContextKey), year)
	if e != nil {
		appFail(c, e)
		return
	}
	c.JSON(200, AppResponse{1, "success", data})
}

// OpenList 开具记录
// @Summary 查询本人模拟纳税记录开具列表
// @Description 无记录生成2条，有记录读库。create_time为YYYY-MM-DD HH:mm:ss字符串（按本项目约定，不是Unix时间戳），images_data初始为空，后台可填写URL，暂不生成任何图片。验证码只在后台维护，不在列表公开。
// @Tags App端 · 所得税业务
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param body body EmptyRequest true "空对象"
// @Success 200 {object} OpenRecordsResponse
// @Router /v1/tax-user/openRecord/list [post]
func (h *AppHandler) OpenList(c *gin.Context) {
	var req EmptyRequest
	if !decode(c, &req) {
		return
	}
	data, e := h.service.OpenRecords(c.Request.Context(), c.GetUint64(auth.UserIDContextKey))
	if e != nil {
		appFail(c, e)
		return
	}
	c.JSON(200, AppResponse{1, "success", data})
}

// OpenInfo 扫码记录
// @Summary 用验证码查询本人的开具记录图片地址
// @Description yzm取二维码-=后部分，与后台开具记录验证码一致。仅查询本人，缺失集合先生成模拟记录，验证码无匹配code=0；正确验证码返回images_data，可为空。不生成图片或二维码。
// @Tags App端 · 所得税业务
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param body body ScanRequest true "扫码验证码"
// @Success 200 {object} ScanResponse
// @Router /v1/tax-user/openRecord/info [post]
func (h *AppHandler) OpenInfo(c *gin.Context) {
	var req ScanRequest
	if !decode(c, &req) {
		return
	}
	req.YZM = strings.TrimSpace(req.YZM)
	if req.YZM == "" || len(req.YZM) > 255 {
		appFail(c, invalid("缺少验证码或验证码过长"))
		return
	}
	data, e := h.service.Scan(c.Request.Context(), c.GetUint64(auth.UserIDContextKey), req.YZM)
	if e != nil {
		appFail(c, e)
		return
	}
	c.JSON(200, AppResponse{1, "success", data})
}

// Messages 消息列表
// @Summary 分页查询本人模拟税务消息
// @Description 无记录生成15条并保存，有记录读库。page从1开始，limit为1至100，count是消息总条数。条目返回id/title/content/create_time。
// @Tags App端 · 所得税业务
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param body body MessageRequest true "分页参数"
// @Success 200 {object} MessagesContract
// @Router /v1/tax-user/taxMsg/list [post]
func (h *AppHandler) Messages(c *gin.Context) {
	var req MessageRequest
	if !decode(c, &req) {
		return
	}
	if req.Page < 1 || req.Page > 1000000 || req.Limit < 1 || req.Limit > 100 {
		appFail(c, invalid("分页参数无效"))
		return
	}
	rows, e := h.service.Ensure(c.Request.Context(), "messages", c.GetUint64(auth.UserIDContextKey), 0)
	if e != nil {
		appFail(c, e)
		return
	}
	total := len(rows)
	start := (req.Page - 1) * req.Limit
	if start > total {
		start = total
	}
	end := start + req.Limit
	if end > total {
		end = total
	}
	data := []map[string]any{}
	for _, r := range rows[start:end] {
		p := payload(r)
		p["id"] = r.ID
		data = append(data, p)
	}
	c.JSON(200, MessagesResponse{1, "success", data, total})
}

// Calculate 计算明细
// @Summary 查询本人指定月份及所得小类的模拟计算明细
// @Description date为YYYY-MM，type默认正常工资薪金。该年份月度记录缺失时生成，已有时读库；累计值汇总至指定月份，本期税额和detail_list来自对应月度记录。后台修改月度明细及扣除字段即可同步修改此接口，不维护第二份可冲突金额。rate为模拟展示10%，非真实税务计算。
// @Tags App端 · 所得税业务
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param body body CalculateRequest true "所属期和所得小类"
// @Success 200 {object} CalculationResponse
// @Router /v1/tax-user/taxCountInfo [post]
func (h *AppHandler) Calculate(c *gin.Context) {
	var req CalculateRequest
	if !decode(c, &req) {
		return
	}
	if req.Type == "" {
		req.Type = "正常工资薪金"
	}
	if len(req.Type) > 255 {
		appFail(c, invalid("所得小类过长"))
		return
	}
	data, e := h.service.Calculation(c.Request.Context(), c.GetUint64(auth.UserIDContextKey), req.Date, req.Type, true)
	if e != nil {
		appFail(c, e)
		return
	}
	c.JSON(200, AppResponse{1, "success", data})
}
func Register(r *gin.RouterGroup, s *Service) {
	h := NewHandler(s)
	r.POST("/v1/tax-user/taxYear/list", h.TaxYear)
	r.POST("/v1/tax-user/applyInfo", h.ApplyInfo)
	r.POST("/v1/tax-user/openRecord/list", h.OpenList)
	r.POST("/v1/tax-user/openRecord/info", h.OpenInfo)
	r.POST("/v1/tax-user/taxMsg/list", h.Messages)
	r.POST("/v1/tax-user/taxCountInfo", h.Calculate)
}
