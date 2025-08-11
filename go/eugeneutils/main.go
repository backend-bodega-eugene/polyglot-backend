package main

import (
	"fmt"
	"log"
	"os"
	"os/signal"
	"syscall"
	"time"

	"eugene-go/tcpserver"
)

func main() {
	srv := tcpserver.New(
		"127.0.0.1:9099",
		tcpserver.WithReadTimeout(0), // 可按需设置读超时
		tcpserver.WithWriteTimeout(5*time.Second),
	)

	// 连接建立
	srv.OnConnect(func(c *tcpserver.Conn) {
		log.Printf("[CONN %d] %s 已连接\n", c.ID(), c.Raw.RemoteAddr())
		_ = c.Send([]byte("welcome!\n")) // 小礼物：欢迎语
	})

	srv.OnMessage(func(c *tcpserver.Conn, msg []byte) {
		log.Printf("[RECV %d] %q\n", c.ID(), string(msg))
		// 回显给发起者
		_ = c.Send([]byte("echo: " + string(msg)))
		// 广播给所有在线（含自己）
		srv.Broadcast([]byte(fmt.Sprintf("broadcast from %d: %s", c.ID(), string(msg))))
	})

	// 连接断开
	srv.OnDisconnect(func(c *tcpserver.Conn, err error) {
		if err != nil {
			log.Printf("[DISC %d] with error: %v\n", c.ID(), err)
		} else {
			log.Printf("[DISC %d]\n", c.ID())
		}
	})

	// 启动
	if err := srv.Start(); err != nil {
		log.Fatal("start:", err)
	}
	log.Println("TCP Server listening on 127.0.0.1:9099")

	// 优雅退出
	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit
	log.Println("Shutting down ...")
	_ = srv.Stop()
	log.Println("Bye.")
}
