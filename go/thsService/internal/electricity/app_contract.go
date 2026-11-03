package electricity

import (
	"encoding/json"
	"errors"
	"log/slog"
	"regexp"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"ths-service/internal/auth"
	"ths-service/internal/response"
)

// AppProfile matches the client contract independently of the administration API.
type AppProfile struct {
	ID           uint64 `json:"id"`
	Username     string `json:"username"`
	DisplayName  string `json:"display_name"`
	Address      string `json:"address"`
	UserIDNumber string `json:"user_id_number"`
	Supplier     string `json:"supplier"`
	MeterReader  string `json:"meter_reader"`
	MeterNo      string `json:"meter_no"`
	PostalCode   string `json:"postal_code"`
}

func appProfile(v View) AppProfile {
	return AppProfile{v.ID, v.Username, v.DisplayName, v.Address, v.UserIDNumber, v.Supplier, v.MeterReader, v.MeterNo, v.PostalCode}
}

type AppProfileResponse struct {
	Code int        `json:"code"`
	Msg  string     `json:"msg"`
	Data AppProfile `json:"data"`
}
type AppLookupData struct {
	List []AppProfile `json:"list"`
}
type AppLookupResponse struct {
	Code int           `json:"code"`
	Msg  string        `json:"msg"`
	Data AppLookupData `json:"data"`
}
type AppBill struct {
	Code       string          `json:"code"`
	BillMonth  string          `json:"bill_month"`
	StartDate  string          `json:"start_date"`
	EndDate    string          `json:"end_date"`
	Amount     json.Number     `json:"amount" swaggertype:"number"`
	TotalUsage json.Number     `json:"total_usage" swaggertype:"number"`
	Detail     json.RawMessage `json:"detail" swaggertype:"object"`
	Billing    Billing         `json:"billing"`
}

func appBills(rows []BillView) []AppBill {
	result := make([]AppBill, 0, len(rows))
	for _, b := range rows {
		result = append(result, AppBill{b.Code, b.Code, b.StartDate, b.EndDate, json.Number(b.Amount), json.Number(b.TotalUsage), b.Detail, BillingForBill(b.Bill)})
	}
	return result
}

type AppBillsResponse struct {
	Code int       `json:"code"`
	Msg  string    `json:"msg"`
	Data []AppBill `json:"data"`
}

// LookupPhone 查询手机号对应的用电资料
// @Summary 按手机号查询会员及完整用电资料
// @Description keyword精确匹配会员表phone，兼容旧username参数，两者不可同时传入。正常且未删除的会员返回data.list单条完整用电资料，无匹配返回空数组；用电资料缺失时生成并保存。字段采用南网文档下划线命名，不返回密码。
// @Tags App端 · 南网资料与账单
// @Produce json
// @Security BearerAuth
// @Param keyword query string false "完整中国大陆手机号，与username二选一"
// @Param username query string false "兼容旧参数，与keyword二选一"
// @Success 200 {object} AppLookupResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 500 {object} response.ErrorEnvelope
// @Router /api/app/users [get]
func (h *Handler) LookupPhone(c *gin.Context) {
	c.Header("Cache-Control", "no-store")
	q := c.Request.URL.Query()
	values, hasKeyword := q["keyword"]
	legacy, hasUsername := q["username"]
	if !hasKeyword {
		values = legacy
	}
	if (hasKeyword && hasUsername) || len(values) != 1 || !regexp.MustCompile(`^1[3-9][0-9]{9}$`).MatchString(values[0]) {
		response.Error(c, 400, response.CodeInvalidRequest, "keyword须传入一个完整手机号，不能与username同时使用")
		return
	}
	result := AppLookupData{List: []AppProfile{}}
	var u auth.User
	err := h.service.db.WithContext(c.Request.Context()).Where("phone = ? AND status = ?", values[0], auth.UserStatusActive).Take(&u).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		c.JSON(200, AppLookupResponse{0, "success", result})
		return
	}
	if err == nil {
		var v View
		v, err = h.service.Mine(c.Request.Context(), u.ID)
		if err == nil {
			result.List = append(result.List, appProfile(v))
		}
	}
	if errors.Is(err, gorm.ErrRecordNotFound) {
		c.JSON(200, AppLookupResponse{0, "success", result})
		return
	}
	if err != nil {
		slog.Error("lookup electricity profile", "requestId", c.GetString("requestId"), "error", err)
		response.Error(c, 500, response.CodeInternalError, "服务器内部错误")
		return
	}
	c.JSON(200, AppLookupResponse{0, "success", result})
}
