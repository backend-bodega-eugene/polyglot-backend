package tax

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
	Name           string `json:"name"`
	Sex            string `json:"sex"`
	Birth          string `json:"birth"`
	CardType       string `json:"cardType"`
	IDCard         string `json:"idCard"`
	Phone          string `json:"phone"`
	Email          string `json:"email"`
	TaxNumber      string `json:"taxNumber"`
	BankNum        string `json:"bankNum"`
	BankType       string `json:"bankType"`
	TaxBackMoney   string `json:"taxBackMoney" gorm:"-"`
	HujiCity       string `json:"hujiCity"`
	HujiAddress    string `json:"hujiAddress"`
	UsuallyCity    string `json:"usuallyCity"`
	UsuallyAddress string `json:"usuallyAddress"`
	ContactCity    string `json:"contactCity"`
	ContactAddress string `json:"contactAddress"`
	Education      string `json:"education"`
	Nation         string `json:"nation"`
	Country        string `json:"country"`
}
type Profile struct {
	UserID     uint64 `gorm:"primaryKey" json:"userId"`
	Fields     `gorm:"embedded"`
	Refund2019 string    `gorm:"column:refund2019" json:"-"`
	Refund2020 string    `gorm:"column:refund2020" json:"-"`
	Refund2021 string    `gorm:"column:refund2021" json:"-"`
	Refund2022 string    `gorm:"column:refund2022" json:"-"`
	Refund2023 string    `gorm:"column:refund2023" json:"-"`
	Version    string    `json:"version"`
	CreatedAt  time.Time `json:"createdAt"`
	UpdatedAt  time.Time `json:"updatedAt"`
}

func (Profile) TableName() string { return "tax_profiles" }

type View struct {
	Profile
	Simulated bool `json:"simulated"`
}
type Row struct {
	View
	Username string `json:"username"`
	RealName string `json:"realName"`
}
type List struct {
	Items    []Row `json:"items"`
	Total    int64 `json:"total"`
	Page     int   `json:"page"`
	PageSize int   `json:"pageSize"`
}
type WriteRequest struct {
	Fields
	ExpectedVersion string `json:"expectedVersion"`
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

func (Audit) TableName() string { return "tax_profile_audits" }

type Service struct{ db *gorm.DB }

func NewService(db *gorm.DB) *Service { return &Service{db: db} }

var ErrConflict = errors.New("所得税资料已存在或已被修改，请刷新后重试")
var ErrValidation = errors.New("所得税资料无效")

func invalid(message string) error { return fmt.Errorf("%w：%s", ErrValidation, message) }

var zone = time.FixedZone("Asia/Shanghai", 8*60*60)

func makeView(p Profile) View {
	p.TaxBackMoney = strings.Join([]string{p.Refund2019, p.Refund2020, p.Refund2021, p.Refund2022, p.Refund2023}, ",")
	p.CreatedAt = p.CreatedAt.In(zone).Truncate(time.Millisecond)
	p.UpdatedAt = p.UpdatedAt.In(zone).Truncate(time.Millisecond)
	return View{Profile: p, Simulated: true}
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
func (s *Service) Mine(ctx context.Context, uid uint64) (View, error) {
	var result View
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
			if err := tx.Create(&p).Error; err != nil {
				return err
			}
			if err := audit(tx, nil, uid, "app_generate", nil, &p); err != nil {
				return err
			}
		}
		result = makeView(p)
		return nil
	})
	return result, err
}
func (s *Service) Get(ctx context.Context, uid uint64) (View, error) {
	var u auth.User
	if err := s.db.WithContext(ctx).Select("id").First(&u, uid).Error; err != nil {
		return View{}, err
	}
	var p Profile
	err := s.db.WithContext(ctx).First(&p, "user_id = ?", uid).Error
	return makeView(p), err
}
func (s *Service) List(ctx context.Context, q string, uid uint64, page, size int) (List, error) {
	result := List{Items: []Row{}, Page: page, PageSize: size}
	query := s.db.WithContext(ctx).Table("tax_profiles p").Joins("JOIN users u ON u.id=p.user_id AND u.deleted_at IS NULL")
	if uid != 0 {
		query = query.Where("p.user_id = ?", uid)
	}
	if q != "" {
		like := "%" + q + "%"
		query = query.Where("CAST(p.user_id AS CHAR)=? OR p.name LIKE ? OR u.real_name LIKE ? OR u.phone LIKE ? OR u.email LIKE ?", q, like, like, like, like)
	}
	if err := query.Count(&result.Total).Error; err != nil {
		return result, err
	}
	var rows []struct {
		Profile  `gorm:"embedded"`
		Username string
		RealName string
	}
	err := query.Select("p.*,COALESCE(u.phone,u.email,'') AS username,u.real_name").Order("p.updated_at DESC,p.user_id ASC").Offset((page - 1) * size).Limit(size).Scan(&rows).Error
	for _, r := range rows {
		result.Items = append(result.Items, Row{View: makeView(r.Profile), Username: r.Username, RealName: r.RealName})
	}
	return result, err
}
func (s *Service) Write(ctx context.Context, adminID, uid uint64, req WriteRequest, action string) (View, error) {
	var result View
	if action == "create" || action == "update" {
		if err := validate(&req.Fields, time.Now()); err != nil {
			return result, err
		}
	}
	if (action == "update" || action == "delete") && req.ExpectedVersion == "" {
		return result, invalid("缺少版本号")
	}
	if action == "create" && req.ExpectedVersion != "" {
		return result, invalid("新建不得传旧版本")
	}
	err := s.lockUser(ctx, uid, func(tx *gorm.DB, u auth.User) error {
		var old Profile
		err := tx.First(&old, "user_id = ?", uid).Error
		if err != nil && !errors.Is(err, gorm.ErrRecordNotFound) {
			return err
		}
		exists := err == nil
		if !exists && (action == "update" || action == "delete") {
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
			return audit(tx, &adminID, uid, action, before, nil)
		}
		p := newProfile(uid, req.Fields)
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
		result = makeView(p)
		return nil
	})
	return result, err
}
func audit(tx *gorm.DB, adminID *uint64, uid uint64, action string, before, after *Profile) error {
	encode := func(p *Profile) *string {
		if p == nil {
			return nil
		}
		b, _ := json.Marshal(makeView(*p))
		v := string(b)
		return &v
	}
	return tx.Create(&Audit{AdminID: adminID, UserID: uid, Action: action, BeforePayload: encode(before), AfterPayload: encode(after)}).Error
}

var refundPattern = regexp.MustCompile(`^[0-9]{1,9}(\.[0-9]{1,2})?$`)

func validate(f *Fields, now time.Time) error {
	f.BankType = strings.TrimSpace(f.BankType)
	if !utf8.ValidString(f.BankType) || utf8.RuneCountInString(f.BankType) > 64 {
		return invalid("银行类型最多64个字符")
	}
	for _, r := range f.BankType {
		if r < 32 || (r >= 127 && r <= 159) {
			return invalid("银行类型不能包含控制字符")
		}
	}
	for _, field := range []struct {
		name  string
		value *string
		max   int
	}{
		{"姓名", &f.Name, 64}, {"性别", &f.Sex, 16}, {"证件类型", &f.CardType, 64}, {"证件号", &f.IDCard, 64}, {"纳税人识别号", &f.TaxNumber, 64}, {"银行卡号", &f.BankNum, 64}, {"户籍城市", &f.HujiCity, 128}, {"户籍地址", &f.HujiAddress, 255}, {"居住城市", &f.UsuallyCity, 128}, {"居住地址", &f.UsuallyAddress, 255}, {"联系城市", &f.ContactCity, 128}, {"联系地址", &f.ContactAddress, 255}, {"学历", &f.Education, 64}, {"民族", &f.Nation, 64}, {"国籍", &f.Country, 64},
	} {
		*field.value = strings.TrimSpace(*field.value)
		if !utf8.ValidString(*field.value) || utf8.RuneCountInString(*field.value) < 1 || utf8.RuneCountInString(*field.value) > field.max {
			return invalid(field.name + "不能为空或超出长度限制")
		}
	}
	birth, err := time.ParseInLocation("2006-01-02", f.Birth, zone)
	if err != nil || birth.Year() < 1900 || birth.After(now.In(zone)) {
		return invalid("出生日期须为1900年起且不晚于今天的YYYY-MM-DD")
	}
	f.Phone = strings.TrimSpace(f.Phone)
	f.Email = strings.TrimSpace(f.Email)
	if f.Phone != "" {
		p, _, err := auth.NormalizeMemberAccount(f.Phone)
		if err != nil || p == nil {
			return invalid("手机号格式错误")
		}
		f.Phone = *p
	}
	if f.Email != "" {
		_, e, err := auth.NormalizeMemberAccount(f.Email)
		if err != nil || e == nil {
			return invalid("邮箱格式错误")
		}
		f.Email = *e
	}
	values := strings.Split(f.TaxBackMoney, ",")
	if len(values) != 5 {
		return invalid("退税金额必须按2019至2023顺序提供5个逗号分隔金额")
	}
	for i, v := range values {
		v = strings.TrimSpace(v)
		if !refundPattern.MatchString(v) {
			return invalid("退税金额须为非负数字，最多9位整数2位小数")
		}
		r, _ := new(big.Rat).SetString(v)
		values[i] = r.FloatString(2)
	}
	f.TaxBackMoney = strings.Join(values, ",")
	return nil
}
func newProfile(uid uint64, f Fields) Profile {
	values := strings.Split(f.TaxBackMoney, ",")
	p := Profile{UserID: uid, Fields: f, Version: rand.Text()}
	if len(values) == 5 {
		p.Refund2019 = values[0]
		p.Refund2020 = values[1]
		p.Refund2021 = values[2]
		p.Refund2022 = values[3]
		p.Refund2023 = values[4]
	}
	return p
}
func generate(u auth.User) (Profile, error) {
	age, err := rand.Int(rand.Reader, big.NewInt(30))
	if err != nil {
		return Profile{}, err
	}
	now := time.Now().In(zone)
	numbers := make([]string, 3)
	for i := range numbers {
		numbers[i], err = displayNumber()
		if err != nil {
			return Profile{}, err
		}
	}
	f := Fields{Name: u.RealName, Sex: "男", Birth: fmt.Sprintf("%04d-06-15", now.Year()-25-int(age.Int64())), CardType: "居民身份证", IDCard: numbers[0], TaxNumber: numbers[1], BankNum: numbers[2], HujiCity: "广东省广州市", HujiAddress: "天河区天河路18号", UsuallyCity: "广东省广州市", UsuallyAddress: "天河区天河路20号", ContactCity: "广东省广州市", ContactAddress: "天河区天河路20号", Education: "本科", Nation: "汉族", Country: "中国"}
	if age.Int64()%2 == 1 {
		f.Sex = "女"
	}
	if u.Phone != nil {
		f.Phone = *u.Phone
	}
	if u.Email != nil {
		f.Email = *u.Email
	}
	refunds := []string{}
	for range 5 {
		n, err := rand.Int(rand.Reader, big.NewInt(500001))
		if err != nil {
			return Profile{}, err
		}
		refunds = append(refunds, fmt.Sprintf("%d.%02d", n.Int64()/100, n.Int64()%100))
	}
	f.TaxBackMoney = strings.Join(refunds, ",")
	if err := validate(&f, now); err != nil {
		return Profile{}, err
	}
	return newProfile(u.ID, f), nil
}

// Twelve-digit display placeholders. Deliberately not real identity, taxpayer
// or bank-card formats: no birth/region encoding, issuer prefix or checksum.
func displayNumber() (string, error) {
	n, err := rand.Int(rand.Reader, big.NewInt(900000000000))
	if err != nil {
		return "", err
	}
	return fmt.Sprintf("%d", 100000000000+n.Int64()), nil
}
