package market

import "time"

type Instrument struct {
	ID                    uint64 `gorm:"primaryKey"`
	Category              string `gorm:"size:32;not null"`
	Symbol                string `gorm:"size:32;not null"`
	Source                string `gorm:"size:16;not null"`
	SourceSymbol          string `gorm:"size:32;not null"`
	Name                  string `gorm:"size:64;not null"`
	Market                string `gorm:"size:16;not null"`
	Currency              string `gorm:"size:8;not null"`
	SortOrder             uint   `gorm:"not null"`
	FallbackPrice         string `gorm:"type:decimal(24,6);not null"`
	FallbackChangeAmount  string `gorm:"type:decimal(24,6);not null"`
	FallbackChangePercent string `gorm:"type:decimal(12,4);not null"`
	Enabled               uint8  `gorm:"not null"`
	CreatedAt             time.Time
	UpdatedAt             time.Time
}

func (Instrument) TableName() string { return "market_instruments" }

type QuoteCache struct {
	InstrumentID  uint64  `gorm:"primaryKey"`
	LatestPrice   string  `gorm:"type:decimal(24,6);not null"`
	PreviousClose *string `gorm:"type:decimal(24,6)"`
	ChangeAmount  string  `gorm:"type:decimal(24,6);not null"`
	ChangePercent string  `gorm:"type:decimal(12,4);not null"`
	QuoteTime     time.Time
	DataStatus    string
	RawData       *string
	CreatedAt     time.Time
	UpdatedAt     time.Time
}

func (QuoteCache) TableName() string { return "market_quote_cache" }
