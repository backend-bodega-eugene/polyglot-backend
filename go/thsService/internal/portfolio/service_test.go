package portfolio

import (
	"fmt"
	"math/big"
	"testing"
	"time"

	"ths-service/internal/market"
)

func cents(t *testing.T, s string) int64 {
	t.Helper()
	r, ok := new(big.Rat).SetString(s)
	if !ok {
		t.Fatalf("invalid money %q", s)
	}
	r.Mul(r, big.NewRat(100, 1))
	if !r.IsInt() {
		t.Fatalf("fractional cents %q", s)
	}
	return r.Num().Int64()
}

func TestGenerateBalancesAndTrades(t *testing.T) {
	quotes := make([]market.QuoteItem, 10)
	for i := range quotes {
		quotes[i] = market.QuoteItem{Symbol: fmt.Sprintf("%06d.SH", 600000+i), Name: "模拟股票", Currency: "CNY", Market: "CN", LatestPrice: fmt.Sprintf("%d.12", 18+i*10), ChangeAmount: "-0.12", QuoteTime: time.Now(), DataStatus: "live"}
	}
	for _, assets := range []int64{1000000, 20000000, 100000000000} {
		for range 100 {
			data, trades, err := generate(7, assets, quotes, time.Now().In(beijing))
			if err != nil {
				t.Fatal(err)
			}
			if len(data.Positions) < 3 || len(data.Positions) > 5 || len(trades) != 15*len(data.Positions) {
				t.Fatal("invalid position/trade count")
			}
			marketValue, profit, daily := int64(0), int64(0), int64(0)
			for _, p := range data.Positions {
				if p.Quantity < 100 || p.Quantity%100 != 0 || p.Quantity != p.AvailableQuantity {
					t.Fatalf("invalid lot %+v", p)
				}
				count, net, costValue := 0, int64(0), int64(0)
				for _, trade := range trades {
					if trade.Symbol != p.Symbol {
						continue
					}
					count++
					if trade.Side == "buy" {
						net += trade.Quantity
						costValue += cents(t, trade.Amount)
					} else {
						net -= trade.Quantity
						costValue -= cents(t, trade.Amount)
					}
					if trade.UserID != 7 || trade.Generation != data.Account.Generation || !trade.TradedAt.Before(data.Account.GeneratedAt) || trade.TradedAt.Weekday() == time.Saturday || trade.TradedAt.Weekday() == time.Sunday {
						t.Fatal("invalid linked trade")
					}
				}
				price, _ := priceUnits(p.LatestPrice)
				cost, _ := priceUnits(p.CostPrice)
				if value(price, p.Quantity) != cents(t, p.MarketValue) || value(cost, p.Quantity) != costValue || count != 15 || net != p.Quantity {
					t.Fatal("price and amount disagree")
				}
				if cents(t, p.MarketValue)-costValue != cents(t, p.Profit) {
					t.Fatal("profit and cost disagree")
				}
				marketValue += cents(t, p.MarketValue)
				profit += cents(t, p.Profit)
				daily += cents(t, p.DailyProfit)
			}
			if marketValue+cents(t, data.Account.AvailableCash) != assets || marketValue != cents(t, data.Account.TotalMarketValue) || profit != cents(t, data.Account.FloatingProfit) || daily != cents(t, data.Account.DailyProfit) {
				t.Fatal("account totals disagree")
			}
			if data.Account.AvailableCash != data.Account.WithdrawableCash || cents(t, data.Account.AvailableCash) < 0 {
				t.Fatal("invalid cash")
			}
		}
	}
}

func TestAssetsValidation(t *testing.T) {
	for _, s := range []string{"", "0", "-1", "9999.99", "1000000000.01", "1e6", "NaN", "10000.001", "10000x"} {
		if _, err := parseAssets(s); err == nil {
			t.Errorf("accepted %q", s)
		}
	}
	for _, s := range []string{"10000", "200000.50", "1000000000.00"} {
		if _, err := parseAssets(s); err != nil {
			t.Errorf("rejected %q: %v", s, err)
		}
	}
}

func TestInsufficientQuotes(t *testing.T) {
	if _, _, err := generate(1, 1000000, nil, time.Now()); err != ErrInsufficientQuotes {
		t.Fatal(err)
	}
}
