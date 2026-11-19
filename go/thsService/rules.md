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
- 后台管理 API 使用 `/api/admin` 前缀，与 App 路由及身份隔离。管理员存储于 administrators 表，密码使用与会员相同的 bcrypt DefaultCost；迁移仅保存默认超级管理员 eugene 的密码哈希。当前仅实现登录、身份查询和退出，不提供管理员新增、改密或删除接口。后台页面使用内嵌的原生 HTML/JS 和参考页面的 AdminLTE 资源，入口 /admin/login.html，登录后进入 /admin/index.html；会话使用 HttpOnly、SameSite=Strict Cookie，HTTPS 下启用 Secure。
- JSON 字段统一使用 `camelCase`。
- 时间统一返回 RFC 3339 格式，并明确时区。
- 列表接口根据需要支持 `page` 和 `pageSize`。
- 需要登录的接口从 JWT 中获取用户 ID，禁止信任客户端提交的用户 ID。
- APK下载固定南网、支付宝、个税、同花顺四个位置，各自独立文件和口令；管理端与下载端通过app参数指定位置，下载共用/app-download页面。原单包迁移保留在南网位置。文件小于100MB，无人工版本号，发布标识由服务端时间生成。
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

部署支持通过服务器.env.production中的THS_APP_ENVIRONMENT（development/testing/production）和THS_SWAGGER_ENABLED（true/false）覆盖镜像配置。为了兼容当前测试服务器，Compose未配置时默认testing/true；正式部署应显式配置production/false。生产YAML自身默认关闭Swagger。健康检查及部署探针统一使用/health/ready，不依赖Swagger。发布不覆盖已有.env.production。

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
- 会员新增、后台独立改密和App注册密码至少6个Unicode字符，不限制字符类型，bcrypt输入最多72字节。普通会员资料编辑禁止接受password字段。管理员账户密码规则不受此条调整。
- 会员删除为逻辑删除，users.deleted_at记录删除时间，保留账号、持仓和交易数据。查询默认排除已删除会员；App登录及所有受保护接口须拦截已删除/停用会员，不能仅依赖JWT过期时间。删除后手机号/邮箱继续占用唯一值，不自动释放。
- 已删除会员使用独立菜单和接口查询及恢复，不改变现有会员列表和删除逻辑，不提供物理删除。恢复清空deleted_at并设为正常状态1，保留原ID、密码和关联数据，与restore审计在同一事务内完成，仅允许恢复已删除会员。
- 登录接口应预留限流能力，错误信息不能泄露用户名是否存在。
- 后台登录采用数据库持久化滑动窗口：同IP60秒最多5次，第6次冻结10分钟；同账号60秒内3次失败冻结10分钟并跨IP统计。成功清空账号失败数，不清空IP请求数；冻结期不续冻，不影响已登录会话。429携带Retry-After，重启不清空冻结。使用管理员ID识别账号别名，只有可信代理可影响ClientIP。当前门槛仅作用于后台登录，App既有限流不变。
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

分类模拟持仓接口 `/api/app/market/holdings` 按用户与分类独立保存，每个分类生成8至12个非零持仓，每个随机40至50条模拟交易，不返回quote行情或未持有标的。传入totalAssets时物理删除当前分类旧数据并重新生成；不传且已有记录时直接读库；没有记录时随机生成200000至1000000元总资产再落库。各分类独立使用各自资产，不合并市值，不影响原portfolio接口。现价作为持仓字段保留，内部生成采用独立候选池，并优先复用可用行情价格快照；候选池不扩展行情接口返回内容。总资产=分类总市值+现金。所有交易数量、成本和盈亏须与该分类持仓对应，删除与创建须在同一事务中处理，失败保留原数据。使用demo_category_holdings表的用户与分类联合主键及JSON快照保存完整数据，无历史版本归档；接口中的id仅在分类与generation内唯一。

原portfolio接口继续保持3至5只A股及每只15条交易的已有规则，与上述分类演示数据独立。

后台持仓管理直接维护demo_category_holdings分类快照，与App读取同一份数据。自动生成与App共用8至12个持仓、每个40至50条成交的扩展分类候选池；手动编辑允许0至50个股票及每个1至500条交易。交易按时间计算移动加权成本和已实现盈亏，持仓净数量须与成交一致，禁止卖超、负现金和超出可用数量；总市值、现金和盈亏统一后端计算。所有修改与admin_holding_audits前后快照审计在同一事务，使用会员行锁及expectedGeneration防止并发覆盖。删除整个分类后App读取会重新生成；保存空items表示持久空仓。后台不修改原portfolio表。

模拟用电资料沿用会员登录，App GET /api/app/electricity/profile只识别Token中的会员，不接收目标会员ID。electricity_profiles以user_id主键和外键保证独立一对一，缺失时锁定会员行自动生成广州模拟资料并持久化，已有数据不重新生成；App只读，后台独立CRUD及重新生成。接口字段camelCase对应南网字段说明，id/username派生自会员而不重复存储登录字段。后台改删需expectedVersion，版本冲突返回409，写入与electricity_profile_audits审计同事务；删除资料后App再次读取会重建，会员逻辑删除不物理删除资料。

模拟电费账单GET /api/app/electricity/bills从Token识别会员，一对多保存于electricity_bills，每会员每账期唯一。没有任何记录时生成最近20个已结束月份，已有记录直接返回且不补齐；App只读本人。金额与用电量为十进制字符串、数据库DECIMAL，明细JSON对象。后台支持单条CRUD及整批生成，行version和全集setVersion提供乐观并发保护，所有写入与electricity_bill_audits审计同事务。删除会员不删除账单；账单全部删空后App再次读取会生成20条。金额生成用整数分计算，不调用外部服务。

所得税资料模块独立Swagger分类“所得税资料”和“后台所得税资料”。GET /api/app/tax/profile沿用会员Token，只读本人，缺失时生成一条并持久化，有记录复用。tax_profiles以user_id主键和外键与会员一对一，后台独立生成及CRUD，expectedVersion防并发覆盖，写入与tax_profile_audits同事务。字段按所得税文档第2节并使用camelCase；税务资料姓名/联系方式不修改会员表。taxBackMoney为2019至2023五项金额CSV，分别使用DECIMAL存储；其他年份展示使用第五项。生成证件/纳税人识别号/银行卡带SIM标记，仅模拟，不接入外部税务服务。删除资料后App再次查询会生成，删除会员则保留资料但禁止访问。

任职受雇单位使用独立tax_employments表与会员一对一。GET /api/app/tax/companies只查询本人，缺失生成并保存，已有记录复用，data为单元素数组。后台提供单位生成和CRUD，expectedVersion保护修改、删除和替换，写入与tax_employment_audits同事务。endDate为空表示在职，离职日期不得早于任职日期。单位修改不影响会员及个人所得税资料。删除单位后App查询重新生成；逻辑删除会员保留单位记录但禁止访问。

南网App接口按2026-10-19客户端示例返回字段：资料及手机号查询使用display_name、user_id_number、meter_reader、meter_no、postal_code；手机号查询主参数keyword，兼容username但不可同时传入，并返回完整用电资料，缺失时生成保存。账单使用bill_month、start_date、end_date、total_usage，bill_month为YYYYMM；amount和total_usage返回JSON数字，内部及后台仍使用DECIMAL和字符串。上述App成功外壳为code/msg/data；登录及后台保持原有协议，错误保持统一错误响应。

南网业务11个POST接口保留文档/mp路径，使用会员Token，成功sta=00。资讯和营业厅公共共享，其余按会员隔离。nanwang_records保存8类模拟数据，nanwang_scopes行锁防止重复初始化；完全无记录才生成，部分删除不补齐，筛选无匹配返回空数组，详情不存在404。户号首次生成复用用电资料，后续独立维护。后台/admin/nanwang.html提供表单CRUD，expectedVersion防覆盖，写入与nanwang_audits同事务；会员逻辑删除保留历史但禁止访问。营业厅distance仅模拟，不计算实时距离。不扩展真实绑定、缴费或外部服务调用。

2026-10-19所得税App协议以文档为准：成功code=1/msg/data，资料及单位改为POST和下划线字段，保留原/api/app/tax路径POST别名并接入/v1/tax-user路径，移除GET；后台协议不变。tax_business_records按会员/年度保存月度明细、汇算状态、开具记录和消息，完全缺失生成、部分删除不补齐。年度合计及计算结果从同份月度记录整数分汇总，后台通过修改月度来源管理计算，不重复保存冲突合计。开具create_time返回YYYY-MM-DD HH:mm:ss，图片地址先空，不实现puppeteerImg。后台tax-business.html提供表单CRUD、版本保护、事务审计及计算预览，会员逻辑删除保留历史。混合命名totalIncome/nowShenTax等保留文档要求。

支付宝（天天）模拟模块沿用公共登录和会员Token，不新增/api/login。7个GET路径按文档，成功code=1/msg/data，401也返回msg。alipay_records按会员保存6类独立资料，records按年度多条且同月同单位唯一；缺失生成，已有读库，部分删除不补齐。社保列表与详情共用记录，金额为单位和个人缴纳之和；summary累计值从已保存记录整数分汇总，编号/城市复用参保元数据。全部金额两位小数字符串，日期按YYYYMMDD或YYYYMM。后台alipay.html及左侧各业务菜单提供表单CRUD、版本校验和事务审计。与会员登录资料、南网及所得税独立，不接外部服务或实际支付。会员逻辑删除保留历史但禁止访问。

Swagger入口按App端和后台独立展示，默认App端，业务分类默认折叠。/swagger/app.json和admin.json仅保留本组路径及引用模型；doc.json兼容返回App组。后台说明管理员Cookie鉴权，App说明公共Bearer登录及各业务实际成功码，不强行统一协议。分类集中在internal/apidocs/spec.go，字段说明在fields.go；通用payload字段表从业务Schemas生成，须与真实表单校验一致。新增接口必须归类并通过文档完整性测试。Swagger开关关闭时页面、JSON和静态资源均关闭。

生成业务数据的展示文本采用一般业务文案，不在姓名、单位、地址、消息、资讯、账单说明中附加“模拟、测试、演示、仅供展示”等文字。保留API状态字段、标识编号及接口文档对数据来源的准确说明，不因此改变真实对接状态。历史展示字段清理由000020迁移按白名单处理，先备份原文、修改版本，不更改金额、JSON类型、验证码、账号或审计历史。

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

PDF生成独立后台菜单，取数只读已有业务数据，用户弹窗编辑仅用于当次PDF，不回写原业务记录，不自动生成缺失数据。确认返回PDF attachment直接下载，不存文件、不提供公开链接，仅存导出元数据审计。导出只读素材位置当前active_id指向的approved版本，工作5、电费3/4/7、社保6；图片缺失留空，不读取根目录旧素材。演示报表必须保留来源及演示标识，不复制官方证明或验真声明。金额用精确有理数/整数分计算，单价费用逐行舍入后汇总；社保合计不拆分推算，手填分险种须与月合计一致。最多600行、512KiB及两个并发任务。

PDF素材管理固定3至7号位置，上传版本持久化在数据库，禁止上传即替换生效图片。仅用户名eugene且is_super_admin为true可审核，后端必须验证；审核通过更新位置active_id，驳回保留旧版。保留版本及审计，不默认导入样本素材。实际解码PNG/JPEG并转存PNG以保留透明度、去除元数据；限制5MiB输入、800万像素。后台预览鉴权，后续导出只能用active_id指向的approved版本。素材管理与PDF生成使用独立菜单，导出仅使用生效且已审核版本。

支付宝社保后台可按会员、起止月份和五险规则批量生成缴费记录，一次最多120个月，不生成未来月份。逐险种使用整数分计算并四舍五入，单位/个人合计与App累计汇总一致，不含公积金。生成前提供预览，同会员已有月份跳过；写入和逐条审计同事务。费率参考及适用范围见 `internal/alipay/CONTRIBUTION-GENERATION.md`，历史变动按政策分段，不将一套参考规则当作历年自动征缴规则。

一次功能变更只有同时满足以下条件才算完成：

- 代码能够编译并通过相关测试。
- 数据库变更附带迁移文件。
- 新增或修改的接口包含完整 Swagger 注释。
- 已执行 Swagger 生成命令，并提交更新后的 `docs/` 文件。
- 示例配置和 README 已按需要更新。
- Docker 环境能够正常启动相关功能。
- 未提交密码、密钥、Token 或生产数据库地址。
