package response

import (
	"fmt"
)

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
	WrongTocken         = 10008
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
		WrongTocken:         "wrong token",
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
		WrongTocken:         "错误的令牌",
	},
}

// 取消息
func GetMsg(code int, lang string) string {
	fmt.Printf("[GetMsg] lang=%q code=%v type=%T\n", lang, code, code)

	m, ok := codeMessages[lang]
	if !ok {
		fmt.Printf("[GetMsg] lang not found. langs=%v\n", mapsOfLangs())
		return "unknown error"
	}
	fmt.Printf("[GetMsg] zh-codes=%v\n", keysOf(m))

	if msg, ok2 := m[code]; ok2 {
		return msg
	}
	fmt.Printf("[GetMsg] code miss: %v (type=%T)\n", code, code)
	return "unknown error"
}

func mapsOfLangs() []string {
	out := make([]string, 0, len(codeMessages))
	for k := range codeMessages {
		out = append(out, k)
	}
	return out
}
func keysOf(m map[int]string) []int {
	out := make([]int, 0, len(m))
	for k := range m {
		out = append(out, k)
	}
	return out
}
