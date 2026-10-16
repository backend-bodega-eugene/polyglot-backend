package market

import (
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
)

type Repository struct{ db *gorm.DB }

func NewRepository(db *gorm.DB) *Repository { return &Repository{db: db} }

func (r *Repository) ListInstruments(category string) ([]Instrument, error) {
	var instruments []Instrument
	err := r.db.Where("category = ? AND enabled = 1", category).
		Order("sort_order ASC, id ASC").Find(&instruments).Error
	return instruments, err
}

func (r *Repository) ListQuoteCaches(instrumentIDs []uint64) (map[uint64]QuoteCache, error) {
	result := make(map[uint64]QuoteCache, len(instrumentIDs))
	if len(instrumentIDs) == 0 {
		return result, nil
	}
	var quotes []QuoteCache
	if err := r.db.Where("instrument_id IN ?", instrumentIDs).Find(&quotes).Error; err != nil {
		return nil, err
	}
	for _, quote := range quotes {
		result[quote.InstrumentID] = quote
	}
	return result, nil
}

func (r *Repository) SaveQuote(quote QuoteCache) error {
	return r.db.Clauses(clause.OnConflict{
		Columns: []clause.Column{{Name: "instrument_id"}},
		DoUpdates: clause.AssignmentColumns([]string{
			"latest_price", "previous_close", "change_amount", "change_percent",
			"quote_time", "data_status", "raw_data", "updated_at",
		}),
	}).Create(&quote).Error
}
