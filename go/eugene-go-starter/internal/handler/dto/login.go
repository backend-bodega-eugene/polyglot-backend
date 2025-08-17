package dto

type LoginReq struct {
	SiteID   uint64 `json:"siteId" binding:"required"`
	Username string `json:"username" binding:"required"`
	Password string `json:"password" binding:"required"`
}
