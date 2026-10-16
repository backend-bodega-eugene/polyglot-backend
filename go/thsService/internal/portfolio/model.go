package portfolio

import "time"

// Account stores the currently visible simulation. Earlier generations remain for audit.
type Account struct {
	HoldingVersion     int       `json:"-"`
	UserID             uint64    `gorm:"primaryKey" json:"-"`
	Generation         string    `json:"generation"`
	Currency           string    `json:"currency"`
	TotalAssets        string    `json:"totalAssets"`
	FloatingProfit     string    `json:"floatingProfit"`
	DailyProfit        string    `json:"dailyProfit"`
	DailyProfitPercent string    `json:"dailyProfitPercent"`
	TotalMarketValue   string    `json:"totalMarketValue"`
	AvailableCash      string    `json:"availableCash"`
	WithdrawableCash   string    `json:"withdrawableCash"`
	PositionRatio      string    `json:"positionRatio"`
	GeneratedAt        time.Time `json:"generatedAt"`
	UpdatedAt          time.Time `json:"-"`
}

func (Account) TableName() string { return "demo_portfolio_accounts" }

type Position struct {
	ID                 uint64    `gorm:"primaryKey" json:"id"`
	UserID             uint64    `json:"-"`
	Generation         string    `json:"-"`
	Symbol             string    `json:"symbol"`
	Name               string    `json:"name"`
	MarketValue        string    `json:"marketValue"`
	Profit             string    `json:"profit"`
	ProfitPercent      string    `json:"profitPercent"`
	DailyProfit        string    `json:"dailyProfit"`
	Quantity           int64     `json:"quantity"`
	AvailableQuantity  int64     `json:"availableQuantity"`
	CostPrice          string    `json:"costPrice"`
	LatestPrice        string    `json:"latestPrice"`
	QuoteTime          time.Time `json:"quoteTime"`
	DataStatus         string    `json:"dataStatus"`
	CreatedAt          time.Time `json:"-"`
	Currency           string    `json:"currency"`
	ExchangeRate       string    `json:"exchangeRate"`
	HoldingDays        int       `json:"holdingDays"`
	RealizedProfit     string    `json:"realizedProfit"`
	TransactionFees    string    `json:"transactionFees"`
	DailyProfitPercent string    `json:"dailyProfitPercent"`
}

func (Position) TableName() string { return "demo_portfolio_positions" }

type Trade struct {
	ID         uint64    `gorm:"primaryKey" json:"id"`
	UserID     uint64    `json:"-"`
	Generation string    `json:"-"`
	Symbol     string    `json:"symbol"`
	Name       string    `json:"name"`
	Side       string    `json:"side"`
	Quantity   int64     `json:"quantity"`
	Price      string    `json:"price"`
	Amount     string    `json:"amount"`
	TradedAt   time.Time `json:"tradedAt"`
	CreatedAt  time.Time `json:"-"`
	Currency   string    `json:"currency"`
	Fees       string    `json:"fees"`
}

func (Trade) TableName() string { return "demo_portfolio_trades" }

type Data struct {
	Simulated bool       `json:"simulated" example:"true"`
	Account   Account    `json:"account"`
	Positions []Position `json:"positions"`
}

type TradeList struct {
	Simulated  bool    `json:"simulated"`
	Generation string  `json:"generation"`
	Items      []Trade `json:"items"`
	Total      int64   `json:"total"`
	Page       int     `json:"page"`
	PageSize   int     `json:"pageSize"`
}

type PositionResponse struct {
	Code      int    `json:"code"`
	Message   string `json:"message"`
	Data      Data   `json:"data"`
	RequestID string `json:"requestId"`
}

type TradeResponse struct {
	Code      int       `json:"code"`
	Message   string    `json:"message"`
	Data      TradeList `json:"data"`
	RequestID string    `json:"requestId"`
}
