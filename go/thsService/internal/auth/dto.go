package auth

type RegisterRequest struct {
	Account  string `json:"account" binding:"required" example:"13800138000"`
	RealName string `json:"realName" binding:"required" example:"张三"`
	Password string `json:"password" binding:"required" example:"Example123"`
}

type LoginRequest struct {
	// 公共会员账号：完整中国大陆手机号或邮箱。
	Account string `json:"account" binding:"required" example:"13800138000"`
	// 会员密码，使用后台创建或重置的密码。
	Password string `json:"password" binding:"required" example:"Example123"`
}

type UserResponse struct {
	ID       uint64 `json:"id" example:"1"`
	Account  string `json:"account" example:"138****8000"`
	RealName string `json:"realName" example:"张三"`
}

type AuthData struct {
	// 公共会员访问Token，后续请求使用Authorization: Bearer <accessToken>。
	AccessToken string `json:"accessToken"`
	// 固定Bearer。
	TokenType string `json:"tokenType" example:"Bearer"`
	// Token有效期，单位秒。
	ExpiresIn int `json:"expiresIn" example:"7200"`
	// 会员基本资料，account为脱敏展示账号。
	User UserResponse `json:"user"`
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
