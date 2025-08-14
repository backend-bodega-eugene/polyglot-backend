package response

// Code 常量（为了业务可读性）
const (
	CodeOK              = 0
	CodeInvalidParam    = 10001
	CodeUnauthorized    = 10002
	CodeForbidden       = 10003
	CodeNotFound        = 10004
	CodeConflict        = 10005
	CodeTooManyRequests = 10006
	CodeInternalError   = 10007

	CodeUserNotExist    = 20001
	CodeUserFrozen      = 20002
	CodeOrderNotPayable = 30001
)

// 多语言消息
var codeMessages = map[string]map[int]string{
	"en": {
		CodeOK:              "success",
		CodeInvalidParam:    "invalid parameter",
		CodeUnauthorized:    "unauthorized",
		CodeForbidden:       "forbidden",
		CodeNotFound:        "not found",
		CodeConflict:        "conflict",
		CodeTooManyRequests: "too many requests",
		CodeInternalError:   "internal server error",
		CodeUserNotExist:    "user not exist",
		CodeUserFrozen:      "user frozen",
		CodeOrderNotPayable: "order not payable",
	},
	"zh": {
		CodeOK:              "成功",
		CodeInvalidParam:    "参数不合法",
		CodeUnauthorized:    "未授权",
		CodeForbidden:       "禁止访问",
		CodeNotFound:        "未找到资源",
		CodeConflict:        "冲突",
		CodeTooManyRequests: "请求过多",
		CodeInternalError:   "服务器内部错误",
		CodeUserNotExist:    "用户不存在",
		CodeUserFrozen:      "用户已冻结",
		CodeOrderNotPayable: "订单不可支付",
	},
}

// 取消息
func GetMsg(code int, lang string) string {
	if m, ok := codeMessages[lang]; ok {
		if msg, ok2 := m[code]; ok2 {
			return msg
		}
	}
	// 默认兜底
	return "unknown error"
}
