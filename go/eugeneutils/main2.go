package main

import (
	"io"
	"net/http"
	"strconv"
	"time"

	"eugene-go/httpserver"
)

func main() {
	s := httpserver.New(":8080")

	// 健康检查
	s.Handle("/ping", func(w http.ResponseWriter, r *http.Request) {
		httpserver.JSON(w, http.StatusOK, httpserver.M{
			"pong":  true,
			"ts":    time.Now().Format(time.RFC3339),
			"agent": r.UserAgent(),
		})
	})

	// 回显：POST 原样返回（演示读取 body）
	s.Handle("/echo", func(w http.ResponseWriter, r *http.Request) {
		b, _ := io.ReadAll(r.Body)
		defer r.Body.Close()
		httpserver.JSON(w, http.StatusOK, httpserver.M{
			"echo": string(b),
		})
	})

	// 求和：/sum?a=10&b=32
	s.Handle("/sum", func(w http.ResponseWriter, r *http.Request) {
		a, _ := strconv.Atoi(r.URL.Query().Get("a"))
		b, _ := strconv.Atoi(r.URL.Query().Get("b"))
		httpserver.JSON(w, http.StatusOK, httpserver.M{
			"a": a, "b": b, "sum": a + b,
		})
	})

	// 故意触发 panic，测试 recover 中间件
	s.Handle("/panic", func(w http.ResponseWriter, r *http.Request) {
		panic("oops")
	})

	if err := s.Start(); err != nil && err != http.ErrServerClosed {
		panic(err)
	}
}
