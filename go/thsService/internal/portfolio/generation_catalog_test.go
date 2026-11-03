package portfolio

import (
	"context"
	"fmt"
	"reflect"
	"testing"
	"ths-service/internal/market"
	"time"
)

func TestCatalogDiversityAndEditableGeneration(t *testing.T) {
	now := time.Date(2026, 11, 3, 16, 0, 0, 0, beijing)
	for category := range categoryLabels {
		t.Run(category, func(t *testing.T) {
			seen := map[string]bool{}
			for range 20 {
				quotes := catalogQuotes(category, now)
				if len(quotes.Items) < 16 {
					t.Fatalf("catalog too small: %d", len(quotes.Items))
				}
				data, e := generateCategory(7, 20000000, quotes, now)
				if e != nil {
					t.Fatal(e)
				}
				if len(data.Items) < 8 || len(data.Items) > 12 {
					t.Fatal("wrong holdings count")
				}
				ids := map[uint64]bool{}
				for _, item := range data.Items {
					seen[item.Position.Symbol] = true
					if len(item.Trades) < 40 || len(item.Trades) > 50 {
						t.Fatal("wrong trade count")
					}
					for _, trade := range item.Trades {
						if ids[trade.ID] {
							t.Fatal("duplicate trade id")
						}
						ids[trade.ID] = true
					}
				}
				edited, e := NormalizeAdminHoldings(7, category, AdminHoldingWrite{TotalAssets: data.Account.TotalAssets, Items: data.Items}, now)
				if e != nil {
					t.Fatalf("generated portfolio cannot be edited: %v", e)
				}
				for i, item := range data.Items {
					p := edited.Items[i].Position
					if p.Quantity != item.Position.Quantity || p.CostPrice != item.Position.CostPrice || p.Profit != item.Position.Profit || p.MarketValue != item.Position.MarketValue {
						t.Fatalf("normalization changed amounts: %+v / %+v", p, item.Position)
					}
				}
			}
			if len(seen) <= 12 {
				t.Fatalf("selection did not vary enough: %d", len(seen))
			}
		})
	}
}

func TestCatalogEveryTradeCountAndAssetBounds(t *testing.T) {
	now := time.Date(2026, 11, 3, 16, 0, 0, 0, beijing)
	for category := range categoryLabels {
		for _, assets := range []int64{1000000, 20000000, 100000000000} {
			data, e := generateCategory(1, assets, catalogQuotes(category, now), now)
			if e == ErrInsufficientQuotes && assets == 1000000 {
				continue
			}
			if e != nil {
				t.Fatalf("%s %d: %v", category, assets, e)
			}
			if _, e := NormalizeAdminHoldings(1, category, AdminHoldingWrite{TotalAssets: data.Account.TotalAssets, Items: data.Items}, now); e != nil {
				t.Fatalf("%s %d not editable: %v", category, assets, e)
			}
		}
	}
	p := makeCategoryHolding(1, "test", market.QuoteItem{Symbol: "600036.SH", Name: "招商银行", LatestPrice: "40", Currency: "CNY"}, "a_stock", 1000000)
	for target := 40; target <= 50; target++ {
		trades := detailTradesCount(p, nil, 100, target, now)
		if len(trades) != target {
			t.Fatal(target, len(trades))
		}
		if _, e := NormalizeAdminHoldings(1, "a_stock", AdminHoldingWrite{TotalAssets: "200000", Items: []HoldingItem{{Position: p, Trades: trades}}}, now); e != nil {
			t.Fatal(target, e)
		}
	}
}

type catalogQuoteSource struct{ data market.QuoteListData }

func (q catalogQuoteSource) List(context.Context, string) (market.QuoteListData, error) {
	return q.data, nil
}
func TestCatalogOverlaysQuotesWithoutMutatingMarketData(t *testing.T) {
	now := time.Now().In(beijing)
	supplied := market.QuoteListData{Category: "a_stock", Items: []market.QuoteItem{{Symbol: "600036.SH", Name: "招商银行", Currency: "CNY", LatestPrice: "39.1234", DataStatus: "live"}}}
	original := append([]market.QuoteItem(nil), supplied.Items...)
	s := &Service{quotes: catalogQuoteSource{supplied}}
	q, e := s.generationQuotes(t.Context(), "a_stock", now)
	if e != nil {
		t.Fatal(e)
	}
	for _, item := range q.Items {
		if item.Symbol == "600036.SH" && item.LatestPrice != "39.1234" {
			t.Fatal("quote not reused")
		}
	}
	if !reflect.DeepEqual(supplied.Items, original) {
		t.Fatal("market response mutated")
	}
	t.Log(fmt.Sprintf("A-share private pool: %d", len(q.Items)))
}
