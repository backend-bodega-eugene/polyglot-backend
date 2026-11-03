package alipay

import (
	"errors"
	"log/slog"
	"strconv"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"ths-service/internal/auth"
)

type AppResponse struct {
	Code int    `json:"code" example:"1"`
	Msg  string `json:"msg" example:"success"`
	Data any    `json:"data"`
}
type Handler struct{ service *Service }

func NewHandler(s *Service) *Handler { return &Handler{s} }
func appError(c *gin.Context, e error) {
	status := 200
	msg := e.Error()
	if errors.Is(e, gorm.ErrRecordNotFound) {
		msg = "未查询到对应记录"
	} else if !errors.Is(e, ErrValidation) {
		status = 500
		msg = "服务器内部错误"
		slog.Error("alipay simulated data", "requestId", c.GetString("requestId"), "error", e)
	}
	c.JSON(status, AppResponse{0, msg, gin.H{}})
}
func success(c *gin.Context, data any, e error) {
	if e != nil {
		appError(c, e)
		return
	}
	c.JSON(200, AppResponse{1, "success", data})
}

// RequireMember shares the existing member token issuer and preserves this App's msg contract.
func RequireMember(db *gorm.DB, tokens *auth.TokenIssuer) gin.HandlerFunc {
	return func(c *gin.Context) {
		c.Header("Cache-Control", "no-store")
		parts := strings.Fields(c.GetHeader("Authorization"))
		if len(parts) != 2 || !strings.EqualFold(parts[0], "Bearer") {
			c.AbortWithStatusJSON(401, AppResponse{0, "请先登录", gin.H{}})
			return
		}
		uid, e := tokens.Parse(parts[1])
		if e != nil {
			c.AbortWithStatusJSON(401, AppResponse{0, "登录已过期", gin.H{}})
			return
		}
		var u auth.User
		e = db.WithContext(c.Request.Context()).Select("id", "status").First(&u, uid).Error
		if e != nil && !errors.Is(e, gorm.ErrRecordNotFound) {
			appError(c, e)
			c.Abort()
			return
		}
		if e != nil || u.Status != auth.UserStatusActive {
			c.AbortWithStatusJSON(401, AppResponse{0, "会员已停用或删除，请重新登录", gin.H{}})
			return
		}
		c.Set(auth.UserIDContextKey, uid)
		c.Next()
	}
}
func queryOne(c *gin.Context, key string) (string, error) {
	v := c.Request.URL.Query()[key]
	if len(v) != 1 || strings.TrimSpace(v[0]) == "" {
		return "", invalid(key + "须传入一个有效值")
	}
	return strings.TrimSpace(v[0]), nil
}
func queryYear(c *gin.Context) (int, error) {
	raw, e := queryOne(c, "year")
	if e != nil {
		return 0, e
	}
	n, e := strconv.Atoi(raw)
	if e != nil || len(raw) != 4 || !ValidYear(n) {
		return 0, invalid("year须为2000年至当前年份的四位年度")
	}
	return n, nil
}

// Profile 我的与账户
// @Summary 查询本人模拟账户资料，缺失时生成保存
// @Description 使用公共会员Token，不传会员ID。独立资料不会修改会员登录姓名、手机号或密码。
// @Tags App端 · 支付宝模拟业务
// @Produce json
// @Security BearerAuth
// @Success 200 {object} ProfileResponse
// @Failure 401 {object} AppResponse
// @Router /api/user/profile [get]
func (h *Handler) Profile(c *gin.Context) {
	data, e := h.service.Singleton(c.Request.Context(), "profile", c.GetUint64(auth.UserIDContextKey))
	success(c, data, e)
}

// Insurance 参保查询
// @Summary 查询本人模拟参保资料和累计缴费
// @Description 元数据缺失生成；无任何缴费记录时生成当前年度，已有记录读库。日期、累计月数、个人和单位累计缴费从已保存记录汇总，利息由后台维护。金额均为两位小数字符串。
// @Tags App端 · 支付宝模拟业务
// @Produce json
// @Security BearerAuth
// @Success 200 {object} SummaryResponse
// @Failure 401 {object} AppResponse
// @Router /api/insurance/summary [get]
func (h *Handler) Insurance(c *gin.Context) {
	data, e := h.service.Summary(c.Request.Context(), c.GetUint64(auth.UserIDContextKey))
	success(c, data, e)
}

// Records 个人缴费记录
// @Summary 按年度查询本人社保缴费记录
// @Description 该年度完全缺失时生成，历史年度12个月，当前年度截至当前月份。已有记录不补齐。列表金额=单位缴纳+个人缴纳，元数据与参保查询共用。
// @Tags App端 · 支付宝模拟业务
// @Produce json
// @Security BearerAuth
// @Param year query string true "2000至当前年份，如2025"
// @Success 200 {object} RecordsResponse
// @Failure 401 {object} AppResponse
// @Router /api/insurance/records [get]
func (h *Handler) Records(c *gin.Context) {
	year, e := queryYear(c)
	if e != nil {
		appError(c, e)
		return
	}
	data, e := h.service.Records(c.Request.Context(), c.GetUint64(auth.UserIDContextKey), year)
	success(c, data, e)
}

// Record 缴费详情
// @Summary 查询本人指定年月和单位的社保缴费详情
// @Description 列表和详情共用同一条数据库记录。该年度无任何数据则先生成，筛选无匹配返回code=0，不单独制造记录。year展示值为YYYYMM。
// @Tags App端 · 支付宝模拟业务
// @Produce json
// @Security BearerAuth
// @Param year query string true "四位年度"
// @Param month query string true "所属期YYYYMM"
// @Param company query string true "列表中的单位名称"
// @Success 200 {object} DetailResponse
// @Failure 401 {object} AppResponse
// @Router /api/insurance/record [get]
func (h *Handler) Record(c *gin.Context) {
	year, e := queryYear(c)
	if e != nil {
		appError(c, e)
		return
	}
	month, e := queryOne(c, "month")
	if e != nil {
		appError(c, e)
		return
	}
	d, e := time.Parse("200601", month)
	if e != nil || d.Format("200601") != month || d.Year() != year {
		appError(c, invalid("month须为与year一致的YYYYMM"))
		return
	}
	company, e := queryOne(c, "company")
	if e != nil || len(company) > 1020 {
		appError(c, invalid("company无效"))
		return
	}
	data, e := h.service.Detail(c.Request.Context(), c.GetUint64(auth.UserIDContextKey), year, month, company)
	success(c, data, e)
}

// LifeAccounts 生活缴费户号
// @Summary 查询本人的左右生活缴费户号
// @Description 缺失生成保存，已有读库。右侧没有户时name和accountNo均为空字符串。
// @Tags App端 · 支付宝模拟业务
// @Produce json
// @Security BearerAuth
// @Success 200 {object} AccountsResponse
// @Failure 401 {object} AppResponse
// @Router /api/life/accounts [get]
func (h *Handler) LifeAccounts(c *gin.Context) {
	data, e := h.service.Accounts(c.Request.Context(), c.GetUint64(auth.UserIDContextKey))
	success(c, data, e)
}

// Electric 电费
// @Summary 查询本人的模拟电费资料
// @Description 缺失生成保存，已有读库。金额字符串，不执行缴费，与南网模拟资料独立。
// @Tags App端 · 支付宝模拟业务
// @Produce json
// @Security BearerAuth
// @Success 200 {object} ElectricResponse
// @Failure 401 {object} AppResponse
// @Router /api/utility/electric [get]
func (h *Handler) Electric(c *gin.Context) {
	data, e := h.service.Singleton(c.Request.Context(), "electric", c.GetUint64(auth.UserIDContextKey))
	success(c, data, e)
}

// Water 水费账单
// @Summary 查询本人的模拟水费账单
// @Description 缺失生成保存，已有读库。addressLine2允许空字符串，不执行真实查询或缴费。
// @Tags App端 · 支付宝模拟业务
// @Produce json
// @Security BearerAuth
// @Success 200 {object} WaterResponse
// @Failure 401 {object} AppResponse
// @Router /api/utility/water [get]
func (h *Handler) Water(c *gin.Context) {
	data, e := h.service.Singleton(c.Request.Context(), "water", c.GetUint64(auth.UserIDContextKey))
	success(c, data, e)
}
func Register(r *gin.RouterGroup, s *Service) {
	h := NewHandler(s)
	r.GET("/api/user/profile", h.Profile)
	r.GET("/api/insurance/summary", h.Insurance)
	r.GET("/api/insurance/records", h.Records)
	r.GET("/api/insurance/record", h.Record)
	r.GET("/api/life/accounts", h.LifeAccounts)
	r.GET("/api/utility/electric", h.Electric)
	r.GET("/api/utility/water", h.Water)
}
