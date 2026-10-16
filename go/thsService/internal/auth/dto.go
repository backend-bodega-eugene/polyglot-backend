package auth

type RegisterRequest struct {
	Account  string `json:"account" binding:"required" example:"13800138000"`
	RealName string `json:"realName" binding:"required" example:"张三"`
	Password string `json:"password" binding:"required" example:"Example123"`
}

type LoginRequest struct {
	Account  string `json:"account" binding:"required" example:"13800138000"`
	Password string `json:"password" binding:"required" example:"Example123"`
}

type UserResponse struct {
	ID       uint64 `json:"id" example:"1"`
	Account  string `json:"account" example:"138****8000"`
	RealName string `json:"realName" example:"张三"`
}

type AuthData struct {
	AccessToken string       `json:"accessToken"`
	TokenType   string       `json:"tokenType" example:"Bearer"`
	ExpiresIn   int          `json:"expiresIn" example:"7200"`
	User        UserResponse `json:"user"`
}

type RegisterResponse struct {
	Code      int      `json:"code" example:"0"`
	Message   string   `json:"message" example:"ok"`
	Data      AuthData `json:"data"`
	RequestID string   `json:"requestId" example:"f5e76095d32f49199f5f7f897f5733a2"`
}

type LoginResponse struct {
	Code      int      `json:"code" example:"0"`
	Message   string   `json:"message" example:"ok"`
	Data      AuthData `json:"data"`
	RequestID string   `json:"requestId" example:"f5e76095d32f49199f5f7f897f5733a2"`
}
