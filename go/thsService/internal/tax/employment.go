package tax

import (
	"context"
	"crypto/rand"
	"encoding/json"
	"errors"
	"fmt"
	"math/big"
	"strings"
	"time"
	"unicode/utf8"

	"gorm.io/gorm"
	"ths-service/internal/auth"
)

type EmploymentFields struct {
	CompanyName string `json:"companyName"`
	TaxID       string `json:"taxId"`
	CompanyNum  string `json:"companyNum"`
	Post        string `json:"post"`
	StartDate   string `json:"startDate"`
	EndDate     string `json:"endDate"`
}
type Employment struct {
	UserID           uint64 `gorm:"primaryKey" json:"userId"`
	EmploymentFields `gorm:"embedded"`
	Version          string    `json:"version"`
	CreatedAt        time.Time `json:"createdAt"`
	UpdatedAt        time.Time `json:"updatedAt"`
}

func (Employment) TableName() string { return "tax_employments" }

type EmploymentView struct {
	Employment
	Simulated bool `json:"simulated"`
}
type EmploymentRow struct {
	EmploymentView
	Username string `json:"username"`
	RealName string `json:"realName"`
}
type EmploymentList struct {
	Items    []EmploymentRow `json:"items"`
	Total    int64           `json:"total"`
	Page     int             `json:"page"`
	PageSize int             `json:"pageSize"`
}
type EmploymentWrite struct {
	EmploymentFields
	ExpectedVersion string `json:"expectedVersion"`
}
type EmploymentAudit struct {
	ID            uint64
	UserID        uint64
	AdminID       *uint64
	Action        string
	BeforePayload *string
	AfterPayload  *string
	CreatedAt     time.Time
}

func (EmploymentAudit) TableName() string { return "tax_employment_audits" }

var ErrEmploymentConflict = errors.New("任职受雇单位已存在或已被修改，请刷新后重试")

func employmentView(e Employment) EmploymentView {
	e.CreatedAt = e.CreatedAt.In(zone).Truncate(time.Millisecond)
	e.UpdatedAt = e.UpdatedAt.In(zone).Truncate(time.Millisecond)
	return EmploymentView{Employment: e, Simulated: true}
}
func (s *Service) MyEmployment(ctx context.Context, uid uint64) ([]EmploymentView, error) {
	result := []EmploymentView{}
	err := s.lockUser(ctx, uid, func(tx *gorm.DB, u auth.User) error {
		if u.Status != auth.UserStatusActive {
			return gorm.ErrRecordNotFound
		}
		var e Employment
		err := tx.First(&e, "user_id = ?", uid).Error
		if err != nil && !errors.Is(err, gorm.ErrRecordNotFound) {
			return err
		}
		if errors.Is(err, gorm.ErrRecordNotFound) {
			e, err = generateEmployment(uid, time.Now())
			if err != nil {
				return err
			}
			if err := tx.Create(&e).Error; err != nil {
				return err
			}
			if err := employmentAudit(tx, nil, uid, "app_generate", nil, &e); err != nil {
				return err
			}
		}
		result = append(result, employmentView(e))
		return nil
	})
	return result, err
}
func (s *Service) GetEmployment(ctx context.Context, uid uint64) (EmploymentView, error) {
	var user auth.User
	if err := s.db.WithContext(ctx).Select("id").First(&user, uid).Error; err != nil {
		return EmploymentView{}, err
	}
	var e Employment
	err := s.db.WithContext(ctx).First(&e, "user_id = ?", uid).Error
	return employmentView(e), err
}
func (s *Service) ListEmployments(ctx context.Context, q string, uid uint64, page, size int) (EmploymentList, error) {
	result := EmploymentList{Items: []EmploymentRow{}, Page: page, PageSize: size}
	query := s.db.WithContext(ctx).Table("tax_employments e").Joins("JOIN users u ON u.id=e.user_id AND u.deleted_at IS NULL")
	if uid != 0 {
		query = query.Where("e.user_id = ?", uid)
	}
	if q != "" {
		like := "%" + q + "%"
		query = query.Where("CAST(e.user_id AS CHAR)=? OR e.company_name LIKE ? OR e.company_num LIKE ? OR u.real_name LIKE ? OR u.phone LIKE ? OR u.email LIKE ?", q, like, like, like, like, like)
	}
	if err := query.Count(&result.Total).Error; err != nil {
		return result, err
	}
	var rows []struct {
		Employment `gorm:"embedded"`
		Username   string
		RealName   string
	}
	err := query.Select("e.*,COALESCE(u.phone,u.email,'') AS username,u.real_name").Order("e.updated_at DESC,e.user_id ASC").Offset((page - 1) * size).Limit(size).Scan(&rows).Error
	for _, r := range rows {
		result.Items = append(result.Items, EmploymentRow{EmploymentView: employmentView(r.Employment), Username: r.Username, RealName: r.RealName})
	}
	return result, err
}
func (s *Service) WriteEmployment(ctx context.Context, adminID, uid uint64, req EmploymentWrite, action string) (EmploymentView, error) {
	var result EmploymentView
	if action == "create" || action == "update" {
		if err := validateEmployment(&req.EmploymentFields); err != nil {
			return result, err
		}
	}
	if (action == "update" || action == "delete") && req.ExpectedVersion == "" {
		return result, invalid("缺少单位版本号")
	}
	if action == "create" && req.ExpectedVersion != "" {
		return result, invalid("新建不得传旧版本")
	}
	err := s.lockUser(ctx, uid, func(tx *gorm.DB, _ auth.User) error {
		var old Employment
		err := tx.First(&old, "user_id = ?", uid).Error
		if err != nil && !errors.Is(err, gorm.ErrRecordNotFound) {
			return err
		}
		exists := err == nil
		if !exists && (action == "update" || action == "delete") {
			return gorm.ErrRecordNotFound
		}
		if exists && old.Version != req.ExpectedVersion || !exists && req.ExpectedVersion != "" {
			return ErrEmploymentConflict
		}
		var before *Employment
		if exists {
			before = &old
		}
		if action == "delete" {
			if err := tx.Delete(&old).Error; err != nil {
				return err
			}
			return employmentAudit(tx, &adminID, uid, action, before, nil)
		}
		e := Employment{UserID: uid, EmploymentFields: req.EmploymentFields, Version: rand.Text()}
		if action == "generate" {
			e, err = generateEmployment(uid, time.Now())
			if err != nil {
				return err
			}
		}
		if exists {
			e.CreatedAt = old.CreatedAt
		}
		if err := tx.Save(&e).Error; err != nil {
			return err
		}
		if err := employmentAudit(tx, &adminID, uid, action, before, &e); err != nil {
			return err
		}
		result = employmentView(e)
		return nil
	})
	return result, err
}
func employmentAudit(tx *gorm.DB, adminID *uint64, uid uint64, action string, before, after *Employment) error {
	encode := func(e *Employment) *string {
		if e == nil {
			return nil
		}
		b, _ := json.Marshal(employmentView(*e))
		v := string(b)
		return &v
	}
	return tx.Create(&EmploymentAudit{UserID: uid, AdminID: adminID, Action: action, BeforePayload: encode(before), AfterPayload: encode(after)}).Error
}
func validateEmployment(e *EmploymentFields) error {
	for _, f := range []struct {
		name  string
		value *string
		max   int
	}{{"单位名称", &e.CompanyName, 128}, {"纳税人识别号", &e.TaxID, 64}, {"单位编号", &e.CompanyNum, 64}, {"职务", &e.Post, 64}} {
		*f.value = strings.TrimSpace(*f.value)
		if !utf8.ValidString(*f.value) || utf8.RuneCountInString(*f.value) < 1 || utf8.RuneCountInString(*f.value) > f.max {
			return invalid(f.name + "不能为空或超出长度限制")
		}
	}
	start, err := time.Parse("2006-01-02", e.StartDate)
	if err != nil || start.Year() < 1900 || start.Year() > 2100 {
		return invalid("任职日期须为1900至2100年的YYYY-MM-DD")
	}
	if e.EndDate != "" {
		end, err := time.Parse("2006-01-02", e.EndDate)
		if err != nil || end.Year() > 2100 || end.Before(start) {
			return invalid("离职日期须为有效YYYY-MM-DD且不早于任职日期；在职时留空")
		}
	}
	return nil
}
func generateEmployment(uid uint64, now time.Time) (Employment, error) {
	n, err := rand.Int(rand.Reader, big.NewInt(9000))
	if err != nil {
		return Employment{}, err
	}
	fields := EmploymentFields{CompanyName: fmt.Sprintf("广州信息科技%d有限公司", n.Int64()+1000), TaxID: "SIM-EMP-TAX-" + rand.Text(), CompanyNum: "SIM-COMPANY-" + rand.Text(), Post: "职员", StartDate: now.In(zone).AddDate(-1-int(n.Int64()%10), 0, 0).Format("2006-01-02"), EndDate: ""}
	if err := validateEmployment(&fields); err != nil {
		return Employment{}, err
	}
	return Employment{UserID: uid, EmploymentFields: fields, Version: rand.Text()}, nil
}
