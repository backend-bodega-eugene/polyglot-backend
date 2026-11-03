后台入口：`/admin/login.html`，登录后进入 `index.html`。

「PDF生成」入口 `/admin/pdf-reports.html`：选工作信息/用电账单/社保记录、搜索会员、选择已有账单或社保月份范围，然后弹窗编辑。基本字段和明细可修改、增删，编辑仅用于本次导出。确认直接下载attachment PDF，不提供公开链接、不在服务器保存文件。不调用App自动补数据逻辑，不写回任何业务表。只增加pdf_export_audits审计元数据（迁移000023），保存管理员、会员、类型、PDF SHA256、素材版本和行数，不保存临时编辑字段或文件内容。

- `GET /api/admin/pdf-reports/schemas`：三种报表字段和明细定义。
- `GET /api/admin/pdf-reports/bills?userId=...`：会员最近100条已有用电账单。
- `POST /api/admin/pdf-reports/prepare`：kind/userId，电费加billId，社保加startMonth/endMonth（YYYY-MM，最多120个月）。返回fields及tables编辑草稿和缺失提示。
- `POST /api/admin/pdf-reports/download`：提交完整草稿，直接返回application/pdf及Content-Disposition attachment，错误仍返回项目JSON。最多600行及512KiB请求，最多两个并发生成任务。校验会员未删除、只用当前approved且active_id生效素材；客户端不能指定图片路径、素材ID或隐藏演示标识。

三种模板参考样本信息层次，统一为普通演示报表、保留每页演示标识，不复制机构验真声明。工作用5号，电费用3/4/7号，社保用6号；缺失素材留空。图片始终从数据库当前生效版加载，不读根目录png中的旧印章。Noto Sans SC静态中文字体和两张用电示意图内嵌程序，许可证见internal/pdfreport/assets/OFL.txt；原始字体来源google/fonts的ofl/notosanssc，可变字体固定为wght=400。图表目前为固定示意图并明确标注，不代表本期数据。

工作资料缺失的部门、收入、地址由表单填写。电费可编辑抄表行和费用行，有单价时按电量乘单价逐行四舍五入到分，无单价时按手填金额汇总；抄表行存在时本期电量由读数差乘倍率汇总。社保默认仅展示数据库已保存的单位/个人合计，未保存分险种信息不推算；可手动补充，填写时按月份校验分险种金额与原月合计一致。参保月数默认仅按所选范围去重统计。长表自动分页重复表头，中文及透明PNG支持，生成错误保留前端表单。

「PDF素材管理」入口 `/admin/pdf-assets.html`：固定3至7号位置，所有管理员可上传PNG/JPEG及预览。上传进入pending，仅用户名严格为eugene且is_super_admin=true可审核approve/reject；通过替换active_id，驳回不影响当前图片。历史版本及上传/审核审计保留，不提供直接写入生效图片或物理删除接口。未配置的位置为空，不自动导入根目录png素材。PDF导出请使用独立的「PDF生成」菜单。

迁移000022创建pdf_asset_slots、pdf_asset_versions、pdf_asset_audits。图片内容按版本保存在数据库MEDIUMBLOB中，随现有数据库卷持久化，无需文件目录挂载或重新发布即可替换。输入最大5MiB、边长8192像素且总像素800万，必须能实际解码为PNG/JPEG；转存PNG、保留透明度并去除元数据，转存上限10MiB。图片预览须管理员Cookie，不提供公开图片目录。列表返回每个位置最近20版，当前生效版单独返回，即使不在最近20版中仍可显示。

- `GET /api/admin/pdf-assets`：固定位置、生效版本、历史元数据和canReview。
- `POST /api/admin/pdf-assets/slots/{slot}/upload`：multipart/form-data单个file。
- `GET /api/admin/pdf-assets/versions/{assetID}/image`：鉴权PNG预览，包含待审核版，仅用于后台审核，不代表可用于导出。
- `POST /api/admin/pdf-assets/versions/{assetID}/review`：JSON decision=approve/reject、note；驳回理由必填，最多500字。审核与替换、审计同事务；已审核版本返回409。

审核不等于自动真实性/授权认证，eugene需人工核实图片内容及用途。后续PDF导出只能读取固定位置active_id指向的approved版本，不能接收调用方指定任意历史/待审核assetID，缺失时留空。不把模拟资料作为官方证明生成。数据库备份应包含这三张表（含图片内容及历史）。回滚000022会删除素材及审核历史。

发布时执行迁移 000008 创建 administrators 表并初始化默认超级管理员 eugene，密码仅保存 bcrypt 哈希。无管理员创建、改密或删除接口。参考页面资源来自 webexample/web，保留原 LICENSE。

接口：POST /api/admin/auth/login（username、password JSON）、GET /api/admin/auth/me、POST /api/admin/auth/logout。统一 code=0 为成功；登录设置 HttpOnly Cookie，不向前端返回 Token。管理员与会员签名密钥隔离。后台静态资源内嵌在 Go 二进制，无需 Node 服务。

持仓管理入口 `/admin/holdings.html`，首页菜单可进入。列表按会员ID、分类过滤并分页；新增时可以搜索会员。编辑页左侧选择股票，右侧分“股票数据”和“交易记录”两个页签。新增/删除股票、增删改成交、修改资产和估值后统一保存。自动生成会直接保存并替换当前分类，遵守3至5个持仓、每个15条交易的原规则。

后台接口（均使用管理员Cookie）：

- `GET /api/admin/holding-members?q=...`：搜索会员。
- `GET /api/admin/holdings?userId=...&category=...&page=1&pageSize=20`：分类持仓列表。
- `GET /api/admin/holdings/{userID}/{category}`：完整详情，不触发生成。
- `POST /api/admin/holdings/{userID}/{category}`：新建，已有数据返回409。
- `PUT /api/admin/holdings/{userID}/{category}`：全量保存股票和交易。
- `DELETE /api/admin/holdings/{userID}/{category}`：删除当前分类。
- `POST /api/admin/holdings/{userID}/{category}/generate`：指定总资产自动生成。

写入体为 `totalAssets`、`items`、`expectedGeneration`。新建时版本为空；更新、删除及重新生成必须携带详情中的 `account.generation`，冲突返回409。修改与审计在同一数据库事务中完成，与App生成共用会员行锁。

手动保存允许0至50个股票，每个股票1至500条交易。清空items并保存可保留空仓；删除整个分类后App首次查询会重新生成。交易数量须符合最小单位、按时间不得卖超，最终净数量须等于持仓数量。同一时刻先处理买入，再处理卖出。成本使用移动加权平均并含买入手续费，卖出手续费计入已实现盈亏；手续费、价格和成交额为原币，收益、市值及资产为CNY。USD7.10/HKD0.91演示汇率不变。成本、成交金额、盈亏比例、现金和账户汇总由后端重算，客户端的这些派生值不会直接写入。

迁移000009新增 `admin_holding_audits`，保存操作管理员、会员、分类及修改前后的完整快照。不会修改旧portfolio表。集成测试使用临时独立数据库，设置THS_ADMIN_TEST_DSN和THS_ADMIN_TEST_PASSWORD后运行`go test ./internal/admin`。

会员管理入口 `/admin/members.html`，支持关键词、状态和分页查询、查看资料、新增、编辑、独立改密及逻辑删除，可从会员行跳转该会员的持仓列表。

- `GET /api/admin/members?q=...&status=1&page=1&pageSize=20`：会员列表，排除已删除记录。
- `GET /api/admin/members/{userID}`：会员详情。
- `POST /api/admin/members`：新增，realName、phone/email至少一项、status（1正常2停用）、password必填。
- `PUT /api/admin/members/{userID}`：编辑realName、phone、email、status，不接受password字段。
- `PUT /api/admin/members/{userID}/password`：单独提交password修改密码。
- `DELETE /api/admin/members/{userID}`：逻辑删除，写deleted_at并停用，不删除持仓和成交数据。

独立菜单「已删除会员」入口 `/admin/deleted-members.html`，按ID、姓名、手机号、邮箱搜索并分页显示删除时间，仅提供恢复操作，不提供物理删除。原会员管理菜单及逻辑不变。

- `GET /api/admin/deleted-members?q=...&page=1&pageSize=20`：仅返回已逻辑删除会员，不含密码或哈希。
- `POST /api/admin/deleted-members/{userID}/restore`：仅恢复已删除会员，清空deleted_at、设置正常状态1；保留ID、账号、密码和关联数据，恢复与restore审计同事务。已恢复/不存在返回404，无需请求体。恢复后可使用原密码登录，不新增数据库迁移。

会员密码至少6个Unicode字符，不限制字符类型；保留bcrypt的72字节上限，新增、改密和App注册共用校验。密码不去空格、不回显、不写日志；列表/详情不返回哈希。手机号/邮箱沿用会员注册格式，删除后仍占用唯一值。App登录自动排除已删除会员；所有受保护App接口还会检查会员状态，未过期Token也不能绕过停用或逻辑删除。

迁移000010新增users.deleted_at及索引和admin_member_audits操作记录表（不记录密码或哈希）。会员修改、删除及审计同事务；与持仓修改共用会员行锁防止删除后继续写入。

后台登录防护（仅作用于POST /api/admin/auth/login）：

- 同IP滚动60秒允许5次请求（包括登录成功、密码错误和格式错误），第6次起冻结10分钟。
- 同账号滚动60秒内第3次验证失败立即冻结10分钟，跨IP累计。真实管理员按数据库ID归一化，大小写和两端空格不会产生新计数；未知账号也执行同样的限流与通用错误提示。
- 成功验证清空账号失败计数，但不清空IP请求计数。数据库错误不算密码失败。冻结期间即便密码正确也不可登录；重复请求不延长冻结，已登录会话仍可正常使用。
- HTTP429及Retry-After秒数统一提示，不透露是账号还是IP冻结；到期自动恢复。数据库故障时登录返回500，不放行。
- 迁移000011创建admin_login_limits。计数和冻结持久化，事务行锁保护并发与多实例。IP/账号存为带用途前缀的SHA256键，不保存明文密码；到期记录限量清理。限流常量位于login_guard.go。
- 使用Gin ClientIP，只信任server.trustedProxies中的反向代理。开发环境默认不信任代理；生产信任Docker后台网络的Nginx，由Nginx覆盖客户端转发头。不要随意设置为信任所有来源。
