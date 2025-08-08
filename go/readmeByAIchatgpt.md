
# 🧰 Eugene Go Utils 工具集

<div align="center">

<b>Go utils | string | sort | set | LRU | red-black tree | logger | panic guard | Unicode | concurrency | generic | 数据结构 | 算法 | 工具库 | 高质量 | 单元测试 | 无第三方依赖</b>

</div>

---

> 高质量、自研、注释详细的 Go 语言基础工具函数与数据结构集合。  
> 所有代码均无第三方依赖，支持 Unicode，配有完善单元测试，适合学习、项目复用与技术展示。

---

**English Description:**

Eugene Go Utils is a high-quality, well-documented, dependency-free Go utility library featuring string manipulation, sorting, sets, LRU cache, red-black tree, logging, panic guard, and more. All functions are Unicode-compatible, generic, and come with comprehensive unit tests. Perfect for learning, production, and technical demonstration.

**Keywords:** Go, Golang, utils, utility, string, sort, set, LRU, red-black tree, logger, panic, Unicode, concurrency, generic, data structure, algorithm, test, reusable, open source

---

---


## 📦 功能模块一览

### 1. 字符串处理（`stringutils`）

- **Reverse / EugeneReverse**  
  反转字符串，支持中文、emoji 等 Unicode 字符，保证多字节字符不乱码。
- **DeduplicateString / EugeneDeduplicateString**  
  去重字符串，保留字符首次出现顺序，支持全 Unicode。

### 2. 排序算法（`stringutils`）

- **BubbleSortInt / EugeneBubbleSortInt**  
  经典冒泡排序，对整数切片原地升序排序。

### 3. 红黑树（`rbtree`）

- **Tree / Node**  
  红黑树实现，支持插入、查找、中序遍历，自动维护红黑树性质。
- **EugeneInsert / EugeneSearch / EugeneInOrder**  
  插入、查找、遍历接口。
- **NoRedRed / BlackHeightEqual**  
  红黑树性质校验（无红红冲突、黑高一致）。

### 4. LRU 缓存（`lru`）

- **Cache**  
  并发安全的 LRU 缓存，支持泛型 key/value，自动淘汰最久未使用元素。
- **Put / Get / Remove / Len**  
  基本操作，支持淘汰回调。

### 5. 泛型集合 Set（`gset`）

- **Set**  
  支持并发安全的集合类型，基于 map 实现。
- **Add / Remove / Has / Len / ToSlice / Clear**  
  基本集合操作。
- **Union / Intersect / Diff**  
  并集、交集、差集运算。

### 6. 日志工具（`logger`）

- **结构化日志输出**  
  支持 INFO/WARN/ERROR/DEBUG 多级别，自动带线程、模块信息，支持日志文件输出。
- **Info / Warn / Error / Debug / InfoThreadnameAndModulename**  
  多种日志接口，便于调试和生产环境使用。

### 7. Panic 守护（`panicguard`）

- **Guard**  
  捕获 goroutine panic，支持自定义处理（如告警、日志、自动恢复）。

---

## 🛠️ 代码示例

### 字符串反转

```go
import "eugene-go/stringutils"

s := "Go语言123"
rev := stringutils.Reverse(s) // "321言语oG"
```

### LRU 缓存

```go
import "eugene-go/lru"

cache := lru.New[int, string](2, nil)
cache.Put(1, "one")
cache.Put(2, "two")
v, ok := cache.Get(1) // v="one", ok=true
```

### 红黑树

```go
import "eugene-go/rbtree"

var tr rbtree.Tree
tr.EugeneInsert(10)
tr.EugeneInsert(5)
found := tr.EugeneSearch(5)
inorder := tr.EugeneInOrder() // 升序切片
```

### 集合 Set

```go
import "eugene-go/gset"

s := gset.New[int](true, 1, 2, 3)
s.Add(4)
s.Remove(2)
has := s.Has(1) // true
```

### 日志

```go
import "eugene-go/logger"

logger.InfoThreadnameAndModulename("服务启动成功！")
```

---

## 🧪 测试覆盖

所有模块均配有 *_test.go 单元测试，覆盖：

- Unicode 字符、边界场景
- 并发安全
- 红黑树性质校验
- LRU 淘汰顺序
- 集合运算正确性

运行全部测试：

```sh
go test ./...
```

---

## 🏗️ 计划中功能

- 快速排序、线程池、Base64、时间工具等

---

## 👑 关于作者

来自一线后端实战的「老胡 Eugene」，热爱技术、全栈开发，欢迎 star ⭐ 或交流！

---

如需更详细的模块说明或用法，欢迎查阅各源码文件注释。
