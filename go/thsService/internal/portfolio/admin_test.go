package portfolio

import (
	"encoding/json"
	"errors"
	"testing"
	"time"
)

func editableHolding(now time.Time) AdminHoldingWrite {
	return AdminHoldingWrite{TotalAssets: "200000.00", Items: []HoldingItem{{Position: Position{Symbol: "600001.SH", Name: "测试股票", Currency: "CNY", LatestPrice: "12.0000", Quantity: 100, AvailableQuantity: 100, DailyProfit: "10.00"}, Trades: []Trade{
		{Side: "buy", Quantity: 200, Price: "10.0000", Fees: "2.00", TradedAt: now.Add(-48 * time.Hour)},
		{Side: "sell", Quantity: 100, Price: "11.0000", Fees: "1.00", TradedAt: now.Add(-24 * time.Hour)},
	}}}}
}
func TestAdminNormalizeUsesTradesAndRecalculates(t *testing.T) {
	now := time.Now()
	req := editableHolding(now)
	// Ignore forged client-derived values and ensure fees enter weighted cost.
	req.Items[0].Position.Profit = "99999.00"
	req.Items[0].Trades[0].Amount = "1.00"
	data, err := NormalizeAdminHoldings(7, "a_stock", req, now)
	if err != nil {
		t.Fatal(err)
	}
	p := data.Items[0].Position
	if p.CostPrice != "10.0100" || p.MarketValue != "1200.00" || p.Profit != "199.00" || p.RealizedProfit != "98.00" || p.TransactionFees != "3.00" {
		t.Fatalf("incorrect derived values: %+v", p)
	}
	if data.Account.AvailableCash != "198800.00" || data.Account.FloatingProfit != "199.00" || data.Account.DailyProfit != "10.00" {
		t.Fatal(data.Account)
	}
	if data.Items[0].Trades[1].Amount != "2000.00" {
		t.Fatal("forged amount preserved")
	}
	if req.Items[0].Trades[0].Amount != "1.00" {
		t.Fatal("mutated input")
	}
	// Empty positions persist intentional empty portfolio.
	req.Items = []HoldingItem{}
	data, err = NormalizeAdminHoldings(7, "a_stock", req, now)
	if err != nil || len(data.Items) != 0 || data.Account.AvailableCash != "200000.00" {
		t.Fatalf("empty: %+v %v", data, err)
	}
}
func TestAdminNormalizeValidation(t *testing.T) {
	now := time.Now()
	tests := []struct {
		name   string
		change func(*AdminHoldingWrite)
	}{
		{"oversell", func(r *AdminHoldingWrite) { r.Items[0].Trades[1].Quantity = 300 }},
		{"mismatch", func(r *AdminHoldingWrite) { r.Items[0].Position.Quantity = 200 }},
		{"board lot", func(r *AdminHoldingWrite) { r.Items[0].Trades[0].Quantity = 201 }},
		{"future", func(r *AdminHoldingWrite) { r.Items[0].Trades[0].TradedAt = now.Add(time.Hour) }},
		{"negative fee", func(r *AdminHoldingWrite) { r.Items[0].Trades[0].Fees = "-1" }},
		{"fractional price", func(r *AdminHoldingWrite) { r.Items[0].Trades[0].Price = "1/2" }},
		{"bad side", func(r *AdminHoldingWrite) { r.Items[0].Trades[0].Side = "other" }},
		{"unfunded", func(r *AdminHoldingWrite) { r.Items[0].Position.LatestPrice = "999999" }},
		{"duplicate", func(r *AdminHoldingWrite) { r.Items = append(r.Items, r.Items[0]) }},
		{"availability", func(r *AdminHoldingWrite) { r.Items[0].Position.AvailableQuantity = 200 }},
		{"currency", func(r *AdminHoldingWrite) { r.Items[0].Position.Currency = "EUR" }},
	}
	for _, test := range tests {
		t.Run(test.name, func(t *testing.T) {
			req := editableHolding(now)
			test.change(&req)
			_, err := NormalizeAdminHoldings(7, "a_stock", req, now)
			if !errors.Is(err, ErrHoldingValidation) {
				t.Fatalf("validation error: %v", err)
			}
		})
	}
}
func TestAdminNormalizeForeignLossAndSameTime(t *testing.T) {
	now := time.Now()
	req := editableHolding(now)
	req.Items[0].Position.Currency = "USD"
	req.Items[0].Trades[1].Price = "9.0000"
	data, err := NormalizeAdminHoldings(7, "us_stock", req, now)
	if err != nil {
		t.Fatal(err)
	}
	if p := data.Items[0].Position; p.RealizedProfit != "-724.20" || p.MarketValue != "8520.00" {
		t.Fatal(p)
	}
	// Round-tripping a normalized snapshot must not change financial values.
	encoded, _ := json.Marshal(data.Items)
	var items []HoldingItem
	json.Unmarshal(encoded, &items)
	req.Items = items
	next, err := NormalizeAdminHoldings(7, "us_stock", req, now)
	if err != nil {
		t.Fatal(err)
	}
	if next.Items[0].Position.Profit != data.Items[0].Position.Profit {
		t.Fatal("repeated save changed profit")
	}
}
