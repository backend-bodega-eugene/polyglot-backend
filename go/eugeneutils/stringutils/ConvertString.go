package stringutils

import (
	"fmt"
	"strconv"
)

// EugeneIntToString 将常见数字类型转换为字符串。
// 支持 int, int64, uint64, float64。其他类型将返回 "unsupported"
func EugeneIntToString(value any) string {
	switch v := value.(type) {
	case int:
		return strconv.Itoa(v)
	case int64:
		return strconv.FormatInt(v, 10)
	case uint64:
		return strconv.FormatUint(v, 10)
	case float64:
		return fmt.Sprintf("%f", v)
	case float32:
		return fmt.Sprintf("%f", v)
	default:
		return "unsupported"
	}
}
