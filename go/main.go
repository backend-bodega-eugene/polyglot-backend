package main

import (
	"eugene-go/stringutils"
	"fmt"
)

func main() {
	examples := []string{
		"hello",
		"Go语言",
		"🐱🐶123",
		"老胡最帅！",
	}

	for _, str := range examples {
		fmt.Printf("原字符串: %s | 反转后: %s\n", str, stringutils.EugeneReverse(str))
	}
}
