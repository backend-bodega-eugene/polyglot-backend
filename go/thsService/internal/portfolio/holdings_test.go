package portfolio

import (
	"testing"
	"time"

	"ths-service/internal/market"
)

func TestRandomAssetsRange(t *testing.T) {
	seen := map[int64]bool{}
	for range 1000 {
		amount := randomAssets()
		if amount < 20000000 || amount > 100000000 || amount%100 != 0 {
			t.Fatalf("invalid random assets: %d", amount)
		}
		seen[amount] = true
	}
	if len(seen) < 2 {
		t.Fatal("random assets did not vary")
	}
}

func TestLinkedHoldingBudgetsAndTradeReconciliation(t *testing.T) {
	for _, test := range []struct {
		category, currency string
		budget             int64
	}{
		{"a_stock", "CNY", 500000}, {"hk_stock", "HKD", 500000}, {"us_stock", "USD", 500000}, {"etf", "CNY", 500000}, {"future", "CNY", 500000}, {"gold", "USD", 500000}, {"convertible_bond", "CNY", 500000}, {"global", "USD", 0},
	} {
		p := makeHolding(1, "generation", market.QuoteItem{Symbol: "TEST", Name: "test", LatestPrice: "31.1250", Currency: test.currency, ChangeAmount: "0.1250", QuoteTime: time.Now()}, test.category, test.budget)
		if decimalCents(p.MarketValue) > test.budget || p.Quantity%lotSize(test.category) != 0 {
			t.Fatalf("exceeded budget or invalid lot: %+v", p)
		}
		trades := detailTrades(p, nil, lotSize(test.category), 20000000)
		if len(trades) != 15 {
			t.Fatalf("wrong history count %d", len(trades))
		}
		net, cost := int64(0), int64(0)
		for _, trade := range trades {
			quantity := trade.Quantity
			amount := decimalCents(trade.Amount)
			if trade.Side == "sell" {
				quantity = -quantity
				amount = -amount
			}
			net += quantity
			cost += amount
		}
		if net != p.Quantity {
			t.Fatalf("trade net %d != quantity %d", net, p.Quantity)
		}
		if toCNY(cost, demoRate(p.Currency))+decimalCents(p.Profit) != decimalCents(p.MarketValue) {
			t.Fatalf("cost + profit != market value: %+v", p)
		}
		account := Account{TotalAssets: "200000.00"}
		recalculate(&account, []Position{p})
		if decimalCents(account.AvailableCash)+decimalCents(account.TotalMarketValue) != 20000000 {
			t.Fatal("account imbalance")
		}
	}
}

func TestExistingTradeIsPreservedAndEnriched(t *testing.T) {
	p := makeHolding(4, "old", market.QuoteItem{Symbol: "600036.SH", Currency: "CNY", LatestPrice: "40.0000", ChangeAmount: "0.1", QuoteTime: time.Now()}, "a_stock", 500000)
	cost, _ := priceUnits(p.CostPrice)
	old := Trade{UserID: 4, Generation: "old", Symbol: p.Symbol, Side: "buy", Quantity: p.Quantity, Price: p.CostPrice, Amount: money(value(cost, p.Quantity)), TradedAt: time.Now().AddDate(0, 0, -10)}
	trades := detailTrades(p, []Trade{old}, 100, 20000000)
	if len(trades) != 14 {
		t.Fatal("must append exactly fourteen records")
	}
	net := int64(0)
	for _, trade := range trades {
		if !trade.TradedAt.Before(old.TradedAt) {
			t.Fatal("round trips must precede opening position")
		}
		if trade.Side == "buy" {
			net += trade.Quantity
		} else {
			net -= trade.Quantity
		}
	}
	if net != 0 {
		t.Fatal("history enrichment changed holdings")
	}
}

func TestHoldingDaysExcludesEarlierClosedPositions(t *testing.T) {
	now := time.Date(2026, 10, 15, 12, 0, 0, 0, beijing)
	trades := []Trade{
		{ID: 1, Side: "buy", Quantity: 100, TradedAt: now.AddDate(0, 0, -30)},
		{ID: 2, Side: "sell", Quantity: 100, TradedAt: now.AddDate(0, 0, -25)},
		{ID: 3, Side: "buy", Quantity: 200, TradedAt: now.AddDate(0, 0, -3)},
	}
	if got := holdingDays(trades, now); got != 3 {
		t.Fatalf("got %d days, expected 3", got)
	}
	trades = append(trades, Trade{ID: 4, Side: "sell", Quantity: 200, TradedAt: now})
	if got := holdingDays(trades, now); got != 0 {
		t.Fatalf("closed position has %d holding days", got)
	}
}
