package alipay

type ProfileData struct {
	Name        string `json:"name"`
	Phone       string `json:"phone"`
	AccountName string `json:"accountName"`
	IDNo        string `json:"idNo"`
}
type ElectricData struct {
	Payee       string `json:"payee"`
	Amount      string `json:"amount"`
	AccountNo   string `json:"accountNo"`
	AccountName string `json:"accountName"`
	Address     string `json:"address"`
	Supplier    string `json:"supplier"`
}
type WaterData struct {
	Amount       string `json:"amount"`
	AccountNo    string `json:"accountNo"`
	AccountName  string `json:"accountName"`
	AddressLine1 string `json:"addressLine1"`
	AddressLine2 string `json:"addressLine2"`
	Supplier     string `json:"supplier"`
}
type ProfileResponse struct {
	Code int         `json:"code" example:"1"`
	Msg  string      `json:"msg"`
	Data ProfileData `json:"data"`
}
type SummaryResponse struct {
	Code int     `json:"code" example:"1"`
	Msg  string  `json:"msg"`
	Data Summary `json:"data"`
}
type RecordsResponse struct {
	Code int     `json:"code" example:"1"`
	Msg  string  `json:"msg"`
	Data Records `json:"data"`
}
type DetailResponse struct {
	Code int    `json:"code" example:"1"`
	Msg  string `json:"msg"`
	Data Detail `json:"data"`
}
type AccountsResponse struct {
	Code int      `json:"code" example:"1"`
	Msg  string   `json:"msg"`
	Data Accounts `json:"data"`
}
type ElectricResponse struct {
	Code int          `json:"code" example:"1"`
	Msg  string       `json:"msg"`
	Data ElectricData `json:"data"`
}
type WaterResponse struct {
	Code int       `json:"code" example:"1"`
	Msg  string    `json:"msg"`
	Data WaterData `json:"data"`
}
