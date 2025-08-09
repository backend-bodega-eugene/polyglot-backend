package httpserver

import (
	"context"
	"encoding/json"
	"log"
	"math/rand"
	"net/http"
	"os"
	"os/signal"
	"sync/atomic"
	"syscall"
	"time"
)

type Server struct {
	addr   string
	mux    *http.ServeMux
	server *http.Server

	reqID uint64
}

// New 创建一个封装好的 HTTP Server（含基本超时）
func New(addr string) *Server {
	mux := http.NewServeMux()
	s := &Server{
		addr: addr,
		mux:  mux,
	}
	s.server = &http.Server{
		Addr:              addr,
		Handler:           s.withMiddlewares(mux), // 接上中间件链
		ReadTimeout:       10 * time.Second,
		ReadHeaderTimeout: 5 * time.Second,
		WriteTimeout:      15 * time.Second,
		IdleTimeout:       60 * time.Second,
	}
	return s
}

// Handle 注册路由（GET/POST 都行）
func (s *Server) Handle(pattern string, h http.HandlerFunc) {
	s.mux.HandleFunc(pattern, h)
}

// Start 启动并支持 Ctrl+C 优雅退出
func (s *Server) Start() error {
	log.Printf("HTTP server listening on %s\n", s.addr)

	// 优雅退出
	stop := make(chan os.Signal, 1)
	signal.Notify(stop, syscall.SIGINT, syscall.SIGTERM)

	errCh := make(chan error, 1)
	go func() {
		if err := s.server.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			errCh <- err
		}
	}()

	select {
	case <-stop:
		log.Println("Shutting down HTTP server...")
		ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
		defer cancel()
		return s.server.Shutdown(ctx)
	case err := <-errCh:
		return err
	}
}

// ---- Middlewares ----

func (s *Server) withMiddlewares(next http.Handler) http.Handler {
	return s.requestID(s.recoverMW(s.logMW(next)))
}

func (s *Server) requestID(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		id := atomic.AddUint64(&s.reqID, 1)
		// 简单随机盐，避免进程重启后从1开始的冲突
		wid := id*1000 + uint64(rand.Intn(999))
		r = r.WithContext(context.WithValue(r.Context(), ctxKeyReqID, wid))
		w.Header().Set("X-Request-ID", reqIDString(wid))
		next.ServeHTTP(w, r)
	})
}

func (s *Server) logMW(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		start := time.Now()
		ww := &wrapWriter{ResponseWriter: w, status: 200}
		next.ServeHTTP(ww, r)
		log.Printf("[%s] %s %s %d %s rid=%s",
			r.RemoteAddr, r.Method, r.URL.Path, ww.status, time.Since(start), reqIDFromCtx(r.Context()))
	})
}

func (s *Server) recoverMW(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		defer func() {
			if rec := recover(); rec != nil {
				log.Printf("panic: %v rid=%s", rec, reqIDFromCtx(r.Context()))
				JSON(w, http.StatusInternalServerError, M{"error": "internal server error"})
			}
		}()
		next.ServeHTTP(w, r)
	})
}

// ---- Helpers ----

type M map[string]any

func JSON(w http.ResponseWriter, code int, v any) {
	w.Header().Set("Content-Type", "application/json; charset=utf-8")
	w.WriteHeader(code)
	_ = json.NewEncoder(w).Encode(v)
}

type ctxKey string

const ctxKeyReqID ctxKey = "rid"

func reqIDFromCtx(ctx context.Context) string {
	if v := ctx.Value(ctxKeyReqID); v != nil {
		return reqIDString(v.(uint64))
	}
	return "-"
}
func reqIDString(id uint64) string { return time.Now().Format("150405") + "-" + itoa(id) }

// 小写 itoa，避免引第三方
func itoa(u uint64) string {
	if u == 0 {
		return "0"
	}
	var b [20]byte
	i := len(b)
	for u > 0 {
		i--
		b[i] = byte('0' + u%10)
		u /= 10
	}
	return string(b[i:])
}

// 记录响应码
type wrapWriter struct {
	http.ResponseWriter
	status int
}

func (w *wrapWriter) WriteHeader(code int) {
	w.status = code
	w.ResponseWriter.WriteHeader(code)
}
