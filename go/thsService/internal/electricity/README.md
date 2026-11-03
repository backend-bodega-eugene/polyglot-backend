# 模拟用电资料

## 2026-10-19 App客户端字段兼容更新

App成功响应使用`code:0,msg:"success",data`，保持现有登录和鉴权，保持本人资料与账单接口不传用户ID。下述旧说明的camelCase和金额字符串仅适用于后台及内部模型，App以本节为准。

- GET `/api/app/electricity/profile`：data字段为id、username、display_name、address、user_id_number、supplier、meter_reader、meter_no、postal_code，与客户端示例一致。
- GET `/api/app/users?keyword=手机号`：精确查询users.phone，返回`data.list`，元素与上述资料字段一致。兼容旧username参数；两个参数不能同时使用，不能重复。资料缺失时生成保存，无正常会员匹配返回空数组。
- GET `/api/app/electricity/bills`：data数组元素为code、bill_month、start_date、end_date、amount、total_usage、detail；code/bill_month为YYYYMM，金额和用电量为JSON数字，detail为对象。数据库仍为DECIMAL，后台仍使用十进制字符串，App序列化使用json.Number保留十进制数值。

不调整后台接口字段，不修改数据库结构，不实现其他南网业务接口。错误响应仍沿用项目统一错误结构。

## 初始实现说明（App字段以后续更新为准）

沿用现有会员登录，所有数据仅供模拟展示，不请求原说明中的外部服务，不新增登录系统或电费账单。

按手机号查询会员：`GET /api/app/users?username=13900000031`，使用现有会员Bearer Token，精确匹配users.phone而不是邮箱或独立资料表。正常且未删除的会员返回`data.list`，最多一条，字段id、username（手机号）、displayName（会员姓名）；无匹配返回空数组。手机号格式错误或重复username参数返回400。不会生成用电资料，不返回密码。

App调用 `GET /api/app/electricity/profile`，携带 `Authorization: Bearer <会员Token>`，不传用户ID。只读取Token对应的本人资料，没有App编辑接口。首次缺失时在事务中锁定会员行、生成广州地区模拟资料并写入数据库；后续读取复用，后台删除后App再次读取会重新生成。

独立表 `electricity_profiles` 以user_id为主键并外键关联users，一位会员最多一条资料，不修改users中的登录资料。username来自会员手机号（优先）或邮箱，id/userId均为会员ID，不是另一个登录账号。展示姓名首次取会员姓名，后续独立维护。会员逻辑删除后保留资料，但禁止App访问，后台列表也排除已删除会员。已停用会员仍可由后台维护资料。

按本项目camelCase规范，对照《南网接口说明.md》：

| 原字段 | 当前响应字段 | 来源 |
| --- | --- | --- |
| id | id、userId | 当前会员ID |
| username | username | 当前会员手机号/邮箱，只读 |
| display_name | displayName | 独立展示姓名 |
| address | address | 独立用电地址 |
| user_id_number | userIdNumber | 户号，字符串，保留前导零 |
| supplier | supplier | 供电单位 |
| meter_reader | meterReader | 抄表员 |
| meter_no | meterNo | 表号，字符串，保留前导零 |
| postal_code | postalCode | 邮编，字符串 |

统一响应 code=0，data包含上述字段及simulated=true、version、createdAt、updatedAt。时间为东八区RFC3339毫秒格式。户号为11至32位数字，表号6至32位数字，邮编6位数字；展示姓名/抄表员最多64字符，供电单位128字符，地址255字符，均必填。

后台入口 `/admin/electricity.html`，使用现有管理员Cookie：

- GET `/api/admin/electricity/profiles?q=...&userId=...&page=1&pageSize=20`：分页查询已有资料，不生成。
- GET `/api/admin/electricity/profiles/{userID}`：详情，不存在404。
- POST `/api/admin/electricity/profiles/{userID}`：创建，body为7项业务字段，已存在409。
- PUT `/api/admin/electricity/profiles/{userID}`：全量修改7项字段，另传expectedVersion（详情version）。
- DELETE `/api/admin/electricity/profiles/{userID}`：body传expectedVersion，只删除资料，不删除会员。
- POST `/api/admin/electricity/profiles/{userID}/generate`：没有资料传空对象；已有资料需expectedVersion，重新生成并保存。

版本冲突409，字段错误400，所有后台写操作与修改前后快照审计同事务完成。migration000012新增资料表和electricity_profile_audits审计表。后台页面可选择已有会员、自动生成或手动填写，并支持查询和编辑；自动生成会立即保存，普通表单修改需点保存。

## 模拟电费账单

App `GET /api/app/electricity/bills`，携带现有会员Token，不传会员ID，仅本人可读。响应data为账单数组，按code账期倒序。没有任何记录时生成最近20个已结束月份并存库，重复读取复用数据库；已有19条就返回19条，不补齐。后台删除全部后，App再次查询重新生成20条。与独立用电资料表互不依赖，查询账单不生成资料。

一位会员多条账单，electricity_bills.user_id外键关联users。同一会员相同code（YYYYMM）只允许一条。字段采用camelCase对应原文档：code账期、billMonth（从code派生YYYY-MM）、startDate/endDate（YYYY-MM-DD字符串）、amount（人民币元）、totalUsage（kWh）、detail（JSON对象）。另有id、userId、version、simulated=true和创建更新时间。金额和用电量为非负十进制字符串，最多9位整数和2位小数，detail最多8KB。不生成真实缴费或付款行为。

默认用电量50.00至700.00 kWh，新生成按深圳居民夏季/非夏季三档电价拆分，detail.billing保存计费快照。App与后台读取均额外返回billing对象，包含三档电量、单价、金额、缴费截止日、违约金起始日及金额等。后台表单可独立修改各项数值，不校验合计、单价乘积或缴费状态关系。历史数据不自动重算或补写日期。字段及接入示例见[BILLING.md](BILLING.md)。起止日期仍沿用原有规则；账期支持2000至2100年合法月份。

后台入口 `/admin/bills.html`：关键词、会员ID、账期筛选和分页，新增或编辑单条、删除单条、为选定会员生成20条。接口均需管理员Cookie：

- GET `/api/admin/electricity/bills?userId=...&code=YYYYMM&q=...&page=1&pageSize=20`：已有账单列表，不生成。指定userId时返回该会员全部账单的setVersion（与分页和月份筛选无关）。
- GET `/api/admin/electricity/bills/{userID}/{billID}`：单条详情，必须同时匹配会员与账单ID。
- POST `/api/admin/electricity/bills/{userID}`：新增单条。
- PUT `/api/admin/electricity/bills/{userID}/{billID}`：修改单条，提交完整字段及expectedVersion=该条version。
- DELETE `/api/admin/electricity/bills/{userID}/{billID}`：删除单条，提交expectedVersion=该条version。
- POST `/api/admin/electricity/bills/{userID}/generate`：替换该会员全部账单并生成20条，expectedVersion=该会员列表setVersion；没有账单可传空对象。版本过期409，防止并发覆盖。

迁移000013创建electricity_bills及electricity_bill_audits。会员行锁保证初始化及后台写入串行，写入与修改前后的完整账单审计同事务。会员逻辑删除会保留账单，但App禁止访问、后台查询排除该会员。资料、持仓、会员密码均不被账单写入更改。
