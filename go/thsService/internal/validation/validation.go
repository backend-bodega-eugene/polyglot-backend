package validation

import (
	"errors"
	"fmt"

	"github.com/go-playground/validator/v10"
)

func Message(err error) string {
	var validationErrors validator.ValidationErrors
	if !errors.As(err, &validationErrors) || len(validationErrors) == 0 {
		return "请求参数错误"
	}

	fieldError := validationErrors[0]
	switch fieldError.Tag() {
	case "required":
		return fmt.Sprintf("%s 为必填项", fieldError.Field())
	case "min":
		return fmt.Sprintf("%s 长度或数值不能小于 %s", fieldError.Field(), fieldError.Param())
	case "max":
		return fmt.Sprintf("%s 长度或数值不能大于 %s", fieldError.Field(), fieldError.Param())
	default:
		return fmt.Sprintf("%s 格式不正确", fieldError.Field())
	}
}
