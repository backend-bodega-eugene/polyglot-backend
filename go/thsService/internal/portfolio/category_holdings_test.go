package portfolio

import (
	"encoding/json"
	"fmt"
	"sort"
	"strings"
	"testing"
	"time"

	"ths-service/internal/market"
)

func TestCategoryGenerationOnlyFundedPositionsAndExpandedTrades(t *testing.T) {
	for category, name := range categoryLabels {
		quotes := market.QuoteListData{Category: category, CategoryName: name}
		for i := range 30 {
			currency := "CNY"
			if category == "hk_stock" {
				currency = "HKD"
			}
			if category == "us_stock" || category == "global" {
				currency = "USD"
			}
			quotes.Items = append(quotes.Items, market.QuoteItem{Symbol: fmt.Sprintf("STOCK%d", i), Name: fmt.Sprintf("标的%d", i), Currency: currency, LatestPrice: fmt.Sprintf("%d.12", 10+i*3), ChangeAmount: "0.125", QuoteTime: time.Now(), DataStatus: "live"})
		}
		for range 10 {
			data, err := generateCategory(7, 70000000, quotes, time.Now().In(beijing))
			if err != nil {
				t.Fatal(err)
			}
			if len(data.Items) < 8 || len(data.Items) > 12 {
				t.Fatalf("%s has %d positions", category, len(data.Items))
			}
			mv, profit, day := int64(0), int64(0), int64(0)
			seen := map[string]bool{}
			for _, item := range data.Items {
				p := item.Position
				if p.Quantity <= 0 || seen[p.Symbol] || len(item.Trades) < 40 || len(item.Trades) > 50 {
					t.Fatal("invalid position or trade count")
				}
				seen[p.Symbol] = true
				ordered := append([]Trade(nil), item.Trades...)
				sort.SliceStable(ordered, func(i, j int) bool {
					if ordered[i].TradedAt.Equal(ordered[j].TradedAt) {
						return ordered[i].ID < ordered[j].ID
					}
					return ordered[i].TradedAt.Before(ordered[j].TradedAt)
				})
				balance := int64(0)
				for _, trade := range ordered {
					if trade.Side == "buy" {
						balance += trade.Quantity
					} else {
						balance -= trade.Quantity
					}
					if balance < 0 || !trade.TradedAt.Before(data.Account.GeneratedAt) {
						t.Fatal("invalid generated trade chronology")
					}
				}
				net, cost := int64(0), int64(0)
				for _, trade := range item.Trades {
					if trade.Side == "buy" {
						net += trade.Quantity
						cost += decimalCents(trade.Amount)
					} else {
						net -= trade.Quantity
						cost -= decimalCents(trade.Amount)
					}
				}
				if net != p.Quantity || toCNY(cost, demoRate(p.Currency))+decimalCents(p.Profit) != decimalCents(p.MarketValue) {
					t.Fatal("trade reconciliation failed")
				}
				mv += decimalCents(p.MarketValue)
				profit += decimalCents(p.Profit)
				day += decimalCents(p.DailyProfit)
			}
			if mv != decimalCents(data.Account.TotalMarketValue) || profit != decimalCents(data.Account.FloatingProfit) || day != decimalCents(data.Account.DailyProfit) || mv+decimalCents(data.Account.AvailableCash) != 70000000 {
				t.Fatal("account totals disagree")
			}
			encoded, err := json.Marshal(data)
			if err != nil {
				t.Fatal(err)
			}
			if strings.Contains(string(encoded), `"quote":`) {
				t.Fatal("response contains quotes")
			}
			var read HoldingsData
			if err = json.Unmarshal(encoded, &read); err != nil {
				t.Fatal(err)
			}
			again, _ := json.Marshal(read)
			if string(encoded) != string(again) {
				t.Fatal("stored payload changes response")
			}
		}
	}
}

func TestCategoryInsufficientAssets(t *testing.T) {
	quotes := market.QuoteListData{Category: "a_stock"}
	for i := range 3 {
		quotes.Items = append(quotes.Items, market.QuoteItem{Symbol: fmt.Sprint(i), Currency: "CNY", LatestPrice: "1000"})
	}
	if _, err := generateCategory(1, 1000000, quotes, time.Now()); err != ErrInsufficientQuotes {
		t.Fatalf("expected insufficient assets, got %v", err)
	}
}
