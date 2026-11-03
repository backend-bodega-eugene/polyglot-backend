package nanwang

// Request and response contracts exposed in Swagger.
type HotRequest struct {
	// 地区码，默认050200
	AreaCode string `json:"areaCode"`
	// 条数，默认3，最多100
	Count string `json:"count"`
}
type LatestRequest struct {
	// 地区码，默认050200
	AreaCode string `json:"areaCode"`
	// 条数，默认10，最多100
	Count string `json:"count"`
}
type ListRequest struct {
	// 地区码，默认050200
	AreaCode string `json:"areaCode"`
	// 分类编号，模拟初始1/2/3，留空查询全部
	ClassID string `json:"classId"`
	// 页码，默认1
	PageNo string `json:"pageNo"`
	// 每页条数，默认10，最多100
	PageSize string `json:"pageSize"`
}
type DetailRequest struct {
	// 资讯列表中的id，必填
	ID string `json:"id"`
}
type HallRequest struct {
	// 经度字符串，必填，-180至180
	Longitude string `json:"longitude"`
	// 纬度字符串，必填，-90至90
	Latitude string `json:"latitude"`
}
type PointRequest struct {
	// 可选户号，仅筛选本人计量点
	EleCustID string `json:"eleCustId"`
}
type EmptyRequest struct {
}
type News struct {
	ID          string `json:"id"`
	Title       string `json:"title"`
	ClassID     string `json:"classId"`
	AreaCode    string `json:"areaCode"`
	IssuedName  string `json:"issuedName"`
	Source      string `json:"source"`
	PublishTime string `json:"publishTime"`
	CreateTime  string `json:"createTime"`
	ImgURL      string `json:"imgUrl"`
	CoverURL    string `json:"coverUrl"`
	Link        string `json:"link"`
	ReadCount   int    `json:"readCount"`
	Content     string `json:"content"`
}
type Customer struct {
	ID              string `json:"id"`
	UserName        string `json:"userName"`
	CustName        string `json:"custName"`
	EleCustID       string `json:"eleCustId"`
	CustNo          string `json:"custNo"`
	Address         string `json:"address"`
	EleAddr         string `json:"eleAddr"`
	EleCustNumberID string `json:"eleCustNumberId"`
	Arrears         string `json:"arrears"`
	TotalArrears    string `json:"totalArrears"`
}
type Hall struct {
	ID       string `json:"id"`
	HallName string `json:"hallName"`
	Address  string `json:"address"`
	Distance string `json:"distance"`
}
type Message struct {
	ID    string `json:"id"`
	Title string `json:"title"`
}
type Point struct {
	ID        string `json:"id"`
	EleCustID string `json:"eleCustId"`
	PointName string `json:"pointName"`
}
type Charging struct {
	ID      string `json:"id"`
	HasPile bool   `json:"hasPile"`
}
type NewsResponse struct {
	Sta     string `json:"sta" example:"00"`
	Message string `json:"message" example:"success"`
	Data    []News `json:"data"`
}
type NewsDetailResponse struct {
	Sta     string `json:"sta" example:"00"`
	Message string `json:"message" example:"success"`
	Data    News   `json:"data"`
}
type CustomerResponse struct {
	Sta     string     `json:"sta" example:"00"`
	Message string     `json:"message" example:"success"`
	Data    []Customer `json:"data"`
}
type HallResponse struct {
	Sta     string `json:"sta" example:"00"`
	Message string `json:"message" example:"success"`
	Data    []Hall `json:"data"`
}
type MessagesResponse struct {
	Sta     string    `json:"sta" example:"00"`
	Message string    `json:"message" example:"success"`
	Data    []Message `json:"data"`
}
type PointsResponse struct {
	Sta     string  `json:"sta" example:"00"`
	Message string  `json:"message" example:"success"`
	Data    []Point `json:"data"`
}
type ChargingResponse struct {
	Sta     string   `json:"sta" example:"00"`
	Message string   `json:"message" example:"success"`
	Data    Charging `json:"data"`
}
