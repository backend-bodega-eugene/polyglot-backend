package portfolio

import (
	"testing"
	"time"
)

func TestExplicitGenerationCounts(t *testing.T) {
	now := time.Now().In(beijing)
	for _, pair := range [][2]int{{1, 3}, {5, 30}, {12, 51}, {16, 500}} {
		data, err := generateCategoryCounts(1, 100000000, catalogQuotes("a_stock", now), now, pair[0], pair[1])
		if err != nil {
			t.Fatal(err)
		}
		if len(data.Items) != pair[0] {
			t.Fatal("position count")
		}
		ids := map[uint64]bool{}
		for _, item := range data.Items {
			if len(item.Trades) != pair[1] {
				t.Fatal("trade count")
			}
			for _, trade := range item.Trades {
				if ids[trade.ID] {
					t.Fatal("duplicate trade ID")
				}
				ids[trade.ID] = true
			}
		}
		if _, err := NormalizeAdminHoldings(1, "a_stock", AdminHoldingWrite{TotalAssets: data.Account.TotalAssets, Items: data.Items}, now); err != nil {
			t.Fatal(err)
		}
	}
	quotes := catalogQuotes("gold", now)
	if _, err := generateCategoryCounts(1, 100000000, quotes, now, len(quotes.Items)+1, 30); err == nil {
		t.Fatal("silently reduced requested count")
	}
}
