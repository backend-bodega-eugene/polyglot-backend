package main

import (
	"eugene-go/logger"
	_ "eugene-go/stringutils"
	_ "fmt"
)

func main() {
	// 👉 可选：设置输出文件
	err := logger.SetLogFile()
	if err != nil {
		panic(err)
	}

	// ✅ 最简用法：全自动 caller 检测
	logger.InfoThreadnameAndModulename("服务启动成功！")
	logger.InfoThreadnameAndModulename("Redis 未连接，使用默认缓存")
	logger.InfoThreadnameAndModulename("数据库连接失败")
	logger.InfoThreadnameAndModulename("当前内存使用率：68%")
	// examples := []string{
	// 	"hello",
	// 	"Go语言",
	// 	"🐱🐶123",
	// 	"老胡最帅！",
	// }

	// for _, str := range examples {
	// 	fmt.Printf("原字符串: %s | 反转后: %s\n", str, stringutils.EugeneReverse(str))
	// }
}
