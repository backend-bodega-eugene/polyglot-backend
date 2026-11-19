package portfolio

import (
	"context"
	"crypto/rand"
	"encoding/json"
	"errors"
	"fmt"
	"math/big"
	"regexp"
	"sort"
	"strings"
	"time"

	"gorm.io/gorm"
)

var ErrHoldingConflict = errors.New("数据已被修改，请刷新后重新编辑")
var ErrHoldingValidation = errors.New("持仓数据无效")

type AdminHoldingRow struct {
	UserID        uint64    `json:"userId"`
	RealName      string    `json:"realName"`
	Category      string    `json:"category"`
	Generation    string    `json:"generation"`
	TotalAssets   string    `json:"totalAssets"`
	CreatedAt     time.Time `json:"updatedAt"`
	PositionCount int       `json:"positionCount"`
}
type AdminHoldingList struct {
	Items    []AdminHoldingRow `json:"items"`
	Total    int64             `json:"total"`
	Page     int               `json:"page"`
	PageSize int               `json:"pageSize"`
}
type AdminHoldingWrite struct {
	PositionCount      *int          `json:"positionCount,omitempty"`     // 仅后台生成：股票数量1至50，省略默认10；受分类候选池和资产限制。
	TradesPerPosition  *int          `json:"tradesPerPosition,omitempty"` // 仅后台生成：每只股票交易记录3至500条，省略默认30。
	ExpectedGeneration string        `json:"expectedGeneration"`
	TotalAssets        string        `json:"totalAssets"`
	Items              []HoldingItem `json:"items"`
}
type HoldingAudit struct {
	ID            uint64 `gorm:"primaryKey"`
	AdminID       uint64
	UserID        uint64
	Category      string
	Action        string
	BeforePayload *string
	AfterPayload  *string
	CreatedAt     time.Time
}

func (HoldingAudit) TableName() string { return "admin_holding_audits" }

func (s *Service) AdminList(ctx context.Context, userID uint64, category string, page, size int) (AdminHoldingList, error) {
	result := AdminHoldingList{Items: []AdminHoldingRow{}, Page: page, PageSize: size}
	q := s.repository.db.WithContext(ctx).Table("demo_category_holdings AS h").Joins("JOIN users u ON u.id = h.user_id AND u.deleted_at IS NULL")
	if userID != 0 {
		q = q.Where("h.user_id = ?", userID)
	}
	if category != "" {
		if _, ok := categoryLabels[category]; !ok {
			return result, invalid("分类无效")
		}
		q = q.Where("h.category = ?", category)
	}
	if err := q.Count(&result.Total).Error; err != nil {
		return result, err
	}
	err := q.Select("h.user_id,u.real_name,h.category,h.generation,h.total_assets,h.created_at,JSON_LENGTH(h.payload, '$.items') AS position_count").Order("h.created_at DESC, h.user_id ASC, h.category ASC").Offset((page - 1) * size).Limit(size).Scan(&result.Items).Error
	return result, err
}
func (s *Service) AdminGet(ctx context.Context, userID uint64, category string) (HoldingsData, error) {
	var row CategorySnapshot
	if err := s.repository.db.WithContext(ctx).Where("user_id = ? AND category = ?", userID, category).Take(&row).Error; err != nil {
		return HoldingsData{}, err
	}
	var data HoldingsData
	err := json.Unmarshal([]byte(row.Payload), &data)
	return data, err
}

// AdminWrite serializes edits with App generation using the same user-row lock.
// expectedGeneration is an optimistic concurrency token; empty is create-only.
func (s *Service) AdminWrite(ctx context.Context, adminID, userID uint64, category string, req AdminHoldingWrite, action string) (HoldingsData, error) {
	var data HoldingsData
	if _, ok := categoryLabels[category]; !ok {
		return data, invalid("分类无效")
	}
	err := s.repository.withUser(ctx, userID, func(tx *gorm.DB) error {
		var old CategorySnapshot
		err := tx.Where("user_id = ? AND category = ?", userID, category).Take(&old).Error
		if err != nil && !errors.Is(err, gorm.ErrRecordNotFound) {
			return err
		}
		exists := err == nil
		if (exists && req.ExpectedGeneration != old.Generation) || (!exists && req.ExpectedGeneration != "") {
			return ErrHoldingConflict
		}
		if action == "delete" {
			if !exists {
				return gorm.ErrRecordNotFound
			}
			if err := tx.Where("user_id = ? AND category = ?", userID, category).Delete(&CategorySnapshot{}).Error; err != nil {
				return err
			}
			return tx.Create(&HoldingAudit{AdminID: adminID, UserID: userID, Category: category, Action: action, BeforePayload: &old.Payload}).Error
		}
		if action == "generate" {
			count, trades := 10, 30
			if req.PositionCount != nil {
				count = *req.PositionCount
			}
			if req.TradesPerPosition != nil {
				trades = *req.TradesPerPosition
			}
			if count < 1 || count > 50 || trades < 3 || trades > 500 {
				return invalid("股票数量须为1至50，每只交易记录须为3至500条")
			}
			assets, err := parseAssets(req.TotalAssets)
			if err != nil {
				return err
			}
			quotes, err := s.generationQuotes(ctx, category, time.Now().In(beijing))
			if err != nil {
				return err
			}
			data, err = generateCategoryCounts(userID, assets, quotes, time.Now().In(beijing), count, trades)
			if err != nil {
				return err
			}
		} else {
			var err error
			data, err = NormalizeAdminHoldings(userID, category, req, time.Now().In(beijing))
			if err != nil {
				return err
			}
		}
		payload, err := json.Marshal(data)
		if err != nil {
			return err
		}
		row := CategorySnapshot{UserID: userID, Category: category, Generation: data.Account.Generation, TotalAssets: data.Account.TotalAssets, Payload: string(payload), CreatedAt: time.Now()}
		if exists {
			if err := tx.Where("user_id = ? AND category = ?", userID, category).Delete(&CategorySnapshot{}).Error; err != nil {
				return err
			}
		}
		if err := tx.Create(&row).Error; err != nil {
			return err
		}
		var before *string
		if exists {
			before = &old.Payload
		}
		auditAction := action
		if action == "save" {
			if exists {
				auditAction = "update"
			} else {
				auditAction = "create"
			}
		}
		return tx.Create(&HoldingAudit{AdminID: adminID, UserID: userID, Category: category, Action: auditAction, BeforePayload: before, AfterPayload: &row.Payload}).Error
	})
	return data, err
}

var positiveDecimal = regexp.MustCompile(`^\d{1,9}(\.\d{1,4})?$`)
var signedMoney = regexp.MustCompile(`^-?\d{1,10}(\.\d{1,2})?$`)

func invalid(message string) error { return fmt.Errorf("%w：%s", ErrHoldingValidation, message) }

// NormalizeAdminHoldings ignores client-supplied derived amounts. Trades determine
// quantity, moving-average cost, fees and realized P/L; stock prices determine valuation.
func NormalizeAdminHoldings(userID uint64, category string, req AdminHoldingWrite, now time.Time) (HoldingsData, error) {
	data := HoldingsData{}
	label, ok := categoryLabels[category]
	if !ok {
		return data, invalid("分类无效")
	}
	assets, err := parseAssets(req.TotalAssets)
	if err != nil {
		return data, err
	}
	if len(req.Items) > 50 {
		return data, invalid("最多50个持仓")
	}
	data = HoldingsData{Simulated: true, Category: category, CategoryName: label, ExchangeRateMode: "demo_fixed", Account: Account{UserID: userID, Generation: rand.Text(), Currency: "CNY", TotalAssets: money(assets), GeneratedAt: now}, Items: make([]HoldingItem, 0, len(req.Items))}
	seen := map[string]bool{}
	positions := []Position{}
	nextTradeID := uint64(1)
	for i, item := range req.Items {
		p := item.Position
		p.Symbol = strings.TrimSpace(p.Symbol)
		p.Name = strings.TrimSpace(p.Name)
		if p.Symbol == "" || len(p.Symbol) > 32 || p.Name == "" || len([]rune(p.Name)) > 64 || seen[p.Symbol] {
			return data, invalid(fmt.Sprintf("第%d个股票代码/名称无效或重复", i+1))
		}
		seen[p.Symbol] = true
		latest, valid := priceUnits(p.LatestPrice)
		if !positiveDecimal.MatchString(p.LatestPrice) || !valid || latest <= 0 {
			return data, invalid(p.Symbol + "现价必须大于0，最多4位小数")
		}
		rate := demoRate(p.Currency)
		if rate == 0 {
			return data, invalid(p.Symbol + "币种仅支持CNY/HKD/USD")
		}
		if !signedMoney.MatchString(p.DailyProfit) {
			return data, invalid(p.Symbol + "当日收益最多2位小数")
		}
		day := decimalCents(p.DailyProfit)
		if len(item.Trades) == 0 || len(item.Trades) > 500 {
			return data, invalid(p.Symbol + "需要1至500条交易")
		}
		trades := append([]Trade(nil), item.Trades...)
		// Generated historical pairs can share a timestamp after weekend adjustment.
		// Buys precede sells at the same instant; IDs stabilize repeated saves.
		sort.SliceStable(trades, func(i, j int) bool {
			if trades[i].TradedAt.Equal(trades[j].TradedAt) {
				if trades[i].Side != trades[j].Side {
					return trades[i].Side == "buy"
				}
				return trades[i].ID < trades[j].ID
			}
			return trades[i].TradedAt.Before(trades[j].TradedAt)
		})
		quantity, costUnits, realized, fees, turnover := int64(0), int64(0), int64(0), int64(0), int64(0)
		for j := range trades {
			t := &trades[j]
			price, valid := priceUnits(t.Price)
			if !positiveDecimal.MatchString(t.Price) || !valid || price <= 0 || t.Quantity <= 0 || t.Quantity > 10000000 || t.Quantity%lotSize(category) != 0 || t.TradedAt.IsZero() || t.TradedAt.After(now) {
				return data, invalid(fmt.Sprintf("%s第%d条交易的价格、数量或时间无效（数量须符合最小单位%d）", p.Symbol, j+1, lotSize(category)))
			}
			if !signedMoney.MatchString(t.Fees) || strings.HasPrefix(t.Fees, "-") {
				return data, invalid(p.Symbol + "手续费必须为非负金额")
			}
			fee := decimalCents(t.Fees)
			fees += fee
			turnover += value(price, t.Quantity) + fee
			if turnover > 100000000000 {
				return data, invalid(p.Symbol + "累计成交金额超过上限")
			}
			if t.Side == "buy" {
				if quantity+t.Quantity > 10000000 {
					return data, invalid(p.Symbol + "持仓数量超过上限")
				}
				costUnits += price*t.Quantity + fee*100
				quantity += t.Quantity
			} else if t.Side == "sell" {
				if t.Quantity > quantity {
					return data, invalid(p.Symbol + "存在先卖后买或卖出超过持仓")
				}
				removedUnits := new(big.Int).Mul(big.NewInt(costUnits), big.NewInt(t.Quantity))
				removed := removedUnits.Quo(removedUnits, big.NewInt(quantity)).Int64()
				realized += value(price, t.Quantity) - fee - (removed+50)/100
				costUnits -= removed
				quantity -= t.Quantity
			} else {
				return data, invalid(p.Symbol + "交易方向只能是buy或sell")
			}
			t.ID = nextTradeID
			nextTradeID++
			t.UserID = userID
			t.Generation = data.Account.Generation
			t.Symbol = p.Symbol
			t.Name = p.Name
			t.Currency = p.Currency
			t.Price = fixed(price, 4)
			t.Amount = money(value(price, t.Quantity))
			t.Fees = money(fee)
		}
		if quantity <= 0 || p.Quantity != quantity {
			return data, invalid(p.Symbol + "持仓数量必须大于0且等于买入减卖出数量")
		}
		if p.AvailableQuantity < 0 || p.AvailableQuantity > quantity {
			return data, invalid(p.Symbol + "可用数量须在0与持仓数量之间")
		}
		if p.QuoteTime.IsZero() {
			p.QuoteTime = now
		}
		if p.QuoteTime.After(now) {
			return data, invalid(p.Symbol + "估值时间不能在未来")
		}
		if value(latest, quantity) > 100000000000 {
			return data, invalid(p.Symbol + "市值超过上限")
		}
		mv := toCNY(value(latest, quantity), rate)
		if day >= mv {
			return data, invalid(p.Symbol + "当日收益须小于当前市值")
		}
		cost := toCNY((costUnits+50)/100, rate)
		p.ID = uint64(i + 1)
		p.UserID = userID
		p.Generation = data.Account.Generation
		p.CostPrice = fixed((costUnits+quantity/2)/quantity, 4)
		p.LatestPrice = fixed(latest, 4)
		p.ExchangeRate = fixed(rate, 6)
		p.MarketValue = money(mv)
		p.Profit = money(mv - cost)
		p.ProfitPercent = ratio(mv-cost, cost)
		p.DailyProfit = money(day)
		p.DailyProfitPercent = ratio(day, mv-day)
		if realized < 0 {
			p.RealizedProfit = money(-toCNY(-realized, rate))
		} else {
			p.RealizedProfit = money(toCNY(realized, rate))
		}
		p.TransactionFees = money(toCNY(fees, rate))
		p.HoldingDays = holdingDays(trades, now)
		p.DataStatus = "manual"
		sort.SliceStable(trades, func(i, j int) bool { return trades[i].TradedAt.After(trades[j].TradedAt) })
		positions = append(positions, p)
		data.Items = append(data.Items, HoldingItem{Position: p, PositionRatio: ratio(mv, assets), Trades: trades})
	}
	recalculate(&data.Account, positions)
	if decimalCents(data.Account.AvailableCash) < 0 {
		return data, invalid("持仓总市值超过总资产，请增加总资产或减少持仓")
	}
	return data, nil
}
