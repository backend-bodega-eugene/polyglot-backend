package taxbusiness

type MessageData struct {
	ID         uint64 `json:"id"`
	Title      string `json:"title"`
	Content    string `json:"content"`
	CreateTime string `json:"create_time"`
}
type MessagesContract struct {
	Code  int           `json:"code" example:"1"`
	Msg   string        `json:"msg" example:"success"`
	Data  []MessageData `json:"data"`
	Count int           `json:"count"`
}

// Client response schemas retain the exact names used by the supplied App.
type MonthlyDetail struct {
	ID                  uint64 `json:"id"`
	Zhaiyao             string `json:"zhaiyao"`
	ExpectDate          string `json:"expect_date"`
	ShenbaoDate         string `json:"shenbao_date"`
	ShenbaoEnd          string `json:"shenbao_end"`
	TaxName             string `json:"tax_name"`
	Channel             string `json:"channel"`
	Xiaolei             string `json:"xiaolei"`
	ObligorName         string `json:"obligor_name"`
	TaxNum              string `json:"tax_num"`
	TaxAuthority        string `json:"tax_authority"`
	CompanyName         string `json:"company_name"`
	CompanyNum          string `json:"company_num"`
	TaxCompany          string `json:"tax_company"`
	Post                string `json:"post"`
	StartDate           string `json:"start_date"`
	EndDate             string `json:"end_date"`
	Income              string `json:"income"`
	Tax                 string `json:"tax"`
	Rate                string `json:"rate"`
	SusuanCount         string `json:"susuanCount"`
	TaxFreeIncome       string `json:"tax_free_income"`
	DeductionMoney      string `json:"deduction_money"`
	SpecialDeduction    string `json:"special_deduction"`
	NowShenTax          string `json:"nowShenTax"`
	Yanglao             string `json:"yanglao"`
	Yiliao              string `json:"yiliao"`
	Shiye               string `json:"shiye"`
	GJJ                 string `json:"gjj"`
	OtherDeduction      string `json:"other_deduction"`
	DonateProject       string `json:"donate_project"`
	Type                int    `json:"type"`
	AdditionalDeduction string `json:"additional_deduction"`
	PersonalPension     string `json:"personal_pension"`
	TaxReduction        string `json:"tax_reduction"`
}
type AnnualData struct {
	List        []MonthlyDetail `json:"list"`
	TotalIncome string          `json:"totalIncome"`
	TotalTax    string          `json:"totalTax"`
}
type ApplicationData struct {
	Year         int     `json:"year"`
	TotalIncome  float64 `json:"total_income"`
	TotalTax     float64 `json:"total_tax"`
	RefundAmount float64 `json:"refund_amount"`
	Status       string  `json:"status"`
}
type OpenRecordData struct {
	ID         uint64 `json:"id"`
	CreateTime string `json:"create_time"`
	ImagesData string `json:"images_data"`
	StartDate  string `json:"start_date"`
	EndDate    string `json:"end_date"`
}
type ScanData struct {
	ImagesData string `json:"images_data"`
}
type CalculationDetail struct {
	Income      string `json:"income"`
	Tax         string `json:"tax"`
	CompanyName string `json:"company_name"`
	IncomeType  string `json:"income_type"`
	Subtype     string `json:"subtype"`
	ExpectDate  string `json:"expect_date"`
}
type CalculationData struct {
	ExpectDate           string              `json:"expect_date"`
	CompanyName          string              `json:"company_name"`
	TotalIncome          string              `json:"totalIncome"`
	TotalMianShuiIncome  string              `json:"totalMianShuiIncome"`
	TotalFuMin           string              `json:"totalFuMin"`
	TotalZhuanMin        string              `json:"totalZhuanMin"`
	TotalFuJiaMin        string              `json:"totalFuJiaMin"`
	TotalOtherMin        string              `json:"totalOtherMin"`
	TotalYanglao         string              `json:"totalYanglao"`
	TotalZhunJuan        string              `json:"totalZhunJuan"`
	TotalYingNaShuiSuoDe string              `json:"totalYingNaShuiSuoDe"`
	Rate                 string              `json:"rate"`
	SusuanCount          string              `json:"susuanCount"`
	TotalYingJiaoTax     string              `json:"totalYingJiaoTax"`
	TotalJiaoTax         string              `json:"totalJiaoTax"`
	TotalYiJiaoTax       string              `json:"totalYiJiaoTax"`
	TotalMin             string              `json:"totalMin"`
	TotalJianMian        string              `json:"totalJianMian"`
	NowShenTax           string              `json:"nowShenTax"`
	Bqsbse               string              `json:"bqsbse"`
	DetailList           []CalculationDetail `json:"detail_list"`
}
type AnnualContract struct {
	Code int        `json:"code" example:"1"`
	Msg  string     `json:"msg" example:"success"`
	Data AnnualData `json:"data"`
}
type ApplicationResponse struct {
	Code int             `json:"code" example:"1"`
	Msg  string          `json:"msg" example:"success"`
	Data ApplicationData `json:"data"`
}
type OpenRecordsResponse struct {
	Code int              `json:"code" example:"1"`
	Msg  string           `json:"msg" example:"success"`
	Data []OpenRecordData `json:"data"`
}
type ScanResponse struct {
	Code int      `json:"code" example:"1"`
	Msg  string   `json:"msg" example:"success"`
	Data ScanData `json:"data"`
}
type CalculationResponse struct {
	Code int             `json:"code" example:"1"`
	Msg  string          `json:"msg" example:"success"`
	Data CalculationData `json:"data"`
}
