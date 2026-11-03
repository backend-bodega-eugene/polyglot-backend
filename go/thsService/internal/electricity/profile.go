package electricity

import (
	"context"
	"crypto/rand"
	"encoding/json"
	"errors"
	"fmt"
	"math/big"
	"regexp"
	"strings"
	"time"
	"unicode/utf8"

	"gorm.io/gorm"
	"gorm.io/gorm/clause"
	"ths-service/internal/auth"
)

type Fields struct {
	DisplayName  string `json:"displayName"`
	Address      string `json:"address"`
	UserIDNumber string `json:"userIdNumber"`
	Supplier     string `json:"supplier"`
	MeterReader  string `json:"meterReader"`
	MeterNo      string `json:"meterNo"`
	PostalCode   string `json:"postalCode"`
}
type Profile struct {
	UserID    uint64 `gorm:"primaryKey" json:"userId"`
	Fields    `gorm:"embedded"`
	Version   string    `json:"version"`
	CreatedAt time.Time `json:"createdAt"`
	UpdatedAt time.Time `json:"updatedAt"`
}

func (Profile) TableName() string { return "electricity_profiles" }

type View struct {
	Profile
	ID        uint64 `json:"id"`
	Username  string `json:"username"`
	RealName  string `json:"realName"`
	Simulated bool   `json:"simulated"`
}
type WriteRequest struct {
	Fields
	ExpectedVersion string `json:"expectedVersion"`
}
type List struct {
	Items    []View `json:"items"`
	Total    int64  `json:"total"`
	Page     int    `json:"page"`
	PageSize int    `json:"pageSize"`
}
type Audit struct {
	ID            uint64
	UserID        uint64
	AdminID       *uint64
	Action        string
	BeforePayload *string
	AfterPayload  *string
	CreatedAt     time.Time
}

func (Audit) TableName() string { return "electricity_profile_audits" }

type Service struct{ db *gorm.DB }

func NewService(db *gorm.DB) *Service { return &Service{db: db} }

var ErrConflict = errors.New("用电资料已存在或已被修改，请刷新后重试")
var ErrValidation = errors.New("用电资料无效")

func invalid(message string) error { return fmt.Errorf("%w：%s", ErrValidation, message) }

func makeView(p Profile, u auth.User) View {
	username := ""
	if u.Phone != nil {
		username = *u.Phone
	} else if u.Email != nil {
		username = *u.Email
	}
	return View{Profile: normalizedProfile(p), ID: u.ID, Username: username, RealName: u.RealName, Simulated: true}
}

var profileZone = time.FixedZone("Asia/Shanghai", 8*60*60)

func normalizedProfile(p Profile) Profile {
	p.CreatedAt = p.CreatedAt.In(profileZone).Truncate(time.Millisecond)
	p.UpdatedAt = p.UpdatedAt.In(profileZone).Truncate(time.Millisecond)
	return p
}
func (s *Service) lockUser(ctx context.Context, uid uint64, fn func(*gorm.DB, auth.User) error) error {
	return s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		var u auth.User
		if err := tx.Clauses(clause.Locking{Strength: "UPDATE"}).First(&u, uid).Error; err != nil {
			return err
		}
		return fn(tx, u)
	})
}

// Mine reads only the authenticated member, generating once under the member row lock.
func (s *Service) Mine(ctx context.Context, uid uint64) (View, error) {
	var view View
	err := s.lockUser(ctx, uid, func(tx *gorm.DB, u auth.User) error {
		if u.Status != auth.UserStatusActive {
			return gorm.ErrRecordNotFound
		}
		var p Profile
		err := tx.First(&p, "user_id = ?", uid).Error
		if err != nil && !errors.Is(err, gorm.ErrRecordNotFound) {
			return err
		}
		if errors.Is(err, gorm.ErrRecordNotFound) {
			p, err = generate(u)
			if err != nil {
				return err
			}
			if err = tx.Create(&p).Error; err != nil {
				return err
			}
			if err = audit(tx, nil, uid, "app_generate", nil, &p); err != nil {
				return err
			}
		}
		view = makeView(p, u)
		return nil
	})
	return view, err
}
func (s *Service) Get(ctx context.Context, uid uint64) (View, error) {
	var u auth.User
	if err := s.db.WithContext(ctx).First(&u, uid).Error; err != nil {
		return View{}, err
	}
	var p Profile
	err := s.db.WithContext(ctx).First(&p, "user_id = ?", uid).Error
	return makeView(p, u), err
}
func (s *Service) List(ctx context.Context, q string, uid uint64, page, size int) (List, error) {
	result := List{Items: []View{}, Page: page, PageSize: size}
	query := s.db.WithContext(ctx).Table("electricity_profiles AS p").Joins("JOIN users u ON u.id=p.user_id AND u.deleted_at IS NULL")
	if uid != 0 {
		query = query.Where("p.user_id = ?", uid)
	}
	if q != "" {
		like := "%" + q + "%"
		query = query.Where("CAST(p.user_id AS CHAR)=? OR p.display_name LIKE ? OR p.user_id_number LIKE ? OR p.meter_no LIKE ? OR u.real_name LIKE ? OR u.phone LIKE ? OR u.email LIKE ?", q, like, like, like, like, like, like)
	}
	if err := query.Count(&result.Total).Error; err != nil {
		return result, err
	}
	var records []struct {
		Profile  `gorm:"embedded"`
		Username string
		RealName string
	}
	err := query.Select("p.*, COALESCE(u.phone,u.email,'') AS username, u.real_name").Order("p.updated_at DESC,p.user_id ASC").Offset((page - 1) * size).Limit(size).Scan(&records).Error
	for _, r := range records {
		result.Items = append(result.Items, View{Profile: normalizedProfile(r.Profile), ID: r.UserID, Username: r.Username, RealName: r.RealName, Simulated: true})
	}
	return result, err
}
func (s *Service) Write(ctx context.Context, adminID, uid uint64, req WriteRequest, action string) (View, error) {
	var view View
	if action == "create" || action == "update" {
		if err := validate(&req.Fields); err != nil {
			return view, err
		}
	}
	if (action == "update" || action == "delete") && req.ExpectedVersion == "" {
		return view, invalid("缺少版本号，请刷新后重试")
	}
	if action == "create" && req.ExpectedVersion != "" {
		return view, invalid("新建资料不得传旧版本号")
	}
	err := s.lockUser(ctx, uid, func(tx *gorm.DB, u auth.User) error {
		var old Profile
		err := tx.First(&old, "user_id = ?", uid).Error
		if err != nil && !errors.Is(err, gorm.ErrRecordNotFound) {
			return err
		}
		exists := err == nil
		if (action == "update" || action == "delete") && !exists {
			return gorm.ErrRecordNotFound
		}
		if exists && old.Version != req.ExpectedVersion || !exists && req.ExpectedVersion != "" {
			return ErrConflict
		}
		var before *Profile
		if exists {
			before = &old
		}
		if action == "delete" {
			if err := tx.Delete(&old).Error; err != nil {
				return err
			}
			return audit(tx, &adminID, uid, "delete", before, nil)
		}
		p := Profile{UserID: uid, Fields: req.Fields, Version: rand.Text()}
		if action == "generate" {
			p, err = generate(u)
			if err != nil {
				return err
			}
		}
		if exists {
			p.CreatedAt = old.CreatedAt
		}
		if err := tx.Save(&p).Error; err != nil {
			return err
		}
		if err := audit(tx, &adminID, uid, action, before, &p); err != nil {
			return err
		}
		view = makeView(p, u)
		return nil
	})
	return view, err
}
func audit(tx *gorm.DB, adminID *uint64, uid uint64, action string, before, after *Profile) error {
	encode := func(p *Profile) *string {
		if p == nil {
			return nil
		}
		b, _ := json.Marshal(p)
		v := string(b)
		return &v
	}
	return tx.Create(&Audit{AdminID: adminID, UserID: uid, Action: action, BeforePayload: encode(before), AfterPayload: encode(after)}).Error
}

var numberPattern = regexp.MustCompile(`^[0-9]{11,32}$`)
var meterPattern = regexp.MustCompile(`^[0-9]{6,32}$`)
var postalPattern = regexp.MustCompile(`^[0-9]{6}$`)

func validate(f *Fields) error {
	for _, field := range []struct {
		name  string
		value *string
		max   int
	}{{"展示姓名", &f.DisplayName, 64}, {"地址", &f.Address, 255}, {"供电单位", &f.Supplier, 128}, {"抄表员", &f.MeterReader, 64}} {
		*field.value = strings.TrimSpace(*field.value)
		if !utf8.ValidString(*field.value) || utf8.RuneCountInString(*field.value) < 1 || utf8.RuneCountInString(*field.value) > field.max {
			return invalid(field.name + "不能为空或超出长度限制")
		}
	}
	f.UserIDNumber = strings.TrimSpace(f.UserIDNumber)
	f.MeterNo = strings.TrimSpace(f.MeterNo)
	f.PostalCode = strings.TrimSpace(f.PostalCode)
	if !numberPattern.MatchString(f.UserIDNumber) {
		return invalid("户号须为11至32位数字")
	}
	if !meterPattern.MatchString(f.MeterNo) {
		return invalid("表号须为6至32位数字")
	}
	if !postalPattern.MatchString(f.PostalCode) {
		return invalid("邮编须为6位数字")
	}
	return nil
}
func generate(u auth.User) (Profile, error) {
	digits := func(n int) (string, error) {
		text := make([]byte, n)
		for i := range text {
			x, err := rand.Int(rand.Reader, big.NewInt(10))
			if err != nil {
				return "", err
			}
			text[i] = byte('0' + x.Int64())
		}
		return string(text), nil
	}
	customer, err := digits(10)
	if err != nil {
		return Profile{}, err
	}
	meter, err := digits(13)
	if err != nil {
		return Profile{}, err
	}
	room, err := rand.Int(rand.Reader, big.NewInt(2901))
	if err != nil {
		return Profile{}, err
	}
	fields := Fields{DisplayName: u.RealName, Address: fmt.Sprintf("广东省广州市天河区天河路18号%d室", room.Int64()+101), UserIDNumber: "050200" + customer, Supplier: "广东电网广州供电局", MeterReader: "李明", MeterNo: "05020" + meter, PostalCode: "510000"}
	if err := validate(&fields); err != nil {
		return Profile{}, err
	}
	return Profile{UserID: u.ID, Fields: fields, Version: rand.Text()}, nil
}
