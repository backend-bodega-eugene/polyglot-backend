package taxbusiness

import (
	"context"
	"crypto/rand"
	"encoding/json"
	"fmt"
	"sort"
	"strconv"
	"time"

	"gorm.io/gorm"
	"gorm.io/gorm/clause"
	"ths-service/internal/auth"
	"ths-service/internal/tax"
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

func (Record) TableName() string { return "tax_business_records" }

type Scope struct {
	Kind   string `gorm:"primaryKey"`
	UserID uint64 `gorm:"primaryKey"`
	Year   int    `gorm:"primaryKey"`
}

func (Scope) TableName() string { return "tax_business_scopes" }

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

func (Audit) TableName() string { return "tax_business_audits" }

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
		return nil, invalid("年度须为2019年至当前年份")
	}
	if !def.Yearly {
		year = 0
	}
	var employment tax.EmploymentView
	if kind == "monthly" {
		var n int64
		if e = s.db.WithContext(ctx).Model(&Record{}).Where("kind=? AND user_id=? AND year=?", kind, uid, year).Count(&n).Error; e != nil {
			return nil, e
		}
		if n == 0 {
			rows, err := tax.NewService(s.db).MyEmployment(ctx, uid)
			if err != nil {
				return nil, err
			}
			employment = rows[0]
		}
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
			for _, p := range generate(kind, uid, year, employment) {
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
	db := s.db.WithContext(ctx).Model(&Record{}).Joins("JOIN users u ON u.id=tax_business_records.user_id AND u.deleted_at IS NULL").Where("tax_business_records.kind=?", kind)
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
	e := db.Select("tax_business_records.*").Order("tax_business_records.id DESC").Offset((page - 1) * size).Limit(size).Find(&out.Items).Error
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
			if kind == "monthly" || kind == "applications" || kind == "openrecords" {
				var others []Record
				if e := tx.Where("kind=? AND user_id=? AND year=? AND id<>?", kind, uid, year, id).Find(&others).Error; e != nil {
					return e
				}
				for _, r := range others {
					p := payload(r)
					if kind == "applications" || (kind == "monthly" && p["expect_date"] == w.Payload["expect_date"] && p["xiaolei"] == w.Payload["xiaolei"]) || (kind == "openrecords" && p["yzm"] == w.Payload["yzm"]) {
						return ErrConflict
					}
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
func monthlyView(r Record) map[string]any {
	p := payload(r)
	p["id"] = r.ID
	p["shenbao_end"] = p["tax"]
	p["nowShenTax"] = p["tax"]
	p["special_deduction"] = money(value(p, "yanglao") + value(p, "yiliao") + value(p, "shiye") + value(p, "gjj"))
	return p
}

type Annual struct {
	List        []map[string]any `json:"list"`
	TotalIncome string           `json:"totalIncome"`
	TotalTax    string           `json:"totalTax"`
}

func annual(rows []Record) Annual {
	out := Annual{List: []map[string]any{}}
	var income, tax int64
	for _, r := range rows {
		p := monthlyView(r)
		income += value(p, "income")
		tax += value(p, "tax")
		out.List = append(out.List, p)
	}
	sort.SliceStable(out.List, func(i, j int) bool { return out.List[i]["expect_date"].(string) > out.List[j]["expect_date"].(string) })
	out.TotalIncome = money(income)
	out.TotalTax = money(tax)
	return out
}
func (s *Service) Annual(ctx context.Context, uid uint64, year int) (Annual, error) {
	rows, e := s.Ensure(ctx, "monthly", uid, year)
	return annual(rows), e
}
func (s *Service) Application(ctx context.Context, uid uint64, year int) (map[string]any, error) {
	a, e := s.Annual(ctx, uid, year)
	if e != nil {
		return nil, e
	}
	rows, e := s.Ensure(ctx, "applications", uid, year)
	if e != nil {
		return nil, e
	}
	p := payload(rows[0])
	p["year"] = year
	p["total_income"] = json.Number(a.TotalIncome)
	p["total_tax"] = json.Number(a.TotalTax)
	p["refund_amount"] = json.Number(p["refund_amount"].(string))
	return p, nil
}
func (s *Service) Calculation(ctx context.Context, uid uint64, date, subtype string, initialize bool) (map[string]any, error) {
	d, e := Month(date)
	if e != nil {
		return nil, e
	}
	var rows []Record
	if initialize {
		rows, e = s.Ensure(ctx, "monthly", uid, d.Year())
	} else {
		var u auth.User
		if e = s.db.WithContext(ctx).First(&u, uid).Error; e != nil {
			return nil, e
		}
		e = s.db.WithContext(ctx).Where("kind='monthly' AND user_id=? AND year=?", uid, d.Year()).Find(&rows).Error
	}
	if e != nil {
		return nil, e
	}
	p := map[string]any{"expect_date": date, "company_name": "", "rate": "10%", "susuanCount": "0.00", "detail_list": []map[string]any{}}
	sums := map[string]int64{}
	mapping := map[string]string{"totalIncome": "income", "totalMianShuiIncome": "tax_free_income", "totalFuMin": "deduction_money", "totalFuJiaMin": "additional_deduction", "totalOtherMin": "other_deduction", "totalYanglao": "personal_pension", "totalZhunJuan": "donate_project", "totalJianMian": "tax_reduction"}
	details := []map[string]any{}
	var taxes, nowTax int64
	for _, r := range rows {
		m := payload(r)
		period := m["expect_date"].(string)
		if period > date || m["xiaolei"] != subtype {
			continue
		}
		for target, source := range mapping {
			sums[target] += value(m, source)
		}
		sums["totalZhuanMin"] += value(m, "yanglao") + value(m, "yiliao") + value(m, "shiye") + value(m, "gjj")
		taxes += value(m, "tax")
		if period == date {
			p["rate"] = m["rate"]
			p["susuanCount"] = m["susuanCount"]
			nowTax += value(m, "tax")
			p["company_name"] = m["company_name"]
			details = append(details, map[string]any{"income": m["income"], "tax": m["tax"], "company_name": m["company_name"], "income_type": m["tax_name"], "subtype": m["xiaolei"], "expect_date": period})
		}
	}
	if len(details) == 0 {
		return nil, gorm.ErrRecordNotFound
	}
	for _, key := range []string{"totalIncome", "totalMianShuiIncome", "totalFuMin", "totalZhuanMin", "totalFuJiaMin", "totalOtherMin", "totalYanglao", "totalZhunJuan", "totalJianMian"} {
		p[key] = money(sums[key])
	}
	deductions := sums["totalFuMin"] + sums["totalZhuanMin"] + sums["totalFuJiaMin"] + sums["totalOtherMin"] + sums["totalYanglao"] + sums["totalZhunJuan"]
	taxable := sums["totalIncome"] - sums["totalMianShuiIncome"] - deductions
	if taxable < 0 {
		taxable = 0
	}
	p["totalYingNaShuiSuoDe"] = money(taxable)
	p["totalMin"] = money(deductions)
	p["totalYingJiaoTax"] = money(taxes)
	p["totalJiaoTax"] = money(taxes)
	p["totalYiJiaoTax"] = money(taxes - nowTax)
	p["nowShenTax"] = money(nowTax)
	p["bqsbse"] = money(nowTax)
	p["detail_list"] = details
	return p, nil
}
func (s *Service) OpenRecords(ctx context.Context, uid uint64) ([]map[string]any, error) {
	rows, e := s.Ensure(ctx, "openrecords", uid, 0)
	out := []map[string]any{}
	for _, r := range rows {
		p := payload(r)
		delete(p, "yzm")
		p["id"] = r.ID
		out = append(out, p)
	}
	sort.SliceStable(out, func(i, j int) bool { return out[i]["create_time"].(string) > out[j]["create_time"].(string) })
	return out, e
}
func (s *Service) Scan(ctx context.Context, uid uint64, code string) (map[string]any, error) {
	rows, e := s.Ensure(ctx, "openrecords", uid, 0)
	if e != nil {
		return nil, e
	}
	for _, r := range rows {
		p := payload(r)
		if p["yzm"] == code {
			return map[string]any{"images_data": p["images_data"]}, nil
		}
	}
	return nil, gorm.ErrRecordNotFound
}
func generate(kind string, uid uint64, year int, v tax.EmploymentView) []map[string]any {
	now := time.Now().In(Zone)
	switch kind {
	case "monthly":
		out := []map[string]any{}
		months := 12
		if year == now.Year() {
			months = int(now.Month())
		}
		for month := 1; month <= months; month++ {
			p := map[string]any{}
			for _, f := range MonthlyFields {
				p[f.Key] = ""
				if f.Type == "money" {
					p[f.Key] = "0.00"
				}
			}
			d := time.Date(year, time.Month(month), 1, 0, 0, 0, 0, Zone)
			p["zhaiyao"] = "工资薪金"
			p["expect_date"] = d.Format("2006-01")
			p["shenbao_date"] = d.AddDate(0, 1, 11).Format("2006-01-02")
			p["tax_name"] = "工资薪金"
			p["channel"] = "其他"
			p["xiaolei"] = "正常工资薪金"
			p["obligor_name"] = v.CompanyName
			p["tax_num"] = v.TaxID
			p["tax_authority"] = "主管税务机关"
			p["company_name"] = v.CompanyName
			p["company_num"] = v.CompanyNum
			p["tax_company"] = "主管税务机关"
			p["post"] = v.Post
			p["start_date"] = fmt.Sprintf("%d-01-01", year)
			p["end_date"] = ""
			income := int64(1500000 + int(uid%100)*1000 + month*15000)
			p["income"] = money(income)
			p["deduction_money"] = "5000.00"
			p["yanglao"] = "400.00"
			p["yiliao"] = "100.00"
			p["shiye"] = "20.00"
			p["gjj"] = "500.00"
			p["tax"] = money((income - 602000) / 10)
			p["rate"] = "10%"
			p["susuanCount"] = "0.00"
			p["type"] = json.Number("1")
			out = append(out, p)
		}
		return out
	case "applications":
		return []map[string]any{{"refund_amount": "0.00", "status": "未申报"}}
	case "openrecords":
		out := []map[string]any{}
		for i := 0; i < 2; i++ {
			month := now.AddDate(0, -i-1, 0).Format("2006-01")
			out = append(out, map[string]any{"create_time": now.Add(-time.Duration(i) * 24 * time.Hour).Format("2006-01-02 15:04:05"), "start_date": month, "end_date": month, "images_data": "", "yzm": "SIM-" + rand.Text()})
		}
		return out
	case "messages":
		out := []map[string]any{}
		for i := 0; i < 15; i++ {
			out = append(out, map[string]any{"title": "税务服务消息 " + strconv.Itoa(i+1), "content": "您可以在服务页面查询收入纳税明细、申报状态及相关记录。", "create_time": now.Add(-time.Duration(i) * 24 * time.Hour).Format("2006-01-02 15:04:05")})
		}
		return out
	}
	return nil
}
