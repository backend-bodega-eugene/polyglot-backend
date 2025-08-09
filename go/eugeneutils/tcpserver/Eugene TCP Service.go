package tcpserver

import (
	"encoding/binary"
	"errors"
	"io"
	"net"
	"sync"
	"sync/atomic"
	"time"
)

var (
	ErrServerClosed = errors.New("tcpserver: server closed")
)

// 连接 ID，方便业务侧定位
type ConnID uint64

// 每个连接的包装（含写锁，避免并发写）
type Conn struct {
	Id     ConnID
	Raw    net.Conn
	Wmu    sync.Mutex
	Server *Server
}

func (c *Conn) ID() ConnID { return c.Id }

// Send 发送一帧（协议：4 字节大端长度 + 数据）
func (c *Conn) Send(data []byte) error {
	c.Wmu.Lock()
	defer c.Wmu.Unlock()

	if c.Server.writeTimeout > 0 {
		_ = c.Raw.SetWriteDeadline(time.Now().Add(c.Server.writeTimeout))
	}
	var hdr [4]byte
	binary.BigEndian.PutUint32(hdr[:], uint32(len(data)))
	if _, err := c.Raw.Write(hdr[:]); err != nil {
		return err
	}
	if len(data) > 0 {
		if _, err := c.Raw.Write(data); err != nil {
			return err
		}
	}
	return nil
}

// Server 封装
type Server struct {
	addr         string
	ln           net.Listener
	readTimeout  time.Duration
	writeTimeout time.Duration

	onConnect    func(*Conn)
	onMessage    func(*Conn, []byte)
	onDisconnect func(*Conn, error)

	mu      sync.RWMutex
	conns   map[ConnID]*Conn
	running atomic.Bool
	nextID  atomic.Uint64
	wg      sync.WaitGroup
}

// New 创建服务端
func New(addr string, opts ...Option) *Server {
	s := &Server{
		addr:  addr,
		conns: make(map[ConnID]*Conn),
	}
	for _, o := range opts {
		o(s)
	}
	return s
}

// Option 配置
type Option func(*Server)

func WithReadTimeout(d time.Duration) Option  { return func(s *Server) { s.readTimeout = d } }
func WithWriteTimeout(d time.Duration) Option { return func(s *Server) { s.writeTimeout = d } }

func (s *Server) OnConnect(f func(*Conn))           { s.onConnect = f }
func (s *Server) OnMessage(f func(*Conn, []byte))   { s.onMessage = f }
func (s *Server) OnDisconnect(f func(*Conn, error)) { s.onDisconnect = f }

// Start 启动监听（非阻塞）
func (s *Server) Start() error {
	if s.running.Load() {
		return nil
	}
	ln, err := net.Listen("tcp", s.addr)
	if err != nil {
		return err
	}
	s.ln = ln
	s.running.Store(true)
	s.wg.Add(1)
	go s.acceptLoop()
	return nil
}

// Stop 关闭监听并踢掉所有连接（阻塞直到 goroutine 退出）
func (s *Server) Stop() error {
	if !s.running.Load() {
		return ErrServerClosed
	}
	s.running.Store(false)
	if s.ln != nil {
		_ = s.ln.Close()
	}
	// 关闭所有连接
	s.mu.Lock()
	for _, c := range s.conns {
		_ = c.Raw.Close()
	}
	s.conns = make(map[ConnID]*Conn)
	s.mu.Unlock()

	s.wg.Wait()
	return nil
}

// Broadcast 广播
func (s *Server) Broadcast(data []byte) {
	s.mu.RLock()
	defer s.mu.RUnlock()
	for _, c := range s.conns {
		_ = c.Send(data) // 不因单个错误阻塞其它连接
	}
}

// SendTo 指定连接发送
func (s *Server) SendTo(id ConnID, data []byte) error {
	s.mu.RLock()
	c := s.conns[id]
	s.mu.RUnlock()
	if c == nil {
		return errors.New("tcpserver: conn not found")
	}
	return c.Send(data)
}

func (s *Server) acceptLoop() {
	defer s.wg.Done()
	for s.running.Load() {
		conn, err := s.ln.Accept()
		if err != nil {
			if !s.running.Load() {
				return // 正常关闭
			}
			continue
		}
		c := &Conn{
			Id:     ConnID(s.nextID.Add(1)),
			Raw:    conn,
			Server: s,
		}
		s.mu.Lock()
		s.conns[c.Id] = c
		s.mu.Unlock()

		if s.onConnect != nil {
			s.onConnect(c)
		}

		s.wg.Add(1)
		go s.readLoop(c)
	}
}

func (s *Server) readLoop(c *Conn) {
	defer func() {
		s.mu.Lock()
		delete(s.conns, c.Id)
		s.mu.Unlock()
		_ = c.Raw.Close()
		if s.onDisconnect != nil {
			s.onDisconnect(c, nil)
		}
		s.wg.Done()
	}()

	hdr := make([]byte, 4)
	for s.running.Load() {
		if s.readTimeout > 0 {
			_ = c.Raw.SetReadDeadline(time.Now().Add(s.readTimeout))
		}
		// 读长度
		if _, err := io.ReadFull(c.Raw, hdr); err != nil {
			if s.onDisconnect != nil {
				s.onDisconnect(c, err)
			}
			return
		}
		n := binary.BigEndian.Uint32(hdr)
		var payload []byte
		if n > 0 {
			payload = make([]byte, n)
			if _, err := io.ReadFull(c.Raw, payload); err != nil {
				if s.onDisconnect != nil {
					s.onDisconnect(c, err)
				}
				return
			}
		}
		if s.onMessage != nil {
			s.onMessage(c, payload)
			// 🔹 加一段自动回复
			reply := []byte("server got: " + string(payload))
			_ = c.Send(reply)
		}
	}
}
