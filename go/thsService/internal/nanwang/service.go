package nanwang

import (
	"context"
	"crypto/rand"
	"encoding/json"
	"errors"
	"fmt"
	"regexp"
	"strconv"
	"time"

	"gorm.io/gorm"
	"gorm.io/gorm/clause"
	"ths-service/internal/auth"
	"ths-service/internal/electricity"
)

var decimalPattern = regexp.MustCompile(`^[0-9]{1,9}(\.[0-9]{1,2})?$`)

type Record struct {
	ID        uint64          `json:"id"`
	Kind      string          `json:"kind"`
	OwnerID   uint64          `json:"userId"`
	Payload   json.RawMessage `json:"payload" swaggertype:"object"`
	Version   string          `json:"version"`
	CreatedAt time.Time       `json:"createdAt"`
	UpdatedAt time.Time       `json:"updatedAt"`
}

func (Record) TableName() string { return "nanwang_records" }

type Scope struct {
	Kind    string `gorm:"primaryKey"`
	OwnerID uint64 `gorm:"primaryKey"`
}

func (Scope) TableName() string { return "nanwang_scopes" }

type Audit struct {
	ID            uint64
	RecordID      uint64
	Kind          string
	OwnerID       uint64
	AdminID       *uint64
	Action        string
	BeforePayload *string
	AfterPayload  *string
	CreatedAt     time.Time
}

func (Audit) TableName() string { return "nanwang_audits" }

type Write struct {
	UserID          uint64         `json:"userId"`
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
func normalized(r Record) Record {
	zone := time.FixedZone("Asia/Shanghai", 28800)
	r.CreatedAt = r.CreatedAt.In(zone).Truncate(time.Millisecond)
	r.UpdatedAt = r.UpdatedAt.In(zone).Truncate(time.Millisecond)
	return r
}
func lockScope(tx *gorm.DB, kind string, uid uint64, app bool, fn func() error) error {
	if uid != 0 {
		var u auth.User
		if err := tx.Clauses(clause.Locking{Strength: "UPDATE"}).First(&u, uid).Error; err != nil {
			return err
		}
		if app && u.Status != auth.UserStatusActive {
			return gorm.ErrRecordNotFound
		}
	}
	scope := Scope{kind, uid}
	if err := tx.Clauses(clause.OnConflict{DoNothing: true}).Create(&scope).Error; err != nil {
		return err
	}
	if err := tx.Clauses(clause.Locking{Strength: "UPDATE"}).First(&scope, "kind=? AND owner_id=?", kind, uid).Error; err != nil {
		return err
	}
	return fn()
}
func audit(tx *gorm.DB, admin *uint64, action string, before, after *Record) error {
	r := after
	if r == nil {
		r = before
	}
	a := Audit{RecordID: r.ID, Kind: r.Kind, OwnerID: r.OwnerID, AdminID: admin, Action: action}
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

// Ensure only initializes an entirely empty scope. A partially deleted set is never topped up.
func (s *Service) Ensure(ctx context.Context, kind string, uid uint64) ([]Record, error) {
	def, err := Definition(kind)
	if err != nil {
		return nil, err
	}
	if def.Public {
		uid = 0
	}
	if !def.Public && uid == 0 {
		return nil, invalid("会员ID必填")
	}
	var profile electricity.View
	// Reuse the existing household when this scope is first initialized. Do not regenerate
	// unrelated electricity profiles on later business reads or public requests.
	if kind == "recommended" || kind == "bound" || kind == "arrears" || kind == "points" {
		var count int64
		if err = s.db.WithContext(ctx).Model(&Record{}).Where("kind=? AND owner_id=?", kind, uid).Count(&count).Error; err != nil {
			return nil, err
		}
		if count == 0 {
			profile, err = electricity.NewService(s.db).Mine(ctx, uid)
			if err != nil {
				return nil, err
			}
		}
	}
	result := []Record{}
	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		return lockScope(tx, kind, uid, true, func() error {
			if e := tx.Where("kind=? AND owner_id=?", kind, uid).Order("id DESC").Find(&result).Error; e != nil {
				return e
			}
			if len(result) > 0 {
				return nil
			}
			for _, p := range generate(kind, profile) {
				if e := Validate(kind, p); e != nil {
					return e
				}
				b, e := json.Marshal(p)
				if e != nil {
					return e
				}
				r := Record{Kind: kind, OwnerID: uid, Payload: b, Version: rand.Text()}
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
		result[i] = normalized(result[i])
	}
	return result, err
}
func (s *Service) List(ctx context.Context, kind string, uid uint64, q string, page, size int) (List, error) {
	out := List{Items: []Record{}, Page: page, PageSize: size}
	def, e := Definition(kind)
	if e != nil {
		return out, e
	}
	db := s.db.WithContext(ctx).Model(&Record{}).Where("nanwang_records.kind=?", kind)
	if !def.Public {
		db = db.Joins("JOIN users u ON u.id=nanwang_records.owner_id AND u.deleted_at IS NULL")
		if uid != 0 {
			db = db.Where("owner_id=?", uid)
		}
	} else {
		db = db.Where("owner_id=0")
	}
	if q != "" {
		db = db.Where("CAST(payload AS CHAR) LIKE ?", "%"+q+"%")
	}
	if e = db.Count(&out.Total).Error; e != nil {
		return out, e
	}
	e = db.Select("nanwang_records.*").Order("nanwang_records.id DESC").Offset((page - 1) * size).Limit(size).Find(&out.Items).Error
	for i := range out.Items {
		out.Items[i] = normalized(out.Items[i])
	}
	return out, e
}
func (s *Service) Get(ctx context.Context, kind string, id uint64) (Record, error) {
	def, e := Definition(kind)
	if e != nil {
		return Record{}, e
	}
	var r Record
	db := s.db.WithContext(ctx).Where("kind=?", kind)
	if !def.Public {
		db = db.Where("owner_id IN (SELECT id FROM users WHERE deleted_at IS NULL)")
	}
	e = db.First(&r, id).Error
	return normalized(r), e
}
func (s *Service) Write(ctx context.Context, admin uint64, kind string, id uint64, w Write, action string) (Record, error) {
	def, err := Definition(kind)
	if err != nil {
		return Record{}, err
	}
	if action != "delete" {
		if err = Validate(kind, w.Payload); err != nil {
			return Record{}, err
		}
	}
	if def.Public && w.UserID != 0 {
		return Record{}, invalid("公共数据会员ID必须为0")
	}
	uid := w.UserID
	if action != "create" {
		r, e := s.Get(ctx, kind, id)
		if e != nil {
			return Record{}, e
		}
		uid = r.OwnerID
		if w.UserID != 0 && w.UserID != uid {
			return Record{}, invalid("不能更换所属会员")
		}
	}
	if !def.Public && uid == 0 {
		return Record{}, invalid("会员ID必填")
	}
	var result Record
	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		return lockScope(tx, kind, uid, false, func() error {
			var before *Record
			if action != "create" {
				var r Record
				if e := tx.First(&r, "id=? AND kind=? AND owner_id=?", id, kind, uid).Error; e != nil {
					return e
				}
				if w.ExpectedVersion == "" || w.ExpectedVersion != r.Version {
					return ErrConflict
				}
				before = &r
			} else {
				if w.ExpectedVersion != "" {
					return ErrConflict
				}
				if def.Singleton {
					var n int64
					if e := tx.Model(&Record{}).Where("kind=? AND owner_id=?", kind, uid).Count(&n).Error; e != nil {
						return e
					}
					if n > 0 {
						return ErrConflict
					}
				}
			}
			if action == "delete" {
				if e := tx.Delete(before).Error; e != nil {
					return e
				}
				return audit(tx, &admin, "delete", before, nil)
			}
			b, e := json.Marshal(w.Payload)
			if e != nil {
				return e
			}
			result = Record{Kind: kind, OwnerID: uid, Payload: b, Version: rand.Text()}
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
	return normalized(result), err
}
func Payload(r Record) map[string]any {
	var p map[string]any
	_ = json.Unmarshal(r.Payload, &p)
	p["id"] = strconv.FormatUint(r.ID, 10)
	return p
}
func generate(kind string, v electricity.View) []map[string]any {
	switch kind {
	case "news":
		out := []map[string]any{}
		now := time.Now().In(time.FixedZone("Asia/Shanghai", 28800))
		titles := []string{"安全用电温馨提示", "停电检修通知", "节约用电小知识", "电费账单查询指南", "营业厅服务介绍", "雨季用电注意事项"}
		for i := 0; i < 12; i++ {
			stamp := now.Add(-time.Duration(i) * 24 * time.Hour).Format("2006-01-02 15:04:05")
			out = append(out, map[string]any{"title": titles[i%len(titles)], "classId": strconv.Itoa(i%3 + 1), "areaCode": "050200", "issuedName": "南网在线", "source": "用电资讯", "publishTime": stamp, "createTime": stamp, "imgUrl": "", "coverUrl": "", "link": "", "readCount": json.Number("0"), "content": "请定期检查用电设备，注意用电安全。检修安排及服务信息请关注供电服务通知。"})
		}
		return out
	case "recommended", "bound", "arrears":
		p := map[string]any{"userName": v.DisplayName, "custName": v.DisplayName, "eleCustId": v.UserIDNumber, "custNo": v.UserIDNumber, "address": v.Address, "eleAddr": "", "eleCustNumberId": fmt.Sprintf("SIM-%d", v.ID)}
		if kind == "arrears" {
			p["arrears"] = "36.80"
			p["totalArrears"] = "36.80"
		}
		return []map[string]any{p}
	case "points":
		return []map[string]any{{"eleCustId": v.UserIDNumber, "pointName": "计量点1"}}
	case "halls":
		return []map[string]any{{"hallName": "天河营业厅", "address": "广东省广州市天河区天河路8号", "distance": "1.20"}, {"hallName": "越秀营业厅", "address": "广东省广州市越秀区天河路16号", "distance": "3.50"}}
	case "messages":
		return []map[string]any{{"title": "电费账单已出"}, {"title": "欢迎使用用电服务"}}
	case "charging":
		return []map[string]any{{"hasPile": false}}
	}
	return nil
}

// Keep errors identifiable across service and HTTP layers.
func IsNotFound(err error) bool { return errors.Is(err, gorm.ErrRecordNotFound) }
