package portfolio

import (
	"math/big"
	randv2 "math/rand/v2"
	"sort"
	"time"

	"ths-service/internal/market"
)

type HoldingItem struct {
	Position      Position `json:"position"`
	PositionRatio string   `json:"positionRatio"`
	Trades        []Trade  `json:"trades"`
	Notice        string   `json:"notice,omitempty"`
}
type HoldingsData struct {
	Simulated        bool          `json:"simulated"`
	Category         string        `json:"category"`
	CategoryName     string        `json:"categoryName"`
	Account          Account       `json:"account"`
	Items            []HoldingItem `json:"items"`
	ExchangeRateMode string        `json:"exchangeRateMode" example:"demo_fixed"`
}
type HoldingsResponse struct {
	Code      int          `json:"code"`
	Message   string       `json:"message"`
	Data      HoldingsData `json:"data"`
	RequestID string       `json:"requestId"`
}

// Amounts in Position and Account are CNY. Prices and Trade amounts use the quote currency.
// Fixed rates are explicitly demo-only; futures use one simulation unit without leverage.
func demoRate(currency string) int64 {
	switch currency {
	case "CNY":
		return 1000000
	case "HKD":
		return 910000
	case "USD":
		return 7100000
	}
	return 0
}
func toCNY(amount, rate int64) int64 { return (amount*rate + 500000) / 1000000 }
func decimalCents(s string) int64 {
	r, ok := new(big.Rat).SetString(s)
	if !ok {
		return 0
	}
	r.Mul(r, big.NewRat(100, 1))
	return new(big.Int).Quo(r.Num(), r.Denom()).Int64()
}
func lotSize(category string) int64 {
	switch category {
	case "a_stock", "etf":
		return 100
	case "convertible_bond":
		return 10
	}
	return 1
}

// Integer yuan, inclusive bounds; internal amounts are cents.
func randomAssets() int64 { return int64(200000+randv2.IntN(800001)) * 100 }

func makeHolding(userID uint64, generation string, q market.QuoteItem, category string, budget int64) Position {
	p := Position{UserID: userID, Generation: generation, Symbol: q.Symbol, Name: q.Name, Currency: q.Currency, MarketValue: "0.00", Profit: "0.00", ProfitPercent: "0.00", DailyProfit: "0.00", DailyProfitPercent: "0.00", RealizedProfit: "0.00", TransactionFees: "0.00", CostPrice: "0.0000", LatestPrice: q.LatestPrice, QuoteTime: q.QuoteTime, DataStatus: q.DataStatus}
	rate := demoRate(q.Currency)
	p.ExchangeRate = fixed(rate, 6)
	price, ok := priceUnits(q.LatestPrice)
	if !ok || rate == 0 {
		return p
	}
	lot := lotSize(category)
	lotValue := toCNY(value(price, lot), rate)
	if lotValue <= 0 {
		return p
	}
	p.Quantity = budget / lotValue * lot
	p.AvailableQuantity = p.Quantity
	// Protect against per-lot rounding when computing the complete position.
	for p.Quantity > 0 && toCNY(value(price, p.Quantity), rate) > budget {
		p.Quantity -= lot
		p.AvailableQuantity = p.Quantity
	}
	cost := price * 90 / 100
	p.CostPrice = fixed(cost, 4)
	p.LatestPrice = fixed(price, 4)
	mv := toCNY(value(price, p.Quantity), rate)
	cv := toCNY(value(cost, p.Quantity), rate)
	change := int64(0)
	if r, ok := new(big.Rat).SetString(q.ChangeAmount); ok {
		r.Mul(r, big.NewRat(10000, 1))
		n := new(big.Int).Quo(r.Num(), r.Denom())
		if n.IsInt64() {
			change = n.Int64()
		}
	}
	if change >= price || change <= -price {
		change = 0
	}
	day := mv - toCNY(value(price-change, p.Quantity), rate)
	p.MarketValue = money(mv)
	p.Profit = money(mv - cv)
	p.ProfitPercent = ratio(mv-cv, cv)
	p.DailyProfit = money(day)
	p.DailyProfitPercent = ratio(day, mv-day)
	return p
}

// Add closed round trips before the existing opening buy. Each pair closes at
// its purchase price, so it changes neither cash, cost basis nor realized profit.
func detailTrades(p Position, existing []Trade, lot, _ int64) []Trade {
	price, ok := priceUnits(p.CostPrice)
	if !ok {
		return nil
	}
	if demoRate(p.Currency) == 0 || len(existing) >= 15 {
		return nil
	}
	end := time.Now().In(beijing).AddDate(0, 0, -3)
	for _, t := range existing {
		if t.TradedAt.Before(end) {
			end = t.TradedAt.In(beijing).AddDate(0, 0, -1)
		}
	}
	result := make([]Trade, 0, 15-len(existing))
	appendTrade := func(side string, quantity, price int64, day time.Time) {
		for day.Weekday() == time.Saturday || day.Weekday() == time.Sunday {
			day = day.AddDate(0, 0, -1)
		}
		day = time.Date(day.Year(), day.Month(), day.Day(), 10, 30, 0, 0, beijing)
		result = append(result, Trade{UserID: p.UserID, Generation: p.Generation, Symbol: p.Symbol, Name: p.Name, Side: side, Quantity: quantity, Price: fixed(price, 4), Amount: money(value(price, quantity)), TradedAt: day, Currency: p.Currency, Fees: "0.00"})
	}
	remaining := 15 - len(existing)
	if remaining == 1 {
		return nil
	}
	if len(existing) == 0 && p.Quantity > 0 {
		appendTrade("buy", p.Quantity, price, end)
		remaining--
	}
	// Three closing records allow an odd number of records with zero net holdings.
	// Double board lots keep every quantity valid, including for zero positions.
	if remaining%2 == 1 {
		triplePrice := max(100, ((price+50)/100)*100)
		appendTrade("buy", 2*lot, triplePrice, end.AddDate(0, 0, -12))
		appendTrade("sell", lot, triplePrice, end.AddDate(0, 0, -9))
		appendTrade("sell", lot, triplePrice, end.AddDate(0, 0, -6))
		remaining -= 3
	}
	for i := 0; i < remaining/2; i++ {
		pairPrice := max(1, price*int64(91+i)/100)
		appendTrade("buy", lot, pairPrice, end.AddDate(0, 0, -(20+i*5)))
		appendTrade("sell", lot, pairPrice, end.AddDate(0, 0, -(18+i*5)))
	}
	return result
}

func recalculate(account *Account, positions []Position) {
	mv, profit, day := int64(0), int64(0), int64(0)
	for _, p := range positions {
		mv += decimalCents(p.MarketValue)
		profit += decimalCents(p.Profit)
		day += decimalCents(p.DailyProfit)
	}
	assets := decimalCents(account.TotalAssets)
	account.TotalMarketValue = money(mv)
	account.FloatingProfit = money(profit)
	account.DailyProfit = money(day)
	account.DailyProfitPercent = ratio(day, assets-day)
	account.AvailableCash = money(assets - mv)
	account.WithdrawableCash = account.AvailableCash
	account.PositionRatio = ratio(mv, assets)
}

func holdingDays(trades []Trade, now time.Time) int {
	ordered := append([]Trade(nil), trades...)
	sort.Slice(ordered, func(i, j int) bool {
		if ordered[i].TradedAt.Equal(ordered[j].TradedAt) {
			return ordered[i].ID < ordered[j].ID
		}
		return ordered[i].TradedAt.Before(ordered[j].TradedAt)
	})
	var started time.Time
	net := int64(0)
	for _, trade := range ordered {
		if trade.Side == "buy" {
			if net == 0 {
				started = trade.TradedAt
			}
			net += trade.Quantity
		} else {
			net -= trade.Quantity
		}
	}
	if net <= 0 || started.IsZero() {
		return 0
	}
	start := started.In(beijing)
	a := time.Date(start.Year(), start.Month(), start.Day(), 0, 0, 0, 0, beijing)
	b := time.Date(now.Year(), now.Month(), now.Day(), 0, 0, 0, 0, beijing)
	return max(0, int(b.Sub(a).Hours()/24))
}
