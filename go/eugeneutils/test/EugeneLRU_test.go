package test

import (
	"eugene-go/lru"
	"reflect"
	"sync"
	"testing"
)

func TestLRUBasic(t *testing.T) {
	var evictedKeys []int
	cache := lru.New[int, string](2, func(k int, v string) {
		evictedKeys = append(evictedKeys, k)
	})

	// Put 2 个元素
	cache.Put(1, "one")
	cache.Put(2, "two")

	if cache.Len() != 2 {
		t.Errorf("Len() = %d; want 2", cache.Len())
	}

	// Get 存在的 key
	if v, ok := cache.Get(1); !ok || v != "one" {
		t.Errorf("Get(1) = %v, %v; want one, true", v, ok)
	}

	// 插入第三个元素 -> 淘汰最旧的 key=2
	cache.Put(3, "three")
	if cache.Len() != 2 {
		t.Errorf("Len after eviction = %d; want 2", cache.Len())
	}
	if _, ok := cache.Get(2); ok {
		t.Errorf("Expected key=2 to be evicted")
	}
	if !reflect.DeepEqual(evictedKeys, []int{2}) {
		t.Errorf("Evicted keys = %v; want [2]", evictedKeys)
	}

	// 覆盖已有 key
	cache.Put(3, "THREE")
	if v, _ := cache.Get(3); v != "THREE" {
		t.Errorf("Value not updated, got %s", v)
	}

	// Remove 存在的 key
	if !cache.Remove(1) {
		t.Errorf("Remove(1) = false; want true")
	}
	if cache.Remove(1) {
		t.Errorf("Remove(1) twice should return false")
	}
}

func TestLRUEvictionOrder(t *testing.T) {
	cache := lru.New[int, string](3, nil)
	cache.Put(1, "one")
	cache.Put(2, "two")
	cache.Put(3, "three")

	// 访问 key=1，使它变成 MRU
	cache.Get(1)

	// 插入 key=4，应该淘汰 key=2（最久未使用）
	cache.Put(4, "four")
	if _, ok := cache.Get(2); ok {
		t.Errorf("Expected key=2 to be evicted")
	}
}

func TestLRUConcurrency(t *testing.T) {
	cache := lru.New[int, int](50, nil)
	wg := sync.WaitGroup{}
	wg.Add(2)

	// Writer
	go func() {
		defer wg.Done()
		for i := 0; i < 1000; i++ {
			cache.Put(i%60, i)
		}
	}()

	// Reader
	go func() {
		defer wg.Done()
		for i := 0; i < 1000; i++ {
			cache.Get(i % 60)
		}
	}()

	wg.Wait()

	if cache.Len() > 50 {
		t.Errorf("Cache len=%d; want <= 50", cache.Len())
	}
}
