package market

import (
	"context"
	"errors"
	"fmt"
	"log/slog"
	"time"
)

var (
	ErrInvalidCategory = errors.New("invalid market category")
	ErrNoInstruments   = errors.New("no instruments configured")
)

var categoryNames = map[string]string{
	"global":           "全球",
	"a_stock":          "A股",
	"hk_stock":         "港股",
	"us_stock":         "美股",
	"etf":              "ETF",
	"future":           "期货",
	"gold":             "黄金",
	"convertible_bond": "可转债",
	"other":            "其他",
}

type Service struct {
	repository *Repository
	provider   *Provider
	cacheTTL   time.Duration
}

func NewService(repository *Repository, provider *Provider, cacheTTL time.Duration) *Service {
	return &Service{repository: repository, provider: provider, cacheTTL: cacheTTL}
}

func (s *Service) List(ctx context.Context, category string) (QuoteListData, error) {
	categoryName, ok := categoryNames[category]
	if !ok {
		return QuoteListData{}, ErrInvalidCategory
	}
	instruments, err := s.repository.ListInstruments(category)
	if err != nil {
		return QuoteListData{}, fmt.Errorf("list market instruments: %w", err)
	}
	if len(instruments) == 0 {
		return QuoteListData{}, ErrNoInstruments
	}

	ids := make([]uint64, 0, len(instruments))
	for _, instrument := range instruments {
		ids = append(ids, instrument.ID)
	}
	cached, err := s.repository.ListQuoteCaches(ids)
	if err != nil {
		return QuoteListData{}, fmt.Errorf("list quote cache: %w", err)
	}

	cutoff := time.Now().Add(-s.cacheTTL)
	needsRefresh := false
	for _, instrument := range instruments {
		quote, exists := cached[instrument.ID]
		if !exists || quote.UpdatedAt.Before(cutoff) {
			needsRefresh = true
			break
		}
	}
	if needsRefresh {
		s.refresh(ctx, instruments, cached)
	}

	items := make([]QuoteItem, 0, len(instruments))
	for _, instrument := range instruments {
		quote, exists := cached[instrument.ID]
		if !exists {
			quote = mockQuote(instrument)
		} else if quote.UpdatedAt.Before(cutoff) {
			quote.DataStatus = "stale"
		}
		items = append(items, QuoteItem{
			Symbol: instrument.Symbol, Name: instrument.Name, Market: instrument.Market,
			Currency: instrument.Currency, LatestPrice: quote.LatestPrice,
			ChangeAmount: quote.ChangeAmount, ChangePercent: quote.ChangePercent,
			QuoteTime: quote.QuoteTime, DataStatus: quote.DataStatus,
		})
	}
	return QuoteListData{Category: category, CategoryName: categoryName, Items: items}, nil
}

func (s *Service) refresh(ctx context.Context, instruments []Instrument, cached map[uint64]QuoteCache) {
	grouped := make(map[string][]Instrument)
	for _, instrument := range instruments {
		grouped[instrument.Source] = append(grouped[instrument.Source], instrument)
	}
	for source, group := range grouped {
		quotes, err := s.provider.Fetch(ctx, source, group)
		if err != nil {
			slog.Warn("refresh market quotes", "source", source, "error", err)
			s.cacheMocksForMissing(group, cached)
			continue
		}
		for _, instrument := range group {
			quote, exists := quotes[instrument.ID]
			if !exists {
				s.cacheMockIfNeeded(instrument, cached)
				continue
			}
			if err := s.repository.SaveQuote(quote); err != nil {
				slog.Warn("save market quote", "symbol", instrument.Symbol, "error", err)
				continue
			}
			quote.UpdatedAt = time.Now()
			cached[instrument.ID] = quote
		}
	}
}

func (s *Service) cacheMocksForMissing(instruments []Instrument, cached map[uint64]QuoteCache) {
	for _, instrument := range instruments {
		s.cacheMockIfNeeded(instrument, cached)
	}
}

func (s *Service) cacheMockIfNeeded(instrument Instrument, cached map[uint64]QuoteCache) {
	existing, exists := cached[instrument.ID]
	if exists && existing.DataStatus != "mock" {
		return
	}
	quote := mockQuote(instrument)
	if err := s.repository.SaveQuote(quote); err != nil {
		slog.Warn("save mock market quote", "symbol", instrument.Symbol, "error", err)
		return
	}
	quote.UpdatedAt = time.Now()
	cached[instrument.ID] = quote
}

func mockQuote(instrument Instrument) QuoteCache {
	return QuoteCache{
		InstrumentID: instrument.ID, LatestPrice: instrument.FallbackPrice,
		ChangeAmount:  instrument.FallbackChangeAmount,
		ChangePercent: instrument.FallbackChangePercent,
		QuoteTime:     time.Now().In(marketLocation), DataStatus: "mock",
	}
}
