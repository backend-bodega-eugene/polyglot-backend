package portfolio

import (
	"crypto/rand"
	"math/big"
	"strings"
	"time"

	"gorm.io/gorm"
)

func scaleAmount(amount, numerator, denominator int64) int64 {
	if denominator <= 0 {
		return 0
	}
	n := new(big.Int).Mul(big.NewInt(amount), big.NewInt(numerator))
	return n.Quo(n, big.NewInt(denominator)).Int64()
}

func positionLot(p Position) int64 {
	if p.Symbol == "000001.SH" || strings.HasPrefix(p.Symbol, "399") {
		return 1
	}
	if strings.HasSuffix(p.Symbol, ".SH") || strings.HasSuffix(p.Symbol, ".SZ") {
		if strings.HasPrefix(p.Symbol, "11") || strings.HasPrefix(p.Symbol, "12") {
			return 10
		}
		return 100
	}
	return 1
}

// Rebuild quantities and history while preserving the existing instrument list,
// names, quote snapshots, currencies and cost prices across every category.
func rebuild(account Account, positions []Position, assets int64, now time.Time) (Data, []Trade, error) {
	previousAssets := decimalCents(account.TotalAssets)
	minimum, weight := int64(0), int64(0)
	for _, p := range positions {
		if p.Quantity > 0 {
			price, _ := priceUnits(p.LatestPrice)
			minimum += toCNY(value(price, positionLot(p)), demoRate(p.Currency))
			weight += decimalCents(p.MarketValue)
		}
	}
	if minimum > assets {
		return Data{}, nil, ErrInsufficientQuotes
	}
	budget := max(minimum, scaleAmount(decimalCents(account.TotalMarketValue), assets, previousAssets))
	extra := budget - minimum
	account.Generation = rand.Text()
	account.TotalAssets = money(assets)
	account.GeneratedAt = now.Truncate(time.Millisecond)
	data := Data{Simulated: true, Account: account, Positions: make([]Position, 0, len(positions))}
	trades := make([]Trade, 0, len(positions)*15)
	for _, old := range positions {
		p := old
		p.ID = 0
		p.Generation = account.Generation
		p.CreatedAt = time.Time{}
		lot := positionLot(p)
		price, _ := priceUnits(p.LatestPrice)
		cost, _ := priceUnits(p.CostPrice)
		rate := demoRate(p.Currency)
		p.Quantity = 0
		if old.Quantity > 0 {
			lotCost := toCNY(value(price, lot), rate)
			allocation := lotCost + scaleAmount(extra, decimalCents(old.MarketValue), weight)
			p.Quantity = allocation / lotCost * lot
			for toCNY(value(price, p.Quantity), rate) > allocation {
				p.Quantity -= lot
			}
		}
		p.AvailableQuantity = p.Quantity
		mv := toCNY(value(price, p.Quantity), rate)
		cv := toCNY(value(cost, p.Quantity), rate)
		p.MarketValue = money(mv)
		p.Profit = money(mv - cv)
		p.ProfitPercent = ratio(mv-cv, cv)
		day := scaleAmount(decimalCents(old.DailyProfit), p.Quantity, old.Quantity)
		p.DailyProfit = money(day)
		p.DailyProfitPercent = ratio(day, mv-day)
		p.RealizedProfit = "0.00"
		p.TransactionFees = "0.00"
		history := detailTrades(p, nil, lot, assets)
		p.HoldingDays = holdingDays(history, now)
		data.Positions = append(data.Positions, p)
		trades = append(trades, history...)
	}
	recalculate(&data.Account, data.Positions)
	return data, trades, nil
}

func rebuildExisting(tx *gorm.DB, account Account, assets int64) (Data, []Trade, error) {
	var positions []Position
	if err := tx.Where("user_id = ? AND generation = ?", account.UserID, account.Generation).Order("id ASC").Find(&positions).Error; err != nil {
		return Data{}, nil, err
	}
	return rebuild(account, positions, assets, time.Now().In(beijing))
}

func activePositions(positions []Position) []Position {
	result := make([]Position, 0, len(positions))
	for _, p := range positions {
		if p.Quantity > 0 {
			result = append(result, p)
		}
	}
	return result
}
