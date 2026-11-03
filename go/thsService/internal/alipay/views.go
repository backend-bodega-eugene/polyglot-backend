package alipay

import (
	"context"
	"fmt"
	"math/rand/v2"
	"sort"
	"strconv"
	"time"

	"gorm.io/gorm"
	"ths-service/internal/auth"
)

func maskedName(name string) string {
	r := []rune(name)
	if len(r) == 0 {
		return "*用户"
	}
	return "*" + string(r[len(r)-1:])
}

func newManagementCode() string {
	code := []byte("110500111111111")
	for i := 6; i < len(code); i++ {
		code[i] = byte('1' + rand.IntN(9))
	}
	return string(code)
}
func generate(kind string, u auth.User, year int) []map[string]any {
	name := u.RealName
	if name == "" {
		name = "用户"
	}
	masked := maskedName(name)
	electricNo := fmt.Sprintf("SIM-E-%08d", u.ID)
	waterNo := fmt.Sprintf("SIM-W-%08d", u.ID)
	switch kind {
	case "profile":
		phone := ""
		if u.Phone != nil {
			phone = *u.Phone
		}
		return []map[string]any{{"name": name, "phone": phone, "accountName": masked, "idNo": fmt.Sprintf("SIM-********%04d", u.ID%10000)}}
	case "insurance":
		return []map[string]any{{"manageCode": newManagementCode(), "cityName": "深圳市", "status": "参保缴费", "interestAmount": "200.00"}}
	case "accounts":
		return []map[string]any{{"leftName": masked, "leftAccountNo": electricNo, "rightName": masked, "rightAccountNo": waterNo}}
	case "electric":
		return []map[string]any{{"payee": "供电有限公司", "amount": "0.00", "accountNo": electricNo, "accountName": masked, "address": "深圳市天河路8号", "supplier": "供电单位"}}
	case "water":
		return []map[string]any{{"amount": "0.00", "accountNo": waterNo, "accountName": masked, "addressLine1": "深圳市天河路8号", "addressLine2": "1栋101室", "supplier": "供水有限公司"}}
	case "records":
		months := 12
		now := time.Now().In(Zone)
		if year == now.Year() {
			months = int(now.Month())
		}
		out := []map[string]any{}
		for m := 1; m <= months; m++ {
			base := int64(450000 + int(u.ID%30)*1000 + m*100)
			out = append(out, map[string]any{"month": fmt.Sprintf("%04d%02d", year, m), "company": "深圳市科技有限公司", "type": "正常核定", "base": money(base), "identity": "城镇职工", "companyPay": money(base * 16 / 100), "personalPay": money(base * 8 / 100)})
		}
		return out
	}
	return nil
}
func (s *Service) Singleton(ctx context.Context, kind string, uid uint64) (map[string]any, error) {
	rows, e := s.Ensure(ctx, kind, uid, 0)
	if e != nil {
		return nil, e
	}
	return payload(rows[0]), nil
}

type Account struct {
	Name      string `json:"name"`
	AccountNo string `json:"accountNo"`
}
type Accounts struct {
	Left  Account `json:"left"`
	Right Account `json:"right"`
}

func (s *Service) Accounts(ctx context.Context, uid uint64) (Accounts, error) {
	p, e := s.Singleton(ctx, "accounts", uid)
	if e != nil {
		return Accounts{}, e
	}
	return Accounts{Account{p["leftName"].(string), p["leftAccountNo"].(string)}, Account{p["rightName"].(string), p["rightAccountNo"].(string)}}, nil
}

type Contribution struct {
	Month   string `json:"month"`
	Company string `json:"company"`
	Amount  string `json:"amount"`
	Type    string `json:"type"`
}
type Records struct {
	ManageCode string         `json:"manageCode"`
	CityName   string         `json:"cityName"`
	Year       string         `json:"year"`
	List       []Contribution `json:"list"`
}
type Detail struct {
	Year        string `json:"year"`
	UnitName    string `json:"unitName"`
	Type        string `json:"type"`
	Base        string `json:"base"`
	Total       string `json:"total"`
	Identity    string `json:"identity"`
	CompanyPay  string `json:"companyPay"`
	PersonalPay string `json:"personalPay"`
}

func contribution(r Record) Contribution {
	p := payload(r)
	return Contribution{p["month"].(string), p["company"].(string), money(value(p, "companyPay") + value(p, "personalPay")), p["type"].(string)}
}
func (s *Service) Records(ctx context.Context, uid uint64, year int) (Records, error) {
	meta, e := s.Singleton(ctx, "insurance", uid)
	if e != nil {
		return Records{}, e
	}
	rows, e := s.Ensure(ctx, "records", uid, year)
	if e != nil {
		return Records{}, e
	}
	out := Records{meta["manageCode"].(string), meta["cityName"].(string), strconv.Itoa(year), []Contribution{}}
	for _, r := range rows {
		out.List = append(out.List, contribution(r))
	}
	sort.SliceStable(out.List, func(i, j int) bool { return out.List[i].Month > out.List[j].Month })
	return out, nil
}
func (s *Service) Detail(ctx context.Context, uid uint64, year int, month, company string) (Detail, error) {
	rows, e := s.Ensure(ctx, "records", uid, year)
	if e != nil {
		return Detail{}, e
	}
	for _, r := range rows {
		p := payload(r)
		if p["month"] == month && p["company"] == company {
			return Detail{month, company, p["type"].(string), p["base"].(string), money(value(p, "companyPay") + value(p, "personalPay")), p["identity"].(string), p["companyPay"].(string), p["personalPay"].(string)}, nil
		}
	}
	return Detail{}, gorm.ErrRecordNotFound
}

type Summary struct {
	ManageCode     string `json:"manageCode"`
	CityName       string `json:"cityName"`
	Status         string `json:"status"`
	FirstDate      string `json:"firstDate"`
	LastMonth      string `json:"lastMonth"`
	LastCompany    string `json:"lastCompany"`
	MonthCount     string `json:"monthCount"`
	PersonalAmount string `json:"personalAmount"`
	CompanyAmount  string `json:"companyAmount"`
	InterestAmount string `json:"interestAmount"`
}

func (s *Service) Summary(ctx context.Context, uid uint64) (Summary, error) {
	meta, e := s.Singleton(ctx, "insurance", uid)
	if e != nil {
		return Summary{}, e
	}
	var count int64
	if e = s.db.WithContext(ctx).Model(&Record{}).Where("kind='records' AND user_id=?", uid).Count(&count).Error; e != nil {
		return Summary{}, e
	}
	if count == 0 {
		if _, e = s.Ensure(ctx, "records", uid, time.Now().In(Zone).Year()); e != nil {
			return Summary{}, e
		}
	}
	out := Summary{ManageCode: meta["manageCode"].(string), CityName: meta["cityName"].(string), Status: meta["status"].(string), InterestAmount: meta["interestAmount"].(string)}
	// Sum a single database snapshot, so dates and amounts refer to the same rows.
	var rows []Record
	if e = s.db.WithContext(ctx).Where("kind='records' AND user_id=?", uid).Order("id DESC").Find(&rows).Error; e != nil {
		return Summary{}, e
	}
	months := map[string]bool{}
	first, last := "", ""
	var personal, company int64
	for _, r := range rows {
		p := payload(r)
		month := p["month"].(string)
		months[month] = true
		personal += value(p, "personalPay")
		company += value(p, "companyPay")
		if first == "" || month < first {
			first = month
		}
		if month > last {
			last = month
			out.LastCompany = p["company"].(string)
		}
	}
	if first != "" {
		out.FirstDate = first + "01"
	}
	out.LastMonth = last
	out.MonthCount = strconv.Itoa(len(months))
	out.PersonalAmount = money(personal)
	out.CompanyAmount = money(company)
	return out, nil
}
