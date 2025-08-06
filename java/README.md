# Java Arsenal ☕️

Reusable Java classes, Spring-style utilities, annotations, and all the boring but essential things.

🧩 For Spring Boot lovers, JDK warriors, and all JVM travelers.
先从java开始吧,我们会找一些有用的东西,放进来,并且创建一个springboot的 web restful api


### 🔄 ArrayUtils - 通用数组反转工具类

提供对各种 Java 数组的原地反转操作，支持泛型数组及所有八大基本类型，适用于常见的数组倒序处理场景。

功能特点：
- 支持 `int[]` / `long[]` / `float[]` / `double[]` 等所有基本类型数组；
- 支持 `T[]` 泛型数组；
- 原地修改，无需额外空间；
- 边界判断友好，null 或长度为 0/1 的数组将自动忽略。

#### 示例代码：

```java
int[] nums = {1, 2, 3, 4, 5};
ArrayUtils.reverse(nums); 
// nums => [5, 4, 3, 2, 1]

String[] names = {"Alice", "Bob", "Charlie"};
ArrayUtils.reverse(names); 
// names => {"Charlie", "Bob", "Alice"}


### 🧩 BracketValidator - 括号匹配校验器

用于检查字符串中的括号是否成对出现并正确嵌套，支持三种常见括号：`()`, `{}`, `[]`。可用于表达式校验、代码分析器、配置文件格式检查等场景。

> 本工具使用线程安全的 `SynchronizedStack` 作为底层数据结构，适合并发环境。

#### ✅ 特性
- 支持嵌套括号、多层级匹配；
- 非括号字符自动忽略（也可扩展为校验非法字符）；
- 可快速集成至表达式解析器、模版渲染器等模块中。

#### 示例代码：

```java
BracketValidator.isValid("{[()]}"); // true
BracketValidator.isValid("([)]");   // false
BracketValidator.isValid("hello(world)[]"); // true


### 🌐 BrowserHistory - 浏览器历史记录模拟器

模拟浏览器中的前进/后退操作，使用两个栈结构维护历史记录。支持初始化主页、访问新页面、后退、前进等常见行为，适用于 UI 控件模拟、算法练习等场景。

> 后退栈与前进栈均使用线程安全的 `SynchronizedStack` 实现。

#### ✅ 特性
- 状态清晰，使用两个栈分别管理前进与后退历史；
- 任意访问新页面将清空前进路径，符合浏览器行为；
- 支持链式操作与页面状态查询；
- 内部线程安全，适合并发模拟或服务端缓存。

#### 示例代码：

```java
BrowserHistory browser = new BrowserHistory("首页");

browser.visit("百度");
browser.visit("知乎");
browser.back();        // => 百度
browser.back();        // => 首页
browser.forward();     // => 百度


### 🧾 ChineseIDValidator - 中国大陆身份证号校验工具

专为中华人民共和国大陆地区设计的身份证号（18 位）校验工具，支持格式检查、校验码校验、出生日期验证、性别提取等功能。

> 🚫 不支持港澳台地区或护照类型，仅适用于标准 18 位身份证。

#### ✅ 支持功能

- 格式合法性检查（含省份编码、生日合法性、校验位）；
- 自动提取出生日期（格式：yyyy-MM-dd）；
- 自动识别性别（基于倒数第二位）；
- 校验码按权重 + 模 11 校验；
- 内部使用 `java.time.LocalDate` 保证日期精确合法。

#### 示例代码：

```java
String id = "110105199003072617";

ChineseIDValidator.isValid(id);       // true
ChineseIDValidator.getGender(id);     // "男"
ChineseIDValidator.getBirthdate(id);  // "1990-03-07"


### 🔐 EugeneAESUtils - 手写版 AES 加密实现（128位）

这是一个完全不依赖标准库的 AES 加密工具类，实现了 **AES-128** 的标准加密流程：

- 支持 16 字节明文 + 16 字节密钥
- 实现完整的 SubBytes → ShiftRows → MixColumns → AddRoundKey 共 10 轮加密
- 完全模拟 Rijndael 算法，无任何 `javax.crypto` 依赖
- 包含 KeyExpansion（密钥扩展）、S-Box 替换、Galois 乘法等所有核心模块

> ⚠ 仅适用于 **ECB 模式单块加密**，主要用于算法学习与底层理解。

---

#### ✅ 实现细节

- **加密轮次：** 共 10 轮，其中第 0 轮仅 AddRoundKey，最后一轮不执行 MixColumns；
- **状态矩阵：** 使用 `4x4` 字节矩阵 `state[4][4]` 处理数据；
- **密钥扩展：** 基于 Rijndael RCON 表完成轮密钥扩展；
- **S-Box 替换：** 固定查表替换，提升运算效率；
- **MixColumns：** Galois Field 上的列混淆变换，纯字节运算实现。

---

#### 示例代码：

```java
byte[] plainText = new byte[]{
    (byte) 0x32, (byte) 0x43, (byte) 0xf6, (byte) 0xa8,
    (byte) 0x88, (byte) 0x5a, (byte) 0x30, (byte) 0x8d,
    (byte) 0x31, (byte) 0x31, (byte) 0x98, (byte) 0xa2,
    (byte) 0xe0, (byte) 0x37, (byte) 0x07, (byte) 0x34
};

byte[] key = new byte[]{
    (byte) 0x2b, (byte) 0x7e, (byte) 0x15, (byte) 0x16,
    (byte) 0x28, (byte) 0xae, (byte) 0xd2, (byte) 0xa6,
    (byte) 0xab, (byte) 0xf7, (byte) 0x15, (byte) 0x88,
    (byte) 0x09, (byte) 0xcf, (byte) 0x4f, (byte) 0x3c
};

byte[] encrypted = EugeneAESUtils.aesEncrypt(plainText, key);


### 🧬 EugeneBase64 - 手写 Base64 编解码工具

轻量级 Base64 编码/解码工具类，实现了标准 Base64 的编码与解码逻辑，**完全不依赖 Java 标准库（如 Base64.getEncoder()）**，适用于底层环境、算法教学、面试展示等场景。

#### ✅ 实现原理

- 每 3 个字节（24 位）被拆分成 4 组，每组 6 位；
- 每组 6 位转为一个整数（0~63），映射到 Base64 字符表；
- 编码末尾不足 3 字节时，补零后使用 `=` 补齐 4 位输出；
- 解码时会过滤非法字符并正确处理 `=` 补位。

#### 示例代码：

```java
String encoded = EugeneBase64.myBase64Encode("hello,Eugene");
System.out.println(encoded); // aGVsbG8sRXVnZW5l

String decoded = EugeneBase64.myBase64DecodeToString(encoded);
System.out.println(decoded); // hello,Eugene


### 🗃 EugeneHashMap - 轻量级哈希表实现（拉链法）

这是一个纯手写实现的 `HashMap` 简化版本，支持常用的键值对存储操作，包括 `put`、`get`、`remove`、`containsKey`、`size` 等。采用 **拉链法（链表）处理哈希冲突**，并支持自动扩容，适合学习、面试或轻量级数据存储场景。

---

#### ✅ 功能特性：

- 使用链表解决哈希冲突（拉链法）；
- 初始容量为 `16`，负载因子为 `0.9`，自动扩容；
- 支持 `null` 键（固定映射到下标 0）；
- 提供结构打印方法 `printTableStructure()`，适合调试与教学；
- 实现了完整的 rehash 逻辑（重新分布桶位置）；

---

#### 示例代码：

```java
EugeneHashMap<String, Integer> map = new EugeneHashMap<>();
map.put("apple", 10);
map.put("banana", 20);
map.put("apple", 30); // 更新 value
map.remove("banana");

System.out.println(map.get("apple"));       // 30
System.out.println(map.containsKey("fig")); // false
System.out.println(map.size());             // 当前键值对数量
map.printTableStructure();                  // 查看哈希结构分布


### 🔁 EugeneQueue - 自定义循环队列（数组实现）

轻量级循环队列实现，支持固定容量、泛型入队出队、环形结构复用空间。适用于消息队列、任务缓冲池、滑动窗口等高性能场景。

> 底层采用数组结构 + front/rear 指针，绕过动态扩容带来的 GC 问题。

---

#### ✅ 支持功能

- 入队 `enqueue(T)` / 出队 `dequeue()`；
- 获取队头元素 `peek()`；
- 判空 `isEmpty()`、判满 `isFull()`；
- 清空队列 `clear()`；
- 获取当前大小 `size()`；

---

#### 示例代码

```java
EugeneQueue<String> queue = new EugeneQueue<>(5);

queue.enqueue("A");
queue.enqueue("B");
System.out.println(queue.dequeue()); // A
System.out.println(queue.peek());    // B
System.out.println(queue.size());    // 1



### 🧵 EugeneThreadPool - 自定义轻量线程池

一个最小可运行的线程池实现，内部维护固定数量的工作线程，通过阻塞队列提交任务。适用于轻量级任务调度、算法验证、多线程教学等场景。

---

#### ✅ 支持功能

- 支持异步任务提交 `execute(Runnable)`；
- 支持线程池立即关闭 `shutdownNow()`；
- 使用 `BlockingQueue` 阻塞等待任务；
- 每个工作线程捕获异常，避免线程挂掉；
- 可水平扩展为完整线程池框架（例如添加线程池监控、拒绝策略等）；

---

#### 示例代码：

```java
EugeneThreadPool pool = new EugeneThreadPool(3);

pool.execute(() -> System.out.println(Thread.currentThread().getName() + " 执行任务 A"));
pool.execute(() -> System.out.println(Thread.currentThread().getName() + " 执行任务 B"));

Thread.sleep(1000); // 模拟任务处理耗时

pool.shutdownNow(); // 强制关闭线程池



### 🔢 GenericBubbleSort - 通用冒泡排序工具

使用 Java 泛型实现的通用冒泡排序类，支持任意实现了 `Comparable<T>` 接口的类型（如 `Integer`、`Double`、`String`、`Character` 等）。可灵活指定升序或降序，适用于排序教学或小规模数据处理。

---

#### ✅ 支持功能

- 支持泛型数组的排序（`T extends Comparable<T>`）；
- 提供升序、降序两种排序方式；
- 自动判断是否提前退出（若已排好序）；
- 附带 `printArray()` 方法，快速打印排序结果；

---

#### 示例代码：

```java
Integer[] nums = {4, 2, 7, 1, 3};
GenericBubbleSort.bubbleSort(nums, true); // 升序
GenericBubbleSort.printArray(nums);       // 输出：1 2 3 4 7

String[] names = {"Tom", "Alice", "Bob"};
GenericBubbleSort.bubbleSort(names, false); // 降序
GenericBubbleSort.printArray(names);        // 输出：Tom Bob Alice



### 🧹 GenericDeduplicator - 泛型数组去重工具类

提供两种去重方式，适用于不同场景：

- ✅ **有序数组** → 使用双指针法，原地去重，效率高；
- ✅ **无序数组** → 使用 `LinkedHashSet`，保留原顺序；

支持任意实现了 `equals()` 的泛型类型。

---

#### 🛠 支持方法

| 方法 | 适用场景 | 特点 |
|------|----------|------|
| `deduplicate(T[], Class<T>)` | 有序数组 | 双指针高效去重 |
| `deduplicateWithSet(T[], Class<T>)` | 无序数组 | 保序去重 |

---

#### 示例代码

```java
// 有序数组去重
Integer[] sorted = {1, 2, 2, 3, 3, 3, 4};
Integer[] uniqueSorted = GenericDeduplicator.deduplicate(sorted, Integer.class);
// 输出: [1, 2, 3, 4]

// 无序数组去重（保留顺序）
String[] names = {"Tom", "Alice", "Tom", "Bob", "Alice"};
String[] uniqueNames = GenericDeduplicator.deduplicateWithSet(names, String.class);
// 输出: ["Tom", "Alice", "Bob"]



### ⚡ QuickSort - 泛型快速排序工具类

使用原地递归方式实现经典快速排序算法，支持任意实现 `Comparable` 接口的泛型数组。

---

#### 🧠 特性

- ✅ **泛型支持**：如 `Integer`、`String`、`Double` 等；
- ✅ **升降序切换**：传参一秒搞定；
- ✅ **原地排序**：空间效率高；
- ✅ **结构清晰**：递归 + 分区逻辑一目了然。

---

#### 🛠 支持方法

| 方法 | 功能说明 |
|------|----------|
| `quickSort(T[])` | 升序排序（默认） |
| `quickSort(T[], boolean ascending)` | 升序 / 降序自选 |
| `printArray(T[])` | 打印数组（调试友好） |

---

#### 示例代码

```java
Integer[] nums = {6, 3, 8, 5, 2};
QuickSort.quickSort(nums, false); // 降序
QuickSort.printArray(nums);       // 输出：8 6 5 3 2


### 🔐 SafeStack - 支持动态扩容的线程安全栈

一个线程安全的栈结构，使用 `ReentrantLock` 保证并发访问的互斥性，支持泛型与动态扩容。

---

#### 🧠 特性

- ✅ **线程安全**：采用 `ReentrantLock` 实现同步；
- ✅ **支持泛型**：适用于任何类型；
- ✅ **自动扩容**：元素超限自动翻倍扩容；
- ✅ **功能完备**：`push` / `pop` / `peek` / `isEmpty` / `size`。

---

#### 🛠 支持方法

| 方法 | 功能说明 |
|------|----------|
| `push(T value)` | 入栈（自动扩容） |
| `pop()`         | 出栈并返回栈顶元素 |
| `peek()`        | 查看栈顶元素但不出栈 |
| `isEmpty()`     | 判断栈是否为空 |
| `size()`        | 返回当前栈中元素数量 |

---

#### 示例代码

```java
SafeStack<String> stack = new SafeStack<>(4);
stack.push("hello");
stack.push("world");
System.out.println(stack.pop()); // world


### 🧵 SynchronizedStack - 使用 synchronized 的线程安全栈

这是一个基于 `synchronized` 关键字实现的线程安全栈，支持泛型和动态扩容，适用于简单并发场景下的栈操作需求。

---

#### 🧠 特性

- ✅ **线程安全**：通过 `synchronized` 关键字加锁；
- ✅ **泛型支持**：兼容任意类型；
- ✅ **自动扩容**：栈满自动翻倍扩容；
- ✅ **基础功能全面**：支持常规操作：push / pop / peek / size / isEmpty / clear。

---

#### 🛠 支持方法

| 方法名 | 功能说明 |
|--------|----------|
| `push(T value)`  | 入栈，自动扩容 |
| `pop()`          | 出栈并返回栈顶元素 |
| `peek()`         | 查看栈顶元素 |
| `isEmpty()`      | 判断栈是否为空 |
| `size()`         | 当前元素数量 |
| `clear()`        | 清空栈内元素（不释放内存） |

---

#### 示例代码

```java
SynchronizedStack<Integer> stack = new SynchronizedStack<>(2);
stack.push(1);
stack.push(2);
System.out.println(stack.pop()); // 2


### 🔓 UnEugeneAESUtils - 原生 AES-128 解密工具

一个完全独立实现的 AES-128 解密工具类，不依赖 `javax.crypto` 等标准加密库，完全手写底层逻辑。  
实现了 AES 标准中的所有解密步骤：**KeyExpansion → InvSubBytes → InvShiftRows → InvMixColumns → AddRoundKey**。

---

#### 🧠 特性

- **纯手写 AES-128 解密**：无任何第三方依赖；
- **完整的 AES 逆过程**：支持 10 轮解密（含轮密钥扩展）；
- **固定块大小**：支持 **128-bit（16字节）密钥** 和 **16字节数据块**；
- **可独立测试**：输入密文块和密钥即可还原明文。

---

#### 🛠 提供的方法

| 方法名 | 功能说明 |
|--------|----------|
| `aesDecrypt(byte[] input, byte[] key)` | 使用指定 128 位密钥解密 16 字节密文块 |
| `invMixColumns(byte[][] state)`        | AES 列混淆的逆运算 |
| `invShiftRows(byte[][] state)`         | AES 行移位的逆运算 |
| `invSubBytes(byte[][] state)`          | 字节替代的逆运算（使用 INV_S_BOX） |
| `addRoundKey(byte[][] state, byte[][] roundKey)` | 添加轮密钥 |
| `keyExpansion(byte[] key)`             | 将原始密钥扩展成 44 个 4 字节的轮密钥 |

---

#### 示例代码

```java
byte[] cipherText = {
    (byte)0x39, (byte)0x25, (byte)0x84, (byte)0x1d,
    (byte)0x02, (byte)0xdc, (byte)0x09, (byte)0xfb,
    (byte)0xdc, (byte)0x11, (byte)0x85, (byte)0x97,
    (byte)0x19, (byte)0x6a, (byte)0x0b, (byte)0x32
};

byte[] key = {
    (byte)0x2b, (byte)0x7e, (byte)0x15, (byte)0x16,
    (byte)0x28, (byte)0xae, (byte)0xd2, (byte)0xa6,
    (byte)0xab, (byte)0xf7, (byte)0x15, (byte)0x88,
    (byte)0x09, (byte)0xcf, (byte)0x4f, (byte)0x3c
};

byte[] plainText = UnEugeneAESUtils.aesDecrypt(cipherText, key);
System.out.println(Arrays.toString(plainText));
