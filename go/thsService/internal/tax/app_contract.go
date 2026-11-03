package tax

import "github.com/gin-gonic/gin"

type AppProfile struct {
	Name           string `json:"name"`
	Sex            string `json:"sex"`
	Birth          string `json:"birth"`
	CardType       string `json:"card_type"`
	IDCard         string `json:"id_card"`
	Phone          string `json:"phone"`
	Email          string `json:"email"`
	TaxNumber      string `json:"tax_number"`
	BankNum        string `json:"bank_num"`
	BankType       string `json:"bank_type"`
	TaxBackMoney   string `json:"tax_back_money"`
	HujiCity       string `json:"huji_city"`
	HujiAddress    string `json:"huji_address"`
	UsuallyCity    string `json:"usually_city"`
	UsuallyAddress string `json:"usually_address"`
	ContactCity    string `json:"contact_city"`
	ContactAddress string `json:"contact_address"`
	Education      string `json:"education"`
	Nation         string `json:"nation"`
	Country        string `json:"country"`
}

func appProfile(v View) AppProfile {
	f := v.Fields
	return AppProfile{f.Name, f.Sex, f.Birth, f.CardType, f.IDCard, f.Phone, f.Email, f.TaxNumber, f.BankNum, f.BankType, f.TaxBackMoney, f.HujiCity, f.HujiAddress, f.UsuallyCity, f.UsuallyAddress, f.ContactCity, f.ContactAddress, f.Education, f.Nation, f.Country}
}

type AppEmployment struct {
	CompanyName string `json:"company_name"`
	TaxID       string `json:"tax_id"`
	CompanyNum  string `json:"company_num"`
	Post        string `json:"post"`
	StartDate   string `json:"start_date"`
	EndDate     string `json:"end_date"`
}

func appEmployments(rows []EmploymentView) []AppEmployment {
	result := make([]AppEmployment, 0, len(rows))
	for _, v := range rows {
		f := v.EmploymentFields
		result = append(result, AppEmployment{f.CompanyName, f.TaxID, f.CompanyNum, f.Post, f.StartDate, f.EndDate})
	}
	return result
}

type AppProfileResponse struct {
	Code int        `json:"code" example:"1"`
	Msg  string     `json:"msg"`
	Data AppProfile `json:"data"`
}
type AppEmploymentResponse struct {
	Code int             `json:"code" example:"1"`
	Msg  string          `json:"msg"`
	Data []AppEmployment `json:"data"`
}

func appFailure(c *gin.Context, status int, msg string) {
	c.JSON(status, gin.H{"code": 0, "msg": msg, "data": gin.H{}})
}
