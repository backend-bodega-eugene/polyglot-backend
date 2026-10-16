# thsService 项目开发规则

本文档是 `thsService` 的项目约定。后续新增、修改和审查代码时，均应遵守本文档；若需求发生变化，应先同步更新本文档。

## 1. 项目定位

- 本项目是使用 Go 开发的单体 HTTP API 服务。
- Go 版本使用当前开发环境的 Go 1.26.4。
- 数据库使用 MySQL 8.0。
- 暂不引入微服务、消息队列、Redis、服务注册中心等额外组件。
- 最终使用 Docker 部署，并使用 Docker Compose 管理 API 与 MySQL。
- 第一阶段以模拟行情、资讯、资产和交易功能为目标，不接入真实证券交易。
- 现有接口需求以 `接口清单.md` 为业务参考；实现时统一整理为版本化 API。

## 2. 技术选型

- HTTP 框架：Gin
- ORM：GORM
- 数据库迁移：golang-migrate
- 日志：Go 标准库 `log/slog`，使用结构化 JSON 日志
- 配置：环境变量为主，可使用 YAML 提供本地默认配置
- 身份认证：JWT Access Token
- 密码哈希：bcrypt 或 Argon2id，禁止保存明文密码
- 接口文档：Swagger/OpenAPI，使用 swaggo 生成
- 测试：Go 标准库 `testing`

## 3. 推荐目录结构

```text
thsService/
├── cmd/
│   └── server/
│       └── main.go
├── internal/
│   ├── config/
│   ├── middleware/
│   ├── response/
│   ├── auth/
│   ├── home/
│   ├── quote/
│   ├── news/
│   ├── wealth/
│   ├── watchlist/
│   ├── portfolio/
│   └── trade/
├── migrations/
├── docs/
├── configs/
│   └── config.example.yaml
├── Dockerfile
├── compose.yaml
├── go.mod
└── README.md
```

各业务模块按需要包含以下文件：

```text
handler.go       # HTTP 参数解析、校验和响应
service.go       # 业务逻辑
repository.go    # 数据库读写
model.go         # 数据库模型
dto.go           # 请求与响应结构
```

保持简单分层，不为了形式增加无实际作用的接口或抽象。

## 4. API 规范

- App 端 API 统一使用 `/api/app` 前缀。
- 后台管理 API 以后使用 `/api/admin` 前缀，并与 App 路由分组隔离；当前阶段不实现后台管理接口。
- JSON 字段统一使用 `camelCase`。
- 时间统一返回 RFC 3339 格式，并明确时区。
- 列表接口根据需要支持 `page` 和 `pageSize`。
- 需要登录的接口从 JWT 中获取用户 ID，禁止信任客户端提交的用户 ID。
- HTTP 状态码表达传输层结果，业务错误同时返回稳定的业务错误码。
- 正常响应统一为：

```json
{
  "code": 0,
  "message": "ok",
  "data": {},
  "requestId": "..."
}
```

- 错误响应统一为：

```json
{
  "code": 40001,
  "message": "用户名或密码错误",
  "data": null,
  "requestId": "..."
}
```

- 自选操作优先使用幂等接口：

```text
PUT    /api/app/watchlist/{code}
DELETE /api/app/watchlist/{code}
```

- 不使用容易因重复请求产生状态反转的 `toggle` 作为最终接口设计。

## 5. Swagger/OpenAPI 规则

Swagger 是本项目交付内容的一部分。每次新增或修改 HTTP 接口时，必须同步新增或更新 Swagger 注释和生成文件，不能只修改接口实现。

使用以下组件：

- `github.com/swaggo/swag`
- `github.com/swaggo/gin-swagger`
- `github.com/swaggo/files`

Swagger 注释至少包含：

- `@Summary`
- `@Description`（存在需要说明的业务规则时）
- `@Tags`
- `@Accept`
- `@Produce`
- `@Param`
- `@Success`
- 可能出现的 `@Failure`
- `@Security BearerAuth`（需要登录时）
- `@Router`

生成命令统一为：

```bash
swag init -g cmd/server/main.go -o docs --parseDependency --parseInternal
```

生成结果至少包括：

```text
docs/docs.go
docs/swagger.json
docs/swagger.yaml
```

开发环境提供 Swagger UI：

```text
GET /swagger/index.html
```

生产环境通过配置决定是否开启 Swagger。提交接口代码前，应确认 Swagger 能成功生成、Swagger UI 能打开，接口路径、参数、响应模型和鉴权要求与实际实现一致。

## 6. 数据库规则

- 数据库使用 MySQL 8.0 和 `utf8mb4` 字符集。
- 表结构变更必须通过 `migrations/` 中的版本化迁移完成。
- 不依赖 GORM AutoMigrate 作为生产环境的表结构发布方式。
- 金额、价格、费率和比例在数据库中使用 `DECIMAL`，禁止使用 `FLOAT` 或 `DOUBLE` 保存财务数据。
- 时间字段统一约定时区并记录创建、更新时间。
- 需要唯一性的业务数据必须建立唯一索引，例如用户名、手机号、自选股用户与股票组合。
- 查询条件、排序和关联字段应建立必要索引。
- 删除重要业务数据时优先保留审计记录，不直接物理删除交易流水。

初始核心表建议包括：

```text
users
stocks
watchlists
orders
trades
positions
position_lots
cash_flows
home_hot_picks
home_feed_items
news_articles
wealth_products
```

## 7. 认证与安全

- 登录成功后签发 JWT，密钥只能从环境变量或安全配置中读取。
- 密码必须经过 bcrypt 或 Argon2id 哈希后保存。
- 登录接口应预留限流能力，错误信息不能泄露用户名是否存在。
- 对所有外部输入进行校验，包括字符串长度、枚举值、价格、数量和分页范围。
- 日志中禁止记录密码、Token、完整身份证件、银行卡等敏感信息。
- SQL 查询使用参数绑定，禁止拼接不可信输入。
- CORS 允许来源通过配置管理，生产环境禁止无条件允许所有来源。

## 8. 模拟交易规则

- 买入和卖出必须使用数据库事务。
- 事务中锁定用户资金账户及相关持仓，防止并发请求造成超买、超卖或负余额。
- 使用订单号或请求幂等键防止重复下单。
- 买入数量必须是 100 的整数倍，价格必须大于 0。
- 卖出执行 T+1，当日买入数量不可卖。
- 佣金、最低佣金、印花税和每手数量使用配置管理，不散落在 handler 中。
- 当前默认规则：佣金率 `0.00025`、最低佣金 5 元、卖出印花税率 `0.001`、每手 100 股。
- 资金、订单、成交、持仓批次和资金流水必须能够相互追溯。
- 交易失败必须整体回滚，不允许只更新部分表。

演示持仓接口属于数据生成场景：默认总资产为人民币 200000 元，首次访问自动落库，传入总资产时生成新的模拟快照。整个账户共持有3至5只A股，每只固定15条模拟买卖成交，不计算手续费，不执行真实下单。总资产等于总市值加可用现金，可取现金等于可用现金。旧版模拟持仓和成交保留在数据库，接口仅展示当前快照。

分类模拟持仓接口 `/api/app/market/holdings` 按用户与分类独立保存，每个分类生成3至5个非零持仓，每个固定15条模拟交易，不返回quote行情或未持有标的。传入totalAssets时物理删除当前分类旧数据并重新生成；不传且已有记录时直接读库；没有记录时随机生成200000至1000000元总资产再落库。各分类独立使用各自资产，不合并市值，不影响原portfolio接口。现价作为持仓字段保留，内部生成时复用行情服务。总资产=分类总市值+现金。所有交易数量、成本和盈亏须与该分类持仓对应，删除与创建须在同一事务中处理，失败保留原数据。使用demo_category_holdings表的用户与分类联合主键及JSON快照保存完整数据，无历史版本归档；接口中的id仅在分类与generation内唯一。

原portfolio接口继续保持3至5只A股及每只15条交易的已有规则，与上述分类演示数据独立。

## 9. 日志与错误处理

- 每个请求生成或透传 `requestId`。
- 结构化日志至少记录请求 ID、方法、路径、状态码、耗时和错误信息。
- 业务层返回可识别的业务错误，handler 负责映射 HTTP 状态码和统一错误响应。
- 不向客户端返回数据库错误、堆栈或内部实现细节。
- 对 panic 使用恢复中间件记录错误并返回统一的 500 响应。

## 10. 测试与质量要求

- 提交前运行 `gofmt`、`go vet ./...` 和 `go test ./...`。
- handler 测试覆盖参数校验、鉴权和响应结构。
- service 测试重点覆盖资金不足、重复请求、T+1、手续费、超卖和事务回滚。
- repository 集成测试使用独立测试数据库，不能连接生产数据库。
- 每次接口变更还必须重新生成 Swagger，并检查生成文件已同步更新。

## 11. 配置规则

- 配置支持环境变量覆盖，禁止把密码、JWT 密钥等秘密提交到仓库。
- 提供 `.env.example` 或 `config.example.yaml`，只放示例值。
- 配置至少包括：服务地址、MySQL DSN、JWT、日志级别、CORS、Swagger 开关和交易费率。
- 应区分开发、测试和生产环境。

## 12. Docker 部署

- Dockerfile 使用多阶段构建，最终镜像只包含运行所需文件和编译后的二进制。
- 容器内使用非 root 用户运行服务。
- 提供 `compose.yaml`，至少包含 `api`、`mysql` 和一次性 `migrate` 服务。
- API 在数据库就绪且迁移成功后启动。
- 提供以下健康检查：

```text
GET /health/live
GET /health/ready
```

- `live` 只表示进程可用；`ready` 应检查数据库等必要依赖。
- 生产部署不应由多个 API 实例同时执行数据库迁移。

## 13. 推荐开发顺序

1. 初始化 Go 模块、配置、日志、路由、统一响应和错误处理。
2. 添加 Docker Compose、MySQL、数据库迁移和健康检查。
3. 接入 Swagger UI，并建立接口注释和生成流程。
4. 实现一个只读接口作为完整样板。
5. 实现注册、登录、JWT 和用户自选。
6. 实现首页、行情、资讯和理财等只读接口。
7. 实现账户、持仓、模拟买入、模拟卖出和成交记录。
8. 补充交易测试、部署配置和运行文档。

## 14. 代码变更完成标准

一次功能变更只有同时满足以下条件才算完成：

- 代码能够编译并通过相关测试。
- 数据库变更附带迁移文件。
- 新增或修改的接口包含完整 Swagger 注释。
- 已执行 Swagger 生成命令，并提交更新后的 `docs/` 文件。
- 示例配置和 README 已按需要更新。
- Docker 环境能够正常启动相关功能。
- 未提交密码、密钥、Token 或生产数据库地址。
