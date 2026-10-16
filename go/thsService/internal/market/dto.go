package market

import "time"

type QuoteItem struct {
	Symbol        string    `json:"symbol" example:"600519.SH"`
	Name          string    `json:"name" example:"贵州茅台"`
	Market        string    `json:"market" example:"CN"`
	Currency      string    `json:"currency" example:"CNY"`
	LatestPrice   string    `json:"latestPrice" example:"1274.89"`
	ChangeAmount  string    `json:"changeAmount" example:"-13.11"`
	ChangePercent string    `json:"changePercent" example:"-1.02"`
	QuoteTime     time.Time `json:"quoteTime"`
	DataStatus    string    `json:"dataStatus" enums:"live,stale,mock" example:"live"`
}

type QuoteListData struct {
	Category     string      `json:"category" example:"a_stock"`
	CategoryName string      `json:"categoryName" example:"A股"`
	Items        []QuoteItem `json:"items"`
}

type QuoteListResponse struct {
	Code      int           `json:"code" example:"0"`
	Message   string        `json:"message" example:"ok"`
	Data      QuoteListData `json:"data"`
	RequestID string        `json:"requestId"`
}
