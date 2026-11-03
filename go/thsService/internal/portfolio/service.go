package portfolio

import (
	"context"
	"crypto/rand"
	"errors"
	"math/big"
	randv2 "math/rand/v2"
	"regexp"
	"sort"
	"strconv"
	"time"

	"ths-service/internal/market"
)

var ErrInvalidAssets = errors.New("总资产须为 10000 到 1000000000 元之间的数字，最多两位小数")
var ErrInsufficientQuotes = errors.New("总资产或可用价格不足以生成或保留所需数量的整手持仓，请提高总资产后重试")
var amountPattern = regexp.MustCompile(`^[0-9]{1,10}(\.[0-9]{1,2})?$`)
var beijing = time.FixedZone("Asia/Shanghai", 8*60*60)

type QuoteSource interface {
	List(context.Context, string) (market.QuoteListData, error)
}
type Service struct {
	repository *Repository
	quotes     QuoteSource
}

func NewService(repository *Repository, quotes QuoteSource) *Service {
	return &Service{repository: repository, quotes: quotes}
}

func parseAssets(value string) (int64, error) {
	if !amountPattern.MatchString(value) {
		return 0, ErrInvalidAssets
	}
	r, _ := new(big.Rat).SetString(value)
	r.Mul(r, big.NewRat(100, 1))
	n := r.Num().Int64()
	if n < 1000000 || n > 100000000000 {
		return 0, ErrInvalidAssets
	}
	return n, nil
}

func (s *Service) Positions(ctx context.Context, userID uint64, totalAssets *string) (Data, error) {
	assets := int64(20000000)
	if totalAssets != nil {
		var err error
		assets, err = parseAssets(*totalAssets)
		if err != nil {
			return Data{}, err
		}
	}
	data, err := s.repository.GetOrGenerate(ctx, userID, totalAssets != nil, assets, func(amount int64) (Data, []Trade, error) {
		quotes, err := s.quotes.List(ctx, "a_stock")
		if err != nil {
			return Data{}, nil, err
		}
		return generate(userID, amount, quotes.Items, time.Now().In(beijing))
	})
	data.Account.GeneratedAt = data.Account.GeneratedAt.In(beijing).Truncate(time.Millisecond)
	for i := range data.Positions {
		data.Positions[i].QuoteTime = data.Positions[i].QuoteTime.In(beijing).Truncate(time.Millisecond)
	}
	return data, err
}

func (s *Service) Trades(ctx context.Context, userID uint64, page, pageSize int) (TradeList, error) {
	upgrade, err := s.repository.NeedsHoldingUpgrade(ctx, userID)
	if err != nil {
		return TradeList{}, err
	}
	if upgrade {
		if _, err = s.Positions(ctx, userID, nil); err != nil {
			return TradeList{}, err
		}
	}
	data, err := s.repository.Trades(ctx, userID, page, pageSize)
	for i := range data.Items {
		data.Items[i].TradedAt = data.Items[i].TradedAt.In(beijing)
	}
	return data, err
}

func money(cents int64) string { return fixed(cents, 2) }
func fixed(n int64, places int) string {
	sign := ""
	if n < 0 {
		sign = "-"
		n = -n
	}
	s := strconv.FormatInt(n, 10)
	for len(s) <= places {
		s = "0" + s
	}
	return sign + s[:len(s)-places] + "." + s[len(s)-places:]
}
func priceUnits(s string) (int64, bool) {
	r, ok := new(big.Rat).SetString(s)
	if !ok || r.Sign() <= 0 {
		return 0, false
	}
	r.Mul(r, big.NewRat(10000, 1))
	n := new(big.Int).Quo(r.Num(), r.Denom())
	if !n.IsInt64() || n.Int64() > 10000000000 {
		return 0, false
	}
	return n.Int64(), n.Sign() > 0
}
func ratio(n, d int64) string {
	if d == 0 {
		return "0.00"
	}
	return fixed(n*10000/d, 2)
}
func value(price, quantity int64) int64 { return (price*quantity + 50) / 100 }

func generate(userID uint64, assets int64, quotes []market.QuoteItem, now time.Time) (Data, []Trade, error) {
	now = now.Truncate(time.Millisecond)
	type candidate struct {
		quote market.QuoteItem
		price int64
	}
	var candidates []candidate
	seen := map[string]bool{}
	for _, q := range quotes {
		p, ok := priceUnits(q.LatestPrice)
		if ok && q.Currency == "CNY" && q.Market == "CN" && !seen[q.Symbol] {
			candidates = append(candidates, candidate{q, p})
			seen[q.Symbol] = true
		}
	}
	budget := assets * int64(55+randv2.IntN(16)) / 100
	// Reserve one affordable board lot for each position before distributing the remainder.
	sort.Slice(candidates, func(i, j int) bool { return candidates[i].price < candidates[j].price })
	count := 3 + randv2.IntN(3)
	if len(candidates) >= 3 {
		minimum := int64(0)
		for _, c := range candidates[:3] {
			minimum += value(c.price, 100)
		}
		if minimum <= assets && minimum > budget {
			budget = minimum
		}
	}
	for count >= 3 {
		if len(candidates) >= count {
			min := int64(0)
			for _, c := range candidates[:count] {
				min += value(c.price, 100)
			}
			if min <= budget {
				break
			}
		}
		count--
	}
	if count < 3 {
		return Data{}, nil, ErrInsufficientQuotes
	}
	randv2.Shuffle(len(candidates), func(i, j int) { candidates[i], candidates[j] = candidates[j], candidates[i] })
	// Keep selection random while ensuring all chosen minimum lots fit the budget.
	selected := make([]candidate, 0, count)
	for _, c := range candidates {
		trial := append(append([]candidate{}, selected...), c)
		remaining := make([]candidate, 0, len(candidates))
		for _, other := range candidates {
			found := false
			for _, x := range trial {
				if x.quote.Symbol == other.quote.Symbol {
					found = true
					break
				}
			}
			if !found {
				remaining = append(remaining, other)
			}
		}
		sort.Slice(remaining, func(i, j int) bool { return remaining[i].price < remaining[j].price })
		minimum := int64(0)
		for _, x := range trial {
			minimum += value(x.price, 100)
		}
		need := count - len(trial)
		for i := 0; i < need && i < len(remaining); i++ {
			minimum += value(remaining[i].price, 100)
		}
		if minimum <= budget {
			selected = append(selected, c)
		}
		if len(selected) == count {
			break
		}
	}
	generation := rand.Text()
	data := Data{Simulated: true, Account: Account{HoldingVersion: 1, UserID: userID, Generation: generation, Currency: "CNY", TotalAssets: money(assets), GeneratedAt: now}, Positions: make([]Position, 0, count)}
	trades := make([]Trade, 0, count)
	minimum := int64(0)
	for _, c := range selected {
		minimum += value(c.price, 100)
	}
	extra := budget - minimum
	totalValue, totalCost, daily := int64(0), int64(0), int64(0)
	for i, c := range selected {
		allocation := extra / int64(count-i)
		lotCost := value(c.price, 100)
		quantity := (1 + allocation/lotCost) * 100
		marketValue := value(c.price, quantity)
		extra -= marketValue - lotCost
		cost := c.price * int64(80+randv2.IntN(18)) / 100
		if cost < 1 {
			cost = 1
		}
		costValue := value(cost, quantity)
		profit := marketValue - costValue
		change := int64(0)
		if r, ok := new(big.Rat).SetString(c.quote.ChangeAmount); ok {
			r.Mul(r, big.NewRat(10000, 1))
			n := new(big.Int).Quo(r.Num(), r.Denom())
			if n.IsInt64() && n.Int64() > -c.price && n.Int64() < c.price {
				change = n.Int64()
			}
		}
		dailyProfit := marketValue - value(c.price-change, quantity)
		tradedAt := now.AddDate(0, 0, -(3 + randv2.IntN(25)))
		for tradedAt.Weekday() == time.Saturday || tradedAt.Weekday() == time.Sunday {
			tradedAt = tradedAt.AddDate(0, 0, -1)
		}
		tradedAt = time.Date(tradedAt.Year(), tradedAt.Month(), tradedAt.Day(), 10, randv2.IntN(60), randv2.IntN(60), 0, beijing)
		data.Positions = append(data.Positions, Position{UserID: userID, Generation: generation, Symbol: c.quote.Symbol, Name: c.quote.Name, MarketValue: money(marketValue), Profit: money(profit), ProfitPercent: ratio(profit, costValue), DailyProfit: money(dailyProfit), Quantity: quantity, AvailableQuantity: quantity, CostPrice: fixed(cost, 4), LatestPrice: fixed(c.price, 4), QuoteTime: c.quote.QuoteTime, DataStatus: c.quote.DataStatus, Currency: "CNY", ExchangeRate: "1.000000", RealizedProfit: "0.00", TransactionFees: "0.00", DailyProfitPercent: ratio(dailyProfit, marketValue-dailyProfit), HoldingDays: int(now.Sub(tradedAt).Hours() / 24)})
		opening := Trade{UserID: userID, Generation: generation, Symbol: c.quote.Symbol, Name: c.quote.Name, Side: "buy", Quantity: quantity, Price: fixed(cost, 4), Amount: money(costValue), TradedAt: tradedAt, Currency: "CNY", Fees: "0.00"}
		trades = append(trades, opening)
		trades = append(trades, detailTrades(data.Positions[len(data.Positions)-1], []Trade{opening}, 100, assets)...)
		totalValue += marketValue
		totalCost += costValue
		daily += dailyProfit
	}
	data.Account.TotalMarketValue = money(totalValue)
	data.Account.FloatingProfit = money(totalValue - totalCost)
	data.Account.DailyProfit = money(daily)
	data.Account.DailyProfitPercent = ratio(daily, assets-daily)
	data.Account.AvailableCash = money(assets - totalValue)
	data.Account.WithdrawableCash = data.Account.AvailableCash
	data.Account.PositionRatio = ratio(totalValue, assets)
	return data, trades, nil
}
