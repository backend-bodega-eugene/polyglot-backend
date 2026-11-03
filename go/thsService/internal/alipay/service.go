package alipay

import (
	"context"
	"crypto/rand"
	"encoding/json"
	"time"

	"gorm.io/gorm"
	"gorm.io/gorm/clause"
	"ths-service/internal/auth"
)

type Record struct {
	ID        uint64          `json:"id"`
	Kind      string          `json:"kind"`
	UserID    uint64          `json:"userId"`
	Year      int             `json:"year"`
	Payload   json.RawMessage `json:"payload" swaggertype:"object"`
	Version   string          `json:"version"`
	CreatedAt time.Time       `json:"createdAt"`
	UpdatedAt time.Time       `json:"updatedAt"`
}

func (Record) TableName() string { return "alipay_records" }

type Scope struct {
	Kind   string `gorm:"primaryKey"`
	UserID uint64 `gorm:"primaryKey"`
	Year   int    `gorm:"primaryKey"`
}

func (Scope) TableName() string { return "alipay_scopes" }

type Audit struct {
	ID            uint64
	RecordID      uint64
	Kind          string
	UserID        uint64
	Year          int
	AdminID       *uint64
	Action        string
	BeforePayload *string
	AfterPayload  *string
	CreatedAt     time.Time
}

func (Audit) TableName() string { return "alipay_audits" }

type Write struct {
	UserID          uint64         `json:"userId"`
	Year            int            `json:"year"`
	Payload         map[string]any `json:"payload" swaggertype:"object"`
	ExpectedVersion string         `json:"expectedVersion"`
}
type List struct {
	Items    []Record `json:"items"`
	Total    int64    `json:"total"`
	Page     int      `json:"page"`
	PageSize int      `json:"pageSize"`
}
type Service struct{ db *gorm.DB }

func NewService(db *gorm.DB) *Service { return &Service{db} }
func normalize(r Record) Record {
	r.CreatedAt = r.CreatedAt.In(Zone).Truncate(time.Millisecond)
	r.UpdatedAt = r.UpdatedAt.In(Zone).Truncate(time.Millisecond)
	return r
}
func lock(tx *gorm.DB, kind string, uid uint64, year int, app bool, fn func() error) error {
	if uid == 0 {
		return invalid("会员ID必填")
	}
	var u auth.User
	if e := tx.Clauses(clause.Locking{Strength: "UPDATE"}).First(&u, uid).Error; e != nil {
		return e
	}
	if app && u.Status != auth.UserStatusActive {
		return gorm.ErrRecordNotFound
	}
	scope := Scope{kind, uid, year}
	if e := tx.Clauses(clause.OnConflict{DoNothing: true}).Create(&scope).Error; e != nil {
		return e
	}
	if e := tx.Clauses(clause.Locking{Strength: "UPDATE"}).First(&scope, "kind=? AND user_id=? AND year=?", kind, uid, year).Error; e != nil {
		return e
	}
	return fn()
}
func audit(tx *gorm.DB, admin *uint64, action string, before, after *Record) error {
	r := after
	if r == nil {
		r = before
	}
	a := Audit{RecordID: r.ID, Kind: r.Kind, UserID: r.UserID, Year: r.Year, AdminID: admin, Action: action}
	if before != nil {
		b, e := json.Marshal(before)
		if e != nil {
			return e
		}
		s := string(b)
		a.BeforePayload = &s
	}
	if after != nil {
		b, e := json.Marshal(after)
		if e != nil {
			return e
		}
		s := string(b)
		a.AfterPayload = &s
	}
	return tx.Create(&a).Error
}
func (s *Service) Ensure(ctx context.Context, kind string, uid uint64, year int) ([]Record, error) {
	def, e := Definition(kind)
	if e != nil {
		return nil, e
	}
	if def.Yearly && !ValidYear(year) {
		return nil, invalid("年度须为2000年至当前年份")
	}
	if !def.Yearly {
		year = 0
	}
	var user auth.User
	if err := s.db.WithContext(ctx).First(&user, uid).Error; err != nil {
		return nil, err
	}
	result := []Record{}
	e = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		return lock(tx, kind, uid, year, true, func() error {
			if e := tx.Where("kind=? AND user_id=? AND year=?", kind, uid, year).Order("id DESC").Find(&result).Error; e != nil {
				return e
			}
			if len(result) > 0 {
				return nil
			}
			for _, p := range generate(kind, user, year) {
				if e := Validate(kind, year, p); e != nil {
					return e
				}
				b, e := json.Marshal(p)
				if e != nil {
					return e
				}
				r := Record{Kind: kind, UserID: uid, Year: year, Payload: b, Version: rand.Text()}
				if e = tx.Create(&r).Error; e != nil {
					return e
				}
				if e = audit(tx, nil, "app_generate", nil, &r); e != nil {
					return e
				}
				result = append([]Record{r}, result...)
			}
			return nil
		})
	})
	for i := range result {
		result[i] = normalize(result[i])
	}
	return result, e
}
func (s *Service) List(ctx context.Context, kind string, uid uint64, year int, q string, page, size int) (List, error) {
	out := List{Items: []Record{}, Page: page, PageSize: size}
	if _, e := Definition(kind); e != nil {
		return out, e
	}
	db := s.db.WithContext(ctx).Model(&Record{}).Joins("JOIN users u ON u.id=alipay_records.user_id AND u.deleted_at IS NULL").Where("alipay_records.kind=?", kind)
	if uid != 0 {
		db = db.Where("user_id=?", uid)
	}
	if year != 0 {
		db = db.Where("year=?", year)
	}
	if q != "" {
		db = db.Where("CAST(payload AS CHAR) LIKE ?", "%"+q+"%")
	}
	if e := db.Count(&out.Total).Error; e != nil {
		return out, e
	}
	e := db.Select("alipay_records.*").Order("alipay_records.id DESC").Offset((page - 1) * size).Limit(size).Find(&out.Items).Error
	for i := range out.Items {
		out.Items[i] = normalize(out.Items[i])
	}
	return out, e
}
func (s *Service) Get(ctx context.Context, kind string, id uint64) (Record, error) {
	if _, e := Definition(kind); e != nil {
		return Record{}, e
	}
	var r Record
	e := s.db.WithContext(ctx).Where("kind=? AND user_id IN (SELECT id FROM users WHERE deleted_at IS NULL)", kind).First(&r, id).Error
	return normalize(r), e
}
func (s *Service) Write(ctx context.Context, admin uint64, kind string, id uint64, w Write, action string) (Record, error) {
	if _, e := Definition(kind); e != nil {
		return Record{}, e
	}
	uid, year := w.UserID, w.Year
	if action != "create" {
		r, e := s.Get(ctx, kind, id)
		if e != nil {
			return Record{}, e
		}
		if (uid != 0 && uid != r.UserID) || (year != 0 && year != r.Year) {
			return Record{}, invalid("不能更换会员或年度")
		}
		uid, year = r.UserID, r.Year
	}
	if action != "delete" {
		if e := Validate(kind, year, w.Payload); e != nil {
			return Record{}, e
		}
	}
	var result Record
	e := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		return lock(tx, kind, uid, year, false, func() error {
			var before *Record
			if action != "create" {
				var r Record
				if e := tx.First(&r, "id=? AND kind=? AND user_id=?", id, kind, uid).Error; e != nil {
					return e
				}
				if w.ExpectedVersion == "" || w.ExpectedVersion != r.Version {
					return ErrConflict
				}
				before = &r
			} else if w.ExpectedVersion != "" {
				return ErrConflict
			}
			if action == "delete" {
				if e := tx.Delete(before).Error; e != nil {
					return e
				}
				return audit(tx, &admin, action, before, nil)
			}
			var others []Record
			if e := tx.Where("kind=? AND user_id=? AND year=? AND id<>?", kind, uid, year, id).Find(&others).Error; e != nil {
				return e
			}
			for _, r := range others {
				p := payload(r)
				if kind != "records" || (p["month"] == w.Payload["month"] && p["company"] == w.Payload["company"]) {
					return ErrConflict
				}
			}

			b, e := json.Marshal(w.Payload)
			if e != nil {
				return e
			}
			result = Record{Kind: kind, UserID: uid, Year: year, Payload: b, Version: rand.Text()}
			if before != nil {
				result.ID = before.ID
				result.CreatedAt = before.CreatedAt
			}
			if e = tx.Save(&result).Error; e != nil {
				return e
			}
			return audit(tx, &admin, action, before, &result)
		})
	})
	return normalize(result), e
}
func payload(r Record) map[string]any {
	var p map[string]any
	_ = json.Unmarshal(r.Payload, &p)
	return p
}
func value(p map[string]any, key string) int64 {
	s, _ := p[key].(string)
	if s == "" {
		return 0
	}
	return cents(s)
}
