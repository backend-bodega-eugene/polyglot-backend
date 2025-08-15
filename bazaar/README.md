# 📦 bazaar

Welcome to the tech bazaar!  
This folder gathers a variety of handy resources — books, websites, snippets, tools, and more.  
Come browse, grab, and go!

- `books.md`: 📚 Recommended books
- `sites.md`: 🌐 Useful websites
- `snippets.md`: 🧩 Code fragments and magic tricks
- `tools.md`: 🛠️ Development tools & utilities

幂等（Idempotent）：
随便来几刀，结果都一样，刀刀不留痕。

熔断（Circuit Breaker）：
出事了，老板一声令下，关门歇业，谁来也没用。

降级（Fallback）：
出事了，假装还在营业，糊弄一下，能混一天是一天。

限流（Rate Limiting）：
慢点来，别催，咱是老弱病残，急不得。

雪崩效应（Avalanche Effect）：
我倒了，兄弟们也跟着一起躺平，主线程不一定在场。

热点数据（Hot Data）：
天天被翻牌的红人，谁都想来蹭一脚。

缓存穿透（Cache Penetration）：
缓存形同虚设，直接被人无视。

缓存击穿（Cache Breakdown）：
刚失效就被人盯上，猝不及防。

缓存雪崩（Cache Avalanche）：
一群缓存一起失效，场面失控，老板头秃。

读写锁（RWMutex）：
写错了，大家都懵圈，谁也别想动。

乐观锁（Optimistic Lock）：
大家都很乐观，觉得不会撞车，结果撞了就重来。

悲观锁（Pessimistic Lock）：
谁都不信谁，先锁了再说，稳妥第一。

分布式锁（Distributed Lock）：
大家排队，谁也别插队，规矩最重要。

并发安全（Concurrency Safe）：
老弱病残也能安全过马路，谁也别抢道。

高可用（HA）：
我倒下了，后面还有千军万马顶上，组织永不倒。

水平扩展（Scale Out）：
多拉几个兄弟一起上，队伍越来越壮。

垂直扩展（Scale Up）：
老板加钱，装备升级，内存 CPU 全都拉满。

异步（Async）：
我是谁？我在哪？我干啥？反正先甩出去再说。

同步（Sync）：
大家一起走，谁也不能掉队，步调一致。

全链路追踪（TraceID）：
给你盖个章，走哪都能查到你，谁欺负你都能找到后台。

灰度发布（Canary Release）：
先让一部分人尝鲜，踩坑也先让他们踩。

蓝绿部署（Blue-Green Deploy）：
两拨人轮流上阵，互相切换，谁都不吃亏。


# 🐒 老胡架构术语翻译官（调侃版）

> ⚠️ 高浓度调侃，请勿对号入座。  
> 适合在交接文档、技术分享、酒桌吹水等场合使用，阅读前请深呼吸三次。  

| 术语 | 官方解释 | 老胡调侃翻译 |
|------|----------|--------------|
| **幂等** (Idempotent) | 多次执行结果一致 | 随便干多少次，结果都一样的 |
| **熔断** (Circuit Breaker) | 保护系统的故障开关 | 出事了就关门，不营业了 |
| **降级** (Fallback) | 降低服务质量维持运行 | 出了事，假装还在工作 |
| **限流** (Rate Limiting) | 控制访问速率 | 我是傻逼、老人、残疾，别干这么快 |
| **雪崩效应** (Avalanche Effect) | 级联故障导致系统全挂 | 我死了，你们都别想活（可能不是主线程） |
| **热点数据** (Hot Data) | 高频访问数据 | 经常用的数据 |
| **缓存穿透** (Cache Penetration) | 缓存查不到，请求打到 DB | 缓存没得了 |
| **缓存击穿** (Cache Breakdown) | 热点缓存失效瞬间被大量访问 | 缓存刚刚没得了 |
| **缓存雪崩** (Cache Avalanche) | 大量缓存同时失效 | 一堆缓存刚刚没得了 |
| **读写锁** (RWMutex) | 控制读写并发访问 | 写错了，我也不晓得 |
| **乐观锁** (Optimistic Lock) | 假设冲突很少的并发控制 | 我是傻逼、老人、残疾，别干这么快 |
| **悲观锁** (Pessimistic Lock) | 假设冲突频繁的并发控制 | 我是傻逼、老人、残疾，别干这么快 |
| **分布式锁** (Distributed Lock) | 分布式环境下的互斥控制 | 大家一起排队 |
| **并发安全** (Concurrency Safe) | 线程安全 | 我是傻逼、老人、残疾，别干这么快 |
| **高可用** (HA) | 保证系统持续运行 | 我倒下了，组织上还有千千万万个我前赴后继 |
| **水平扩展** (Scale Out) | 增加更多机器分担压力 | 加入组织，成为千千万万个我 |
| **垂直扩展** (Scale Up) | 增加单机硬件性能 | 老板出钱了，内存、CPU 不够 |
| **异步** (Async) | 非阻塞执行 | 我是谁，我从哪里来，我要去哪里 |
| **同步** (Sync) | 阻塞执行 | 我是谁，我从哪里来，我要去哪里 |
| **全链路追踪** (TraceID) | 请求全路径跟踪 | 给你盖个章，以后有人欺负你，就报我的名字 |
| **灰度发布** (Canary Release) | 小范围试运行 | 一部分人先富起来 |
| **蓝绿部署** (Blue-Green Deploy) | 两套环境无缝切换 | 两部分人先富起来，还能你日我婆娘，我日你婆娘 |

---
> 🥂 **免责声明**：本表仅供技术人调侃，任何雷同纯属巧合，若被对号入座请自行关门放狗。
