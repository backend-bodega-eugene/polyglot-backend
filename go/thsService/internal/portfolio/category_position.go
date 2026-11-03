package portfolio

import (
	randv2 "math/rand/v2"
	"ths-service/internal/market"
)

func makeCategoryHolding(userID uint64, generation string, q market.QuoteItem, category string, budget int64) Position {
	price, _ := priceUnits(q.LatestPrice)
	// Keep generated datasets editable under the existing 10-million unit limit.
	p := makeHolding(userID, generation, q, category, budget)
	if p.Quantity > 10000000 {
		p = makeHolding(userID, generation, q, category, toCNY(value(price, 10000000), demoRate(q.Currency)))
	}
	cost := max(1, price*int64(80+randv2.IntN(36))/100)
	p.CostPrice = fixed(cost, 4)
	costValue := toCNY(value(cost, p.Quantity), demoRate(p.Currency))
	profit := decimalCents(p.MarketValue) - costValue
	p.Profit = money(profit)
	p.ProfitPercent = ratio(profit, costValue)
	return p
}
