package electricity

import (
	"encoding/json"
	"fmt"
	"math/big"
	"regexp"
	"time"
)

// TariffTier is a saved price snapshot, not a reference to a mutable price list.
type TariffTier struct {
	Level     int    `json:"level"`
	Usage     string `json:"usage"`
	UnitPrice string `json:"unit_price"`
	Amount    string `json:"amount"`
}

// Billing is shared by App responses and detail.billing in administration writes.
type Billing struct {
	Region           string       `json:"region"`
	UsageType        string       `json:"usage_type"`
	PricingMode      string       `json:"pricing_mode"` // tiered or flat
	TariffName       string       `json:"tariff_name"`
	Tiers            []TariffTier `json:"tiers"`
	ElectricityFee   string       `json:"electricity_fee"`
	AveragePrice     string       `json:"average_price"`
	PaymentStatus    string       `json:"payment_status"` // paid, unpaid or overdue
	DueDate          string       `json:"due_date"`
	LateFeeStartDate string       `json:"late_fee_start_date"`
	LateFeeAsOfDate  string       `json:"late_fee_as_of_date"`
	ArrearsPrincipal string       `json:"arrears_principal"`
	LateFeeDailyRate string       `json:"late_fee_daily_rate"`
	LateFee          string       `json:"late_fee"`
	PayableAmount    string       `json:"payable_amount"`
}

func BillingForBill(b Bill) Billing {
	var d struct {
		Billing *Billing `json:"billing"`
	}
	_ = json.Unmarshal([]byte(b.Detail), &d)
	if d.Billing != nil {
		return *d.Billing
	}
	average := "0.00000000"
	usage, ok := new(big.Rat).SetString(b.TotalUsage)
	amount, valid := new(big.Rat).SetString(b.Amount)
	if ok && valid && usage.Sign() > 0 {
		average = new(big.Rat).Quo(amount, usage).FloatString(8)
	}
	return Billing{PricingMode: "flat", Tiers: []TariffTier{}, ElectricityFee: b.Amount, AveragePrice: average, ArrearsPrincipal: "0.00", LateFeeDailyRate: "0.001", LateFee: "0.00", PayableAmount: b.Amount}
}

func generatedBilling(usage int64, start, end time.Time) Billing {
	first, second := int64(20000), int64(40000)
	if start.Month() >= 5 && start.Month() <= 10 {
		first, second = 26000, 60000
	}
	quantities := []int64{min(usage, first), min(max(usage-first, 0), second-first), max(usage-second, 0)}
	prices := []int64{66286875, 71286875, 96286875}
	v := Billing{Region: "深圳市", UsageType: "居民生活用电", PricingMode: "tiered", TariffName: "深圳居民单一制阶梯电价", PaymentStatus: "paid", DueDate: start.AddDate(0, 1, 23).Format("2006-01-02"), LateFeeStartDate: start.AddDate(0, 1, 24).Format("2006-01-02"), ArrearsPrincipal: "0.00", LateFeeDailyRate: "0.001", LateFee: "0.00", Tiers: []TariffTier{}}
	total := new(big.Rat)
	for i, q := range quantities {
		fee := new(big.Rat).Mul(big.NewRat(q, 100), big.NewRat(prices[i], 100000000)).FloatString(2)
		f, _ := new(big.Rat).SetString(fee)
		total.Add(total, f)
		v.Tiers = append(v.Tiers, TariffTier{i + 1, fmt.Sprintf("%d.%02d", q/100, q%100), big.NewRat(prices[i], 100000000).FloatString(8), fee})
	}
	v.ElectricityFee = total.FloatString(2)
	v.PayableAmount = v.ElectricityFee
	v.AveragePrice = new(big.Rat).Quo(total, big.NewRat(usage, 100)).FloatString(8)
	return v
}

var priceDecimal = regexp.MustCompile(`^[0-9]{1,9}(\.[0-9]{1,8})?$`)

func validateBilling(req BillWrite) error {
	var detail map[string]json.RawMessage
	if len(req.Detail) == 0 {
		return nil
	}
	if json.Unmarshal(req.Detail, &detail) != nil {
		return invalid("账单明细格式错误")
	}
	raw, exists := detail["billing"]
	if !exists {
		return nil
	}
	var v Billing
	if string(raw) == "null" || json.Unmarshal(raw, &v) != nil {
		return invalid("billing须为计费信息对象")
	}
	if len(v.Region) > 120 || len(v.UsageType) > 120 || len(v.TariffName) > 240 {
		return invalid("计费方案文字过长")
	}
	if v.PricingMode != "flat" && v.PricingMode != "tiered" {
		return invalid("计费方式须为flat或tiered")
	}
	if v.PaymentStatus != "" && v.PaymentStatus != "paid" && v.PaymentStatus != "unpaid" && v.PaymentStatus != "overdue" {
		return invalid("缴费状态无效")
	}
	for _, s := range []string{v.DueDate, v.LateFeeStartDate, v.LateFeeAsOfDate} {
		if s != "" {
			d, e := time.Parse("2006-01-02", s)
			if e != nil || d.Year() < 2000 || d.Year() > 2100 {
				return invalid("缴费日期须为YYYY-MM-DD")
			}
		}
	}
	for _, s := range []string{v.ElectricityFee, v.ArrearsPrincipal, v.LateFee, v.PayableAmount} {
		if !billDecimal.MatchString(s) {
			return invalid("计费金额须为非负数，最多两位小数")
		}
	}
	if !priceDecimal.MatchString(v.AveragePrice) || !priceDecimal.MatchString(v.LateFeeDailyRate) {
		return invalid("平均电价、日违约金率最多8位小数")
	}
	if len(v.Tiers) > 3 {
		return invalid("最多三个档位")
	}
	for _, t := range v.Tiers {
		if t.Level < 1 || t.Level > 3 || !billDecimal.MatchString(t.Usage) || !billDecimal.MatchString(t.Amount) || !priceDecimal.MatchString(t.UnitPrice) {
			return invalid("阶梯档位须为1至3，电量、单价、金额须为合法数字")
		}
	}
	return nil
}
