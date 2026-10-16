package portfolio

import (
	"testing"
	"ths-service/internal/market"
	"time"
)

func TestRebuildPreservesInstrumentDataAndReplacesTrades(t *testing.T) {
	now := time.Now().In(beijing)
	positions := []Position{
		makeHolding(3, "old", market.QuoteItem{Symbol: "600036.SH", Name: "招商银行", Currency: "CNY", LatestPrice: "40.0000", ChangeAmount: "0.50", QuoteTime: now}, "a_stock", 5000000),
		makeHolding(3, "old", market.QuoteItem{Symbol: "AAPL.US", Name: "苹果", Currency: "USD", LatestPrice: "200.0000", ChangeAmount: "1", QuoteTime: now}, "us_stock", 5000000),
		makeHolding(3, "old", market.QuoteItem{Symbol: "600519.SH", Name: "贵州茅台", Currency: "CNY", LatestPrice: "1280.0000", ChangeAmount: "1", QuoteTime: now}, "a_stock", 0),
	}
	account := Account{UserID: 3, Generation: "old", Currency: "CNY", TotalAssets: "200000.00"}
	for _, assets := range []int64{1000000, 50000000, 100000000000} {
		data, trades, err := rebuild(account, positions, assets, now)
		if err != nil {
			t.Fatal(err)
		}
		if data.Account.Generation == "old" || len(data.Positions) != len(positions) {
			t.Fatal("generation or instrument count invalid")
		}
		for i, p := range data.Positions {
			old := positions[i]
			if p.Symbol != old.Symbol || p.Name != old.Name || p.LatestPrice != old.LatestPrice || p.CostPrice != old.CostPrice || p.Currency != old.Currency || !p.QuoteTime.Equal(old.QuoteTime) {
				t.Fatal("instrument data changed")
			}
			var net int64
			for _, trade := range trades {
				if trade.Generation != data.Account.Generation {
					t.Fatal("old generation trade")
				}
				if trade.Symbol == p.Symbol {
					if trade.Side == "buy" {
						net += trade.Quantity
					} else {
						net -= trade.Quantity
					}
				}
			}
			if net != p.Quantity {
				t.Fatal("trade net differs from position")
			}
		}
		if decimalCents(data.Account.TotalMarketValue)+decimalCents(data.Account.AvailableCash) != assets || decimalCents(data.Account.AvailableCash) < 0 {
			t.Fatal("invalid asset balance")
		}
	}
}

func TestRebuildRejectsAssetsBelowMinimumLots(t *testing.T) {
	now := time.Now().In(beijing)
	p := makeHolding(1, "old", market.QuoteItem{Symbol: "600519.SH", Currency: "CNY", LatestPrice: "1280", ChangeAmount: "0", QuoteTime: now}, "a_stock", 20000000)
	account := Account{UserID: 1, Generation: "old", HoldingVersion: 1, TotalAssets: "200000.00"}
	_, _, err := rebuild(account, []Position{p}, 1000000, now)
	if err != ErrInsufficientQuotes {
		t.Fatalf("expected insufficient assets, got %v", err)
	}
	if p.Quantity == 0 || account.Generation != "old" {
		t.Fatal("input snapshot mutated")
	}
}
