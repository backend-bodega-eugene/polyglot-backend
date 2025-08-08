package gset

import "sync"

// Set 是一个通用集合，基于 map[T]struct{} 实现
// - K: comparable 表示集合元素类型必须可比较（map 的要求）
// - 值用 struct{} 占位，节省内存（零字节）
// - safe=true 时会加锁保证并发安全；safe=false 时性能更高但非并发安全
type Set[T comparable] struct {
	m      map[T]struct{} // 存放元素的底层 map
	mu     sync.RWMutex   // 并发安全用的读写锁
	safe   bool           // 是否开启并发安全
}

// New 创建一个新的 Set
// safe=true 则开启读写锁保护
// vals... 可选初始化元素
func New[T comparable](safe bool, vals ...T) *Set[T] {
	s := &Set[T]{
		m:    make(map[T]struct{}, len(vals)),
		safe: safe,
	}
	for _, v := range vals {
		s.m[v] = struct{}{}
	}
	return s
}

// Add 向集合添加元素（存在则忽略）
func (s *Set[T]) Add(v T) {
	if s.safe {
		s.mu.Lock()
		defer s.mu.Unlock()
	}
	s.m[v] = struct{}{}
}

// Remove 从集合中删除元素（不存在则忽略）
func (s *Set[T]) Remove(v T) {
	if s.safe {
		s.mu.Lock()
		defer s.mu.Unlock()
	}
	delete(s.m, v)
}

// Has 检查元素是否存在于集合中
func (s *Set[T]) Has(v T) bool {
	if s.safe {
		s.mu.RLock()
		defer s.mu.RUnlock()
	}
	_, ok := s.m[v]
	return ok
}

// Len 返回集合中元素的数量
func (s *Set[T]) Len() int {
	if s.safe {
		s.mu.RLock()
		defer s.mu.RUnlock()
	}
	return len(s.m)
}

// ToSlice 将集合转换为切片（无序）
func (s *Set[T]) ToSlice() []T {
	if s.safe {
		s.mu.RLock()
		defer s.mu.RUnlock()
	}
	out := make([]T, 0, len(s.m))
	for v := range s.m {
		out = append(out, v)
	}
	return out
}

// Clear 清空集合
func (s *Set[T]) Clear() {
	if s.safe {
		s.mu.Lock()
		defer s.mu.Unlock()
	}
	s.m = make(map[T]struct{})
}

// Union 返回两个集合的并集（不会修改原集合）
// 并集 = a 中的所有元素 ∪ b 中的所有元素
func Union[T comparable](a, b *Set[T]) *Set[T] {
	r := New[T](a.safe || b.safe)
	for _, s := range []*Set[T]{a, b} {
		if s.safe {
			s.mu.RLock()
		}
		for v := range s.m {
			r.m[v] = struct{}{}
		}
		if s.safe {
			s.mu.RUnlock()
		}
	}
	return r
}

// Intersect 返回两个集合的交集（不会修改原集合）
// 交集 = 同时存在于 a 和 b 的元素
func Intersect[T comparable](a, b *Set[T]) *Set[T] {
	r := New[T](a.safe || b.safe)
	if a.safe {
		a.mu.RLock()
	}
	if b.safe {
		b.mu.RLock()
	}
	for v := range a.m {
		if _, ok := b.m[v]; ok {
			r.m[v] = struct{}{}
		}
	}
	if a.safe {
		a.mu.RUnlock()
	}
	if b.safe {
		b.mu.RUnlock()
	}
	return r
}

// Diff 返回两个集合的差集（不会修改原集合）
// 差集 (a - b) = 存在于 a 但不存在于 b 的元素
func Diff[T comparable](a, b *Set[T]) *Set[T] {
	r := New[T](a.safe || b.safe)
	if a.safe {
		a.mu.RLock()
	}
	if b.safe {
		b.mu.RLock()
	}
	for v := range a.m {
		if _, ok := b.m[v]; !ok {
			r.m[v] = struct{}{}
		}
	}
	if a.safe {
		a.mu.RUnlock()
	}
	if b.safe {
		b.mu.RUnlock()
	}
	return r
}
