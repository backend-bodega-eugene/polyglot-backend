package electricity

import (
	"bytes"
	"context"
	"crypto/rand"
	"crypto/sha256"
	"encoding/json"
	"errors"
	"fmt"
	"math/big"
	"regexp"
	"sort"
	"time"

	"gorm.io/gorm"
	"ths-service/internal/auth"
)

type Bill struct {
	ID         uint64    `gorm:"primaryKey" json:"id"`
	UserID     uint64    `json:"userId"`
	Code       string    `json:"code"`
	StartDate  string    `json:"startDate"`
	EndDate    string    `json:"endDate"`
	Amount     string    `json:"amount"`
	TotalUsage string    `json:"totalUsage"`
	Detail     string    `json:"-"`
	Version    string    `json:"version"`
	CreatedAt  time.Time `json:"createdAt"`
	UpdatedAt  time.Time `json:"updatedAt"`
}

func (Bill) TableName() string { return "electricity_bills" }

type BillView struct {
	Bill
	BillMonth string          `json:"billMonth"`
	Detail    json.RawMessage `json:"detail" swaggertype:"object"`
	Simulated bool            `json:"simulated"`
	Billing   Billing         `json:"billing"`
}
type BillWrite struct {
	Code            string          `json:"code"`
	StartDate       string          `json:"startDate"`
	EndDate         string          `json:"endDate"`
	Amount          string          `json:"amount"`
	TotalUsage      string          `json:"totalUsage"`
	Detail          json.RawMessage `json:"detail" swaggertype:"object"`
	ExpectedVersion string          `json:"expectedVersion"`
}
type BillRow struct {
	BillView
	Username string `json:"username"`
	RealName string `json:"realName"`
}
type BillList struct {
	Items      []BillRow `json:"items"`
	Total      int64     `json:"total"`
	Page       int       `json:"page"`
	PageSize   int       `json:"pageSize"`
	SetVersion string    `json:"setVersion"`
}
type BillAudit struct {
	ID            uint64
	UserID        uint64
	AdminID       *uint64
	BillID        *uint64
	Action        string
	BeforePayload *string
	AfterPayload  *string
	CreatedAt     time.Time
}

func (BillAudit) TableName() string { return "electricity_bill_audits" }

var ErrBillConflict = errors.New("账单已存在或已被修改，请刷新后重试")

func billView(b Bill) BillView {
	b.CreatedAt = b.CreatedAt.In(profileZone).Truncate(time.Millisecond)
	b.UpdatedAt = b.UpdatedAt.In(profileZone).Truncate(time.Millisecond)
	month := ""
	if len(b.Code) == 6 {
		month = b.Code[:4] + "-" + b.Code[4:]
	}
	// MySQL rewrites JSON spacing/key order; canonicalize so first generation and
	// subsequent database reads have the same response representation.
	decoder := json.NewDecoder(bytes.NewBufferString(b.Detail))
	decoder.UseNumber()
	var value any
	if err := decoder.Decode(&value); err == nil {
		encoded, _ := json.Marshal(value)
		b.Detail = string(encoded)
	}
	return BillView{Bill: b, BillMonth: month, Detail: json.RawMessage(b.Detail), Simulated: true, Billing: BillingForBill(b)}
}
func readBills(tx *gorm.DB, uid uint64) ([]Bill, error) {
	rows := []Bill{}
	err := tx.Where("user_id = ?", uid).Order("code DESC,id DESC").Find(&rows).Error
	return rows, err
}
func billSetVersion(rows []Bill) string {
	if len(rows) == 0 {
		return ""
	}
	copyRows := append([]Bill(nil), rows...)
	sort.Slice(copyRows, func(i, j int) bool { return copyRows[i].ID < copyRows[j].ID })
	values := []string{}
	for _, b := range copyRows {
		values = append(values, fmt.Sprintf("%d:%s", b.ID, b.Version))
	}
	encoded, _ := json.Marshal(values)
	return fmt.Sprintf("%x", sha256.Sum256(encoded))
}
func (s *Service) MyBills(ctx context.Context, uid uint64) ([]BillView, error) {
	result := []BillView{}
	err := s.lockUser(ctx, uid, func(tx *gorm.DB, u auth.User) error {
		if u.Status != auth.UserStatusActive {
			return gorm.ErrRecordNotFound
		}
		rows, err := readBills(tx, uid)
		if err != nil {
			return err
		}
		if len(rows) == 0 {
			rows, err = generateBills(uid, time.Now())
			if err != nil {
				return err
			}
			if err := tx.Create(&rows).Error; err != nil {
				return err
			}
			if err := billAudit(tx, nil, uid, nil, "app_generate", nil, rows); err != nil {
				return err
			}
		}
		for _, b := range rows {
			result = append(result, billView(b))
		}
		return nil
	})
	return result, err
}
func (s *Service) GetBill(ctx context.Context, uid, bid uint64) (BillView, error) {
	var user auth.User
	if err := s.db.WithContext(ctx).Select("id").First(&user, uid).Error; err != nil {
		return BillView{}, err
	}
	var bill Bill
	err := s.db.WithContext(ctx).Where("user_id = ? AND id = ?", uid, bid).First(&bill).Error
	return billView(bill), err
}
func (s *Service) ListBills(ctx context.Context, uid uint64, code, q string, page, size int) (BillList, error) {
	result := BillList{Items: []BillRow{}, Page: page, PageSize: size}
	if code != "" {
		if err := validBillCode(code); err != nil {
			return result, err
		}
	}
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		// A user-specific version covers every bill, including rows on other pages.
		if uid != 0 {
			var u auth.User
			if err := tx.Select("id").First(&u, uid).Error; err != nil {
				return err
			}
			rows, err := readBills(tx, uid)
			if err != nil {
				return err
			}
			result.SetVersion = billSetVersion(rows)
		}
		query := tx.Table("electricity_bills b").Joins("JOIN users u ON u.id=b.user_id AND u.deleted_at IS NULL")
		if uid != 0 {
			query = query.Where("b.user_id = ?", uid)
		}
		if code != "" {
			query = query.Where("b.code = ?", code)
		}
		if q != "" {
			query = query.Where("CAST(b.user_id AS CHAR)=? OR u.real_name LIKE ? OR u.phone LIKE ? OR u.email LIKE ?", q, "%"+q+"%", "%"+q+"%", "%"+q+"%")
		}
		if err := query.Count(&result.Total).Error; err != nil {
			return err
		}
		var rows []struct {
			Bill     `gorm:"embedded"`
			Username string
			RealName string
		}
		if err := query.Select("b.*,COALESCE(u.phone,u.email,'') AS username,u.real_name").Order("b.code DESC,b.id DESC").Offset((page - 1) * size).Limit(size).Scan(&rows).Error; err != nil {
			return err
		}
		for _, row := range rows {
			result.Items = append(result.Items, BillRow{BillView: billView(row.Bill), Username: row.Username, RealName: row.RealName})
		}
		return nil
	})
	return result, err
}
func (s *Service) WriteBill(ctx context.Context, adminID, uid, bid uint64, req BillWrite, action string) (any, error) {
	var result any
	if action == "create" || action == "update" {
		if err := validateBill(req); err != nil {
			return nil, err
		}
	}
	if (action == "update" || action == "delete") && req.ExpectedVersion == "" {
		return nil, invalid("账单缺少版本号，请刷新后重试")
	}
	if action == "create" && req.ExpectedVersion != "" {
		return nil, invalid("新建账单不能传旧版本")
	}
	err := s.lockUser(ctx, uid, func(tx *gorm.DB, _ auth.User) error {
		if action == "generate" {
			before, err := readBills(tx, uid)
			if err != nil {
				return err
			}
			if billSetVersion(before) != req.ExpectedVersion {
				return ErrBillConflict
			}
			rows, err := generateBills(uid, time.Now())
			if err != nil {
				return err
			}
			if err := tx.Where("user_id = ?", uid).Delete(&Bill{}).Error; err != nil {
				return err
			}
			if err := tx.Create(&rows).Error; err != nil {
				return err
			}
			if err := billAudit(tx, &adminID, uid, nil, action, before, rows); err != nil {
				return err
			}
			views := []BillView{}
			for _, b := range rows {
				views = append(views, billView(b))
			}
			result = views
			return nil
		}
		var before *Bill
		if action != "create" {
			var old Bill
			if err := tx.Where("user_id = ? AND id = ?", uid, bid).First(&old).Error; err != nil {
				return err
			}
			if old.Version != req.ExpectedVersion {
				return ErrBillConflict
			}
			before = &old
		}
		if action == "delete" {
			if err := tx.Delete(before).Error; err != nil {
				return err
			}
			return billAudit(tx, &adminID, uid, &bid, action, before, nil)
		}
		detail := req.Detail
		if len(detail) == 0 {
			detail = json.RawMessage(`{}`)
		}
		b := Bill{ID: bid, UserID: uid, Code: req.Code, StartDate: req.StartDate, EndDate: req.EndDate, Amount: canonicalDecimal(req.Amount), TotalUsage: canonicalDecimal(req.TotalUsage), Detail: string(detail), Version: rand.Text()}
		if before != nil {
			b.CreatedAt = before.CreatedAt
		}
		var duplicate int64
		if err := tx.Model(&Bill{}).Where("user_id = ? AND code = ? AND id <> ?", uid, b.Code, bid).Count(&duplicate).Error; err != nil {
			return err
		}
		if duplicate > 0 {
			return ErrBillConflict
		}
		if err := tx.Save(&b).Error; err != nil {
			return err
		}
		if err := billAudit(tx, &adminID, uid, &b.ID, action, before, &b); err != nil {
			return err
		}
		result = billView(b)
		return nil
	})
	return result, err
}
func billAudit(tx *gorm.DB, adminID *uint64, uid uint64, bid *uint64, action string, before, after any) error {
	encode := func(v any) *string {
		if v == nil {
			return nil
		}
		switch value := v.(type) {
		case *Bill:
			if value != nil {
				v = billView(*value)
			}
		case []Bill:
			views := []BillView{}
			for _, b := range value {
				views = append(views, billView(b))
			}
			v = views
		}
		b, _ := json.Marshal(v)
		value := string(b)
		return &value
	}
	return tx.Create(&BillAudit{UserID: uid, AdminID: adminID, BillID: bid, Action: action, BeforePayload: encode(before), AfterPayload: encode(after)}).Error
}

var billDecimal = regexp.MustCompile(`^[0-9]{1,9}(\.[0-9]{1,2})?$`)

func canonicalDecimal(v string) string { r, _ := new(big.Rat).SetString(v); return r.FloatString(2) }
func validBillCode(code string) error {
	if len(code) != 6 {
		return invalid("账期须为YYYYMM")
	}
	date, err := time.Parse("200601", code)
	if err != nil || date.Year() < 2000 || date.Year() > 2100 {
		return invalid("账期须为2000至2100年的有效YYYYMM")
	}
	return nil
}
func validateBill(req BillWrite) error {
	if err := validBillCode(req.Code); err != nil {
		return err
	}
	start, e1 := time.Parse("2006-01-02", req.StartDate)
	end, e2 := time.Parse("2006-01-02", req.EndDate)
	if e1 != nil || e2 != nil || start.After(end) || start.Year() < 2000 || end.Year() > 2100 {
		return invalid("账单起止日期无效，格式为YYYY-MM-DD，结束不得早于开始")
	}
	if !billDecimal.MatchString(req.Amount) || !billDecimal.MatchString(req.TotalUsage) {
		return invalid("账单金额和用电量须为非负数字，最多9位整数和2位小数")
	}
	if len(req.Detail) > 8192 {
		return invalid("账单明细不能超过8KB")
	}
	if len(req.Detail) > 0 {
		var detail map[string]json.RawMessage
		if json.Unmarshal(req.Detail, &detail) != nil || detail == nil {
			return invalid("账单明细须为JSON对象")
		}
	}
	return validateBilling(req)
}
func generateBills(uid uint64, now time.Time) ([]Bill, error) {
	current := now.In(profileZone)
	month := time.Date(current.Year(), current.Month(), 1, 0, 0, 0, 0, profileZone)
	rows := make([]Bill, 0, 20)
	for i := 1; i <= 20; i++ {
		n, err := rand.Int(rand.Reader, big.NewInt(65001))
		if err != nil {
			return nil, err
		}
		usage := n.Int64() + 5000 // 50.00–700.00 kWh
		totalUsage := fmt.Sprintf("%d.%02d", usage/100, usage%100)
		start := month.AddDate(0, -i, 0)
		end := start.AddDate(0, 1, -1)
		billing := generatedBilling(usage, start, end)
		amount := billing.ElectricityFee
		detail, _ := json.Marshal(map[string]any{"billing": billing, "unitPrice": billing.AveragePrice, "electricityFee": amount, "serviceFee": "0.00", "description": "居民用电账单"})
		rows = append(rows, Bill{UserID: uid, Code: start.Format("200601"), StartDate: start.Format("2006-01-02"), EndDate: end.Format("2006-01-02"), Amount: amount, TotalUsage: totalUsage, Detail: string(detail), Version: rand.Text()})
	}
	return rows, nil
}
