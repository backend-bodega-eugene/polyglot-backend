package lru

import "sync"

// entry 是链表节点，保存了 key/value 以及双向链表的前后指针
type entry[K comparable, V any] struct {
	key   K
	value V
	prev  *entry[K, V]
	next  *entry[K, V]
}

// Cache 是一个支持并发安全的 LRU 缓存
// - K：key 类型，必须可比较（map 的要求）
// - V：value 类型
type Cache[K comparable, V any] struct {
	capacity int                     // 最大容量（节点数）
	items    map[K]*entry[K, V]      // key -> 链表节点
	head     *entry[K, V]            // 链表头（MRU：最近使用）
	tail     *entry[K, V]            // 链表尾（LRU：最久未使用）
	onEvict  func(key K, value V)    // 淘汰回调（可选）

	mu sync.RWMutex                  // 读写锁（去掉就是非并发安全版本）
}

// New 创建一个 LRU 缓存
// capacity：最大容量（>0）
// onEvict：淘汰时的回调，可为 nil
func New[K comparable, V any](capacity int, onEvict func(K, V)) *Cache[K, V] {
	if capacity <= 0 {
		panic("lru: capacity must be > 0")
	}
	return &Cache[K, V]{
		capacity: capacity,
		items:    make(map[K]*entry[K, V], capacity),
		onEvict:  onEvict,
	}
}

// Get 获取 key 对应的 value，并将该节点移动到链表头（标记为最近使用）
// 返回值：value, 是否存在
func (c *Cache[K, V]) Get(key K) (V, bool) {
	// 先读锁检查是否存在
	c.mu.RLock()
	e, ok := c.items[key]
	c.mu.RUnlock()
	if !ok {
		var zero V
		return zero, false
	}

	// 存在则加写锁移动到链表头
	c.mu.Lock()
	c.moveToFront(e)
	val := e.value
	c.mu.Unlock()
	return val, true
}

// Put 添加或更新一个 key/value
// - 如果 key 已存在，更新 value 并移动到链表头
// - 如果 key 不存在，插入新节点到链表头
// - 如果超容量，淘汰链表尾节点（LRU）
func (c *Cache[K, V]) Put(key K, value V) {
	c.mu.Lock()
	// 已存在：更新值并移动到头部
	if e, ok := c.items[key]; ok {
		e.value = value
		c.moveToFront(e)
		c.mu.Unlock()
		return
	}
	// 不存在：新建节点
	e := &entry[K, V]{key: key, value: value}
	c.items[key] = e
	c.addToFront(e)

	// 容量溢出 -> 淘汰尾节点
	if len(c.items) > c.capacity {
		ev := c.tail
		c.remove(ev)
		delete(c.items, ev.key)
		if c.onEvict != nil {
			// 注意：不要在锁内做耗时操作，这里是快速调用
			c.onEvict(ev.key, ev.value)
		}
	}
	c.mu.Unlock()
}

// Remove 删除一个 key
// 返回值：是否存在
func (c *Cache[K, V]) Remove(key K) bool {
	c.mu.Lock()
	defer c.mu.Unlock()
	if e, ok := c.items[key]; ok {
		c.remove(e)
		delete(c.items, key)
		return true
	}
	return false
}

// Len 返回当前缓存中的元素数量
func (c *Cache[K, V]) Len() int {
	c.mu.RLock()
	n := len(c.items)
	c.mu.RUnlock()
	return n
}

// ===== 以下为链表操作（调用前需加锁） =====

// addToFront 将节点插入到链表头（MRU）
func (c *Cache[K, V]) addToFront(e *entry[K, V]) {
	e.prev = nil
	e.next = c.head
	if c.head != nil {
		c.head.prev = e
	}
	c.head = e
	if c.tail == nil {
		// 空链表，尾部也指向新节点
		c.tail = e
	}
}

// moveToFront 将节点移动到链表头
func (c *Cache[K, V]) moveToFront(e *entry[K, V]) {
	if c.head == e {
		return // 已经是头
	}
	c.remove(e)
	c.addToFront(e)
}

// remove 从链表中移除节点
func (c *Cache[K, V]) remove(e *entry[K, V]) {
	if e.prev != nil {
		e.prev.next = e.next
	} else {
		// e 是头节点
		c.head = e.next
	}
	if e.next != nil {
		e.next.prev = e.prev
	} else {
		// e 是尾节点
		c.tail = e.prev
	}
	e.prev, e.next = nil, nil
}
