
# 🧰 Eugene Go Utils 工具集

> 高质量、自写、注释全的 Go 语言工具函数集，适合学习、项目复用、技术展示。  
> 每一个函数都经过测试，兼容 Unicode，干净实用，不依赖第三方库。

---

## 📦 当前收录函数

### 🔁 字符串处理

#### `Reverse(s string) string` / `EugeneReverse(s string) string`
反转字符串，支持中文、emoji 等 Unicode 字符。
**示例：**
```go
Reverse("老胡最帅")        // 输出: "帅最胡老"
EugeneReverse("Go语言123") // 输出: "321言语oG"
```

#### `DeduplicateString(s string) string` / `EugeneDeduplicateString(s string) string`
去重字符串，保留字符首次出现的顺序。
**示例：**
```go
DeduplicateString("老胡老胡最帅最帅")        // 输出: "老胡最帅"
EugeneDeduplicateString("�🐱🐶🐶")         // 输出: "🐱🐶"
```

---

### �🔢 数组与排序

#### `BubbleSortInt(nums []int)` / `EugeneBubbleSortInt(nums []int)`
经典冒泡排序，对整数数组进行原地升序排序。
**示例：**
```go
nums := []int{5, 2, 3, 1}
BubbleSortInt(nums)        // nums 变为 [1, 2, 3, 5]
EugeneBubbleSortInt(nums)  // nums 变为 [1, 2, 3, 5]
```

---

### 🧪 测试覆盖

所有方法都配有完整的 *_test.go 单元测试，包括：

- Unicode 字符处理（中文、emoji）
- 空字符串 / 空数组
- 重复值处理
- 已排序 / 逆序等边界场景

使用 `go test ./...` 可一键运行全部测试 ✅

---

### 🤖 命名规范说明

所有函数都提供一个标准版本（如 `Reverse`），和一个「Eugene风格」版本（如 `EugeneReverse`），逻辑一致，命名自由。方便练习、比较、展示不同编码风格，全部自研 🧠

---

### 🏗️ 计划中函数（To Do）

- QuickSort 快速排序
- 字符串补全/对齐（左/右/居中）
- 字符串格式转换（驼峰转下划线）
- 自定义线程池（Go版本）
- Base64 编解码、异或加密
- 时间工具类（格式化、区间判断）

---

### 📚 项目定位

本项目目标是打造一个可展示编程能力的「个人基础工具库」，尤其适合用于：

- 求职面试代码展示
- GitHub 技术积累
- 多语言版本同步实现（将同步开发 Java / Go / C++ 版本）

---

### 👑 关于作者

来自某大型后端项目现场的火线实战选手「老胡 Eugene」，日常开发 Java、Go、C++ 等，偶尔弹吉他，赛车压弯，全栈通杀，风格暴力优雅兼具。  
欢迎 star ⭐ 或拍砖💥！

> 人生就像一行行代码，有时候要反转（Reverse），有时候要去重（Deduplicate），但更多时候，要自己一步步 BubbleSort，才能走到你想要的位置。

---