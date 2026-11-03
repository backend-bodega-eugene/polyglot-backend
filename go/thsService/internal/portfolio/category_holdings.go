package portfolio

import (
	"context"
	"crypto/rand"
	"encoding/json"
	"errors"
	randv2 "math/rand/v2"
	"sort"
	"time"

	"gorm.io/gorm"
	"ths-service/internal/market"
)

type CategorySnapshot struct {
	UserID      uint64 `gorm:"primaryKey"`
	Category    string `gorm:"primaryKey"`
	Generation  string
	TotalAssets string
	Payload     string `gorm:"type:json"`
	CreatedAt   time.Time
}

func (CategorySnapshot) TableName() string { return "demo_category_holdings" }

var categoryLabels = map[string]string{
	"global": "全球", "a_stock": "A股", "hk_stock": "港股", "us_stock": "美股", "etf": "ETF", "future": "期货", "gold": "黄金", "convertible_bond": "可转债", "other": "其他",
}

const (
	minGeneratedHoldings = 8
	maxGeneratedHoldings = 12
	minGeneratedTrades   = 40
	maxGeneratedTrades   = 50
)

func (s *Service) Holdings(ctx context.Context, userID uint64, category string, totalAssets *string) (HoldingsData, error) {
	if _, ok := categoryLabels[category]; !ok {
		return HoldingsData{}, market.ErrInvalidCategory
	}
	var assets int64
	if totalAssets != nil {
		var err error
		assets, err = parseAssets(*totalAssets)
		if err != nil {
			return HoldingsData{}, err
		}
	}
	var data HoldingsData
	err := s.repository.withUser(ctx, userID, func(tx *gorm.DB) error {
		var snapshot CategorySnapshot
		err := tx.Where("user_id = ? AND category = ?", userID, category).Take(&snapshot).Error
		if err != nil && !errors.Is(err, gorm.ErrRecordNotFound) {
			return err
		}
		if err == nil && totalAssets == nil {
			return json.Unmarshal([]byte(snapshot.Payload), &data)
		}
		if totalAssets == nil {
			assets = randomAssets()
		}
		quotes, err := s.generationQuotes(ctx, category, time.Now().In(beijing))
		if err != nil {
			return err
		}
		data, err = generateCategory(userID, assets, quotes, time.Now().In(beijing))
		if err != nil {
			return err
		}
		payload, err := json.Marshal(data)
		if err != nil {
			return err
		}
		// The complete category dataset is one row; delete and insert atomically.
		if err = tx.Where("user_id = ? AND category = ?", userID, category).Delete(&CategorySnapshot{}).Error; err != nil {
			return err
		}
		return tx.Create(&CategorySnapshot{UserID: userID, Category: category, Generation: data.Account.Generation, TotalAssets: data.Account.TotalAssets, Payload: string(payload)}).Error
	})
	return data, err
}

func generateCategory(userID uint64, assets int64, quotes market.QuoteListData, now time.Time) (HoldingsData, error) {
	type candidate struct {
		quote   market.QuoteItem
		minimum int64
	}
	lot := lotSize(quotes.Category)
	candidates := make([]candidate, 0, len(quotes.Items))
	seen := map[string]bool{}
	for _, q := range quotes.Items {
		price, ok := priceUnits(q.LatestPrice)
		rate := demoRate(q.Currency)
		if !ok || rate == 0 || seen[q.Symbol] {
			continue
		}
		minimum := toCNY(value(price, lot), rate)
		if minimum <= 0 {
			continue
		}
		seen[q.Symbol] = true
		candidates = append(candidates, candidate{q, minimum})
	}
	if len(candidates) < minGeneratedHoldings {
		return HoldingsData{}, ErrInsufficientQuotes
	}
	count := min(minGeneratedHoldings+randv2.IntN(maxGeneratedHoldings-minGeneratedHoldings+1), len(candidates))
	budget := assets * int64(55+randv2.IntN(16)) / 100
	randv2.Shuffle(len(candidates), func(i, j int) { candidates[i], candidates[j] = candidates[j], candidates[i] })
	minimum := func(n int) int64 {
		total := int64(0)
		for _, c := range candidates[:n] {
			total += c.minimum
		}
		return total
	}
	if minimum(count) > budget {
		sort.Slice(candidates, func(i, j int) bool { return candidates[i].minimum < candidates[j].minimum })
		for count > minGeneratedHoldings && minimum(count) > budget {
			count--
		}
		budget = max(budget, minimum(count))
	}
	if budget > assets {
		return HoldingsData{}, ErrInsufficientQuotes
	}
	// Re-shuffle after affordability calculations instead of always choosing the
	// cheapest names when a random combination is too expensive.
	randv2.Shuffle(len(candidates), func(i, j int) { candidates[i], candidates[j] = candidates[j], candidates[i] })
	selected := make([]candidate, 0, count)
	reserved := int64(0)
	for i, c := range candidates {
		need := count - len(selected) - 1
		if len(candidates)-i-1 < need {
			continue
		}
		tail := append([]candidate(nil), candidates[i+1:]...)
		sort.Slice(tail, func(i, j int) bool { return tail[i].minimum < tail[j].minimum })
		trial := reserved + c.minimum
		for j := 0; j < need; j++ {
			trial += tail[j].minimum
		}
		if trial <= budget {
			selected = append(selected, c)
			reserved += c.minimum
		}
		if len(selected) == count {
			break
		}
	}
	if len(selected) != count {
		return HoldingsData{}, ErrInsufficientQuotes
	}
	candidates = selected
	extra := budget - minimum(count)
	account := Account{UserID: userID, Generation: rand.Text(), Currency: "CNY", TotalAssets: money(assets), GeneratedAt: now.In(beijing).Truncate(time.Millisecond)}
	data := HoldingsData{Simulated: true, Category: quotes.Category, CategoryName: quotes.CategoryName, Account: account, Items: make([]HoldingItem, 0, count), ExchangeRateMode: "demo_fixed"}
	positions := make([]Position, 0, count)
	weights := make([]int64, count)
	remainingWeight := int64(0)
	for i := range weights {
		weights[i] = int64(1 + randv2.IntN(5))
		remainingWeight += weights[i]
	}
	for i, c := range candidates[:count] {
		allocation := c.minimum + extra*weights[i]/remainingWeight
		remainingWeight -= weights[i]
		p := makeCategoryHolding(userID, account.Generation, c.quote, quotes.Category, allocation)
		if p.Quantity <= 0 {
			return HoldingsData{}, ErrInsufficientQuotes
		}
		extra -= decimalCents(p.MarketValue) - c.minimum
		p.ID = uint64(i + 1)
		p.QuoteTime = p.QuoteTime.In(beijing).Truncate(time.Millisecond)
		target := minGeneratedTrades + randv2.IntN(maxGeneratedTrades-minGeneratedTrades+1)
		trades := detailTradesCount(p, nil, lot, target, now.AddDate(0, 0, -randv2.IntN(22)))
		if len(trades) != target {
			return HoldingsData{}, ErrInsufficientQuotes
		}
		for j := range trades {
			trades[j].ID = uint64(i*maxGeneratedTrades + j + 1)
			trades[j].TradedAt = trades[j].TradedAt.In(beijing)
		}
		p.HoldingDays = holdingDays(trades, now)
		sort.Slice(trades, func(i, j int) bool {
			if trades[i].TradedAt.Equal(trades[j].TradedAt) {
				return trades[i].ID > trades[j].ID
			}
			return trades[i].TradedAt.After(trades[j].TradedAt)
		})
		positions = append(positions, p)
		data.Items = append(data.Items, HoldingItem{Position: p, PositionRatio: ratio(decimalCents(p.MarketValue), assets), Trades: trades})
	}
	recalculate(&data.Account, positions)
	return data, nil
}
