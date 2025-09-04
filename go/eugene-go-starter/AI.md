# AI.md — 团队 AI 协作与编码规范（示例模板）

> 目标：让 IDE 内置 AI（Copilot、Cursor、Codeium、Cline 等）理解并遵循我们的工程标准，尽可能一次生成可用、可测、可维护的代码。

---

## 1. 工程总体约定（给 AI 的“世界观”）

* 语言与版本：Java 21 / Go 1.22 / Node 20（按仓库具体项目选择）。
* 框架：Spring Boot 3 + MyBatis-Plus；Go: Gin + Gorm；Web：Vite + Vue 3 + TS。
* 数据库：MySQL 8、MongoDB 6、Redis 7。所有时间采用 UTC 存储，应用层用 `LocalDateTime`（Java）或 `time.Time`（Go）。
* 统一返回结构（Java）：`Result<T>`，使用实例：`new Result<T>().ok(data)` / `new Result<T>().error(message)`。
* Swagger 使用：Controller 与 DTO 需要 Swagger 注解；Entity 不加 Swagger 注解但保留 Javadoc。
* MyBatis-Plus 风格：Mapper 继承 `BaseMapper<T>`；Service 接口继承 `IService<T>`；ServiceImpl 继承 `ServiceImpl<Mapper,T>`；**不使用 XML**，尽量用 `LambdaQueryWrapper`。

> **AI 指令**：在生成任何 Controller/DTO/Service/Mapper 时，必须遵循以上约定；不引入与现有风格冲突的库。

---

## 2. 代码风格与静态校验

* 统一格式化：使用 `.editorconfig`（缩进、换行、编码）。
* Java：启用 Checkstyle/Spotless + Google Style；
* Go：`gofmt` + `golangci-lint`；
* TS/JS：ESLint（recommended + @typescript-eslint），Prettier 自动格式化。

> **AI 指令**：生成代码必须通过对应 Lint 规则；若需禁用规则，**说明原因**并仅对局部禁用。

---

## 3. 项目结构（示例）

```
backend/
  src/main/java/...
  src/test/java/...
  domain/ dto/ controller/ service/ mapper/ entity/
frontend/
  src/
  tests/
```

> **AI 指令**：按分层放置文件，不得在 Controller 中写业务逻辑；DTO 与 Entity 分离。

---

## 4. API 设计与安全

* 认证：使用 JWT（Header: `Authorization: Bearer <token>`）。
* 加密：服务端密钥不入库、不硬编码；使用环境变量 / 密钥管理。
* 日志：不打印明文密码/敏感 token。

> **AI 指令**：涉及登录/密码的接口，**切记**在查询用户时返回 `password_hash`（字段名以仓库为准），但日志中掩码处理。

---

## 5. 数据访问与实体映射

* MySQL：字段与实体命名保持语义清晰；时间字段 `datetime/timestamp` → `LocalDateTime`（Java）。
* Mongo：**必须**为 struct / entity 字段补充 `bson:"field_name"` 标签；查询需显式 Projection 时，确保 Handler 所需字段齐全。

> **AI 指令（坑位提醒）**：
>
> * 若方法名 `findByUsername`，**不要**仅返回 username；需根据调用方需求包含 `password_hash` 等必需字段。
> * 新增 Mongo 实体时，**务必**添加 `bson` 标签；否则解码失败。

---

## 6. 事务、并发与性能

* 事务边界：应用层 Service 维护；禁止在 Controller 开事务。
* 并发：线程池/协程池需配置上限；外部系统调用**逐条处理**或使用可靠队列，避免“批量瞬时洪峰”。
* 缓存：读多写少接口可加 Redis；设置合理过期与穿透保护。

> **AI 指令**：涉及批量外呼的任务，默认策略为**来一条发一条**；若对方扛不住，由对方限流或返回错误，我们记录并重试。

---

## 7. 测试与样例

* 单元测试覆盖核心服务逻辑；
* 提供最小可运行样例（mock repo/HTTP）。

> **AI 指令**：为新增 Service/Repository 补 1-2 个关键用例；至少覆盖成功与失败路径。

---

## 8. 提交信息与分支

* 分支命名：`feat/`、`fix/`、`refactor/`、`chore/`、`docs/`。
* 提交信息：英文为主，中英对照亦可；首行不超过 72 字符。

> **AI 指令**：在输出代码后，附上一条建议的 commit message。

---

## 9. 典型 Prompt 模板（给开发者复制到 IDE）

**实现 Service + Controller（Java / MyBatis-Plus）**

```
你是资深后端工程师。请在遵循以下仓库约定下完成任务：
- Controller+DTO 需要 Swagger 注解；Entity 不加 Swagger 注解但需 Javadoc。
- 统一返回 Result<T>（示例：new Result<T>().ok(data)）。
- MyBatis-Plus：BaseMapper/IService/ServiceImpl；不使用 XML。
- 时间字段使用 LocalDateTime。
任务：为用户登录实现 service 方法与 controller 接口；用户名查询需返回 password_hash；日志中遮盖敏感信息；补 2 个单元测试。
输出：关键代码片段 + 文件路径建议 + commit message。
```

**修复 Mongo Projection 漏字段**

```
在 Go/Gin 项目中，修复 findByUsername 仅返回 username 的问题：
- Handler 需要 password_hash；
- 为相关 struct 增加 bson 标签；
- 补充查询 Projection，保证字段齐全；
- 给出回归测试示例。
```

---

## 10. 常见坑与对策（AI 也会踩）

1. **Mongo struct 忘记 `bson` 标签** → 数据解码失败或字段始终为空。解决：定义/修改实体时强制添加；Review 清单检查。
2. **按方法名字面投影**（如 `findByUsername` 只投影 username）→ Handler 缺关键字段。解决：从调用方需求反推 Projection；为关键链路写测试。
3. **批量瞬时外呼** → 对方系统间歇雪崩。解决：逐条/限流/队列；失败重试与熔断。

---

## 11. 代码评审清单（PR 模板片段）

* [ ] 遵循分层：Controller(薄) / Service(业务) / Repo(数据访问)
* [ ] DTO/Controller 有 Swagger 注解；Entity 有 Javadoc
* [ ] 统一 `Result<T>` 返回
* [ ] MyBatis-Plus 无 XML，自检 `LambdaQueryWrapper`
* [ ] Mongo struct `bson` 标签完整，Projection 满足调用方
* [ ] 无敏感日志；配置走环境变量
* [ ] 单元测试覆盖成功/失败路径

---

## 12. 给 AI 的最终规则（一段话）

> 在本仓库中生成任何代码时，必须：遵循上文工程约定；为关键链路补最小测试；对 Mongo 实体补 `bson` 标签；对用户认证相关查询包含 `password_hash`；不得在 Controller 写业务；输出建议的文件路径与提交信息；确保通过 Lint/Format。

---

# README 片段（把 AI.md 接入工程）

## 如何让 IDE 内置 AI 遵循规范

1. 在项目根目录保留 `AI.md`，并在 `README.md` 顶部加上：

   > **如果你在本项目中使用 AI 辅助生成代码，请先阅读并遵循 `AI.md`。**
2. 在 `.vscode/settings.json` 中加入提示（示例）：

```
{
  "editor.formatOnSave": true,
  "files.eol": "\n",
  "editor.defaultFormatter": "esbenp.prettier-vscode",
  "ai.assistant.instructionsFile": "AI.md"  // 某些插件/Agent 支持自定义说明文件
}
```

3. 在 CI 中添加检查：若 PR 改动包含 Controller/DTO 但缺少 Swagger 注解，或 Mongo 实体缺少 `bson` 标签，则拒绝构建（示例 GitHub Action 略）。

---

# 两周上手计划（IDE 内 AI 训练）

**第 1 周（熟悉 + 改 prompt）**

* Day1：让 AI 生成一个简单 Controller + Service + 单测；你负责把关风格与目录。
* Day2：同一功能重做一遍，刻意训练“要求它输出文件路径与 commit message”。
* Day3：Mongo 实体 + 查询；专门训练 `bson` 标签与 Projection。
* Day4：错误日志与掩码规范；加一条 Lint 规则，逼 AI 通过。
* Day5：把本文件中的“AI 指令”压缩成 8\~10 条 bullet，作为**系统提示**贴进插件。

**第 2 周（实战 + 抗脆弱）**

* Day6：让 AI 生成一条外部系统调用链，强制逐条发送 + 可重试。
* Day7：引入限流/熔断（Resilience4j 或 Go 中间件），AI 生成样例配置。
* Day8：让 AI 写 3 个常犯错的反例并修正，为团队做分享稿（10 张幻灯）。
* Day9：在 CI 接入一条“缺少 bson 标签/Swagger 注解时拒绝 PR”的规则。
* Day10：复盘：记录“AI 写得快但错点在哪里”，沉淀到 `AI.md` 的【常见坑】。

> 坚持两周，你就从“会用 AI”升级到“**能驱动 AI**”。
