package main

import (
	"fmt"
	_ "net/http/pprof" // 导入 pprof
	"net/http"
	"time"
)

func busyWork() {
	for i := 0; i < 1e8; i++ { // 模拟 CPU 密集型任务
	}
}

func main() {
	// 启动 pprof 服务
	go func() {
		fmt.Println("pprof running on :6060")
		http.ListenAndServe("0.0.0.0:6060", nil)
	}()

	for {
		busyWork()
		time.Sleep(1 * time.Second)
	}
}
