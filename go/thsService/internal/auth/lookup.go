package auth

import (
	"context"
	"errors"
	"log/slog"
	"strings"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"ths-service/internal/response"
)

type LookupUser struct {
	ID          uint64 `json:"id"`
	Username    string `json:"username"`
	DisplayName string `json:"displayName"`
}
type LookupData struct {
	List []LookupUser `json:"list"`
}
type LookupResponse struct {
	Code      int        `json:"code"`
	Message   string     `json:"message"`
	Data      LookupData `json:"data"`
	RequestID string     `json:"requestId"`
}

func (s *Service) LookupPhone(ctx context.Context, phone string) (LookupData, error) {
	result := LookupData{List: []LookupUser{}}
	phone = strings.TrimSpace(phone)
	if !mainlandPhonePattern.MatchString(phone) {
		return result, ErrInvalidAccount
	}
	var user User
	err := s.repository.db.WithContext(ctx).Select("id", "phone", "real_name").Where("phone = ? AND status = ?", phone, UserStatusActive).Take(&user).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return result, nil
	}
	if err != nil {
		return result, err
	}
	result.List = append(result.List, LookupUser{ID: user.ID, Username: *user.Phone, DisplayName: user.RealName})
	return result, nil
}

// LookupPhone 按手机号查询会员
// @Summary 按username中的手机号查询会员表
// @Description 沿用App会员Token鉴权。username必填，精确匹配users.phone，只查询正常且未删除的会员，不生成用电资料；data.list最多一条，不存在为空数组。返回id、username（手机号）、displayName（会员姓名），不返回密码或用电资料。
// @Tags App端 · 南网资料与账单
// @Produce json
// @Security BearerAuth
// @Param username query string true "完整中国大陆手机号，如13800138000"
// @Success 200 {object} LookupResponse
// @Failure 400 {object} response.ErrorEnvelope
// @Failure 401 {object} response.ErrorEnvelope
// @Failure 500 {object} response.ErrorEnvelope
func (h *Handler) LookupPhone(c *gin.Context) {
	c.Header("Cache-Control", "no-store")
	values := c.Request.URL.Query()["username"]
	if len(values) != 1 {
		response.Error(c, 400, response.CodeInvalidRequest, "username须传入一个完整手机号")
		return
	}
	data, err := h.service.LookupPhone(c.Request.Context(), values[0])
	if errors.Is(err, ErrInvalidAccount) {
		response.Error(c, 400, response.CodeInvalidRequest, "username须为有效的中国大陆手机号")
		return
	}
	if err != nil {
		slog.Error("lookup member by phone", "requestId", c.GetString("requestId"), "error", err)
		response.Error(c, 500, response.CodeInternalError, "服务器内部错误")
		return
	}
	response.Success(c, data)
}
