# 模拟所得税业务

遵循《所得税接口说明.md》的字段与响应结构。现有登录不变，不新增`/v1/tax-user/login`。所得税App成功返回`code:1,msg:"success",data`；业务参数或查无记录返回HTTP200、code=0、msg提示，服务异常500，身份鉴权沿用现有会员Bearer Token和401行为。App不接受会员ID，只访问本人。

## 路径和协议

- POST `/v1/tax-user/info`：个人资料，缺失生成保存；原`/api/app/tax/profile`同步改为POST别名，GET已移除。
- POST `/v1/tax-user/company/list`：每会员一条单位，返回单元素数组；原`/api/app/tax/companies`同步改为POST别名。
- POST `/v1/tax-user/taxYear/list`：`{year}`，字符串或数字；返回list、totalIncome、totalTax。
- POST `/v1/tax-user/applyInfo`：`{date:年份}`，字符串或数字；返回year、total_income、total_tax、refund_amount、status，金额为JSON数字。
- POST `/v1/tax-user/openRecord/list`：`{}`，返回开具记录数组。
- POST `/v1/tax-user/openRecord/info`：`{yzm}`，仅匹配本人记录，返回images_data。
- POST `/v1/tax-user/taxMsg/list`：`{page,limit}`，page从1开始，limit为1至100，额外返回count总条数。
- POST `/v1/tax-user/taxCountInfo`：`{date:"YYYY-MM",type:"正常工资薪金"}`，type省略使用默认值。

不注册`puppeteerImg`，不生成图片、二维码、PDF或正式凭证。开具记录初始images_data为空，后台可填逗号分隔http/https图片地址；验证码在后台记录中维护，可作为二维码文本`SIM-=验证码`的后半部分。列表不公开验证码。按照用户明确要求，create_time使用东八区`YYYY-MM-DD HH:mm:ss`字符串，不使用原文秒/毫秒时间戳示例，客户端需按字符串展示。

个人资料与单位的App字段改为文档下划线，如card_type、tax_number、tax_back_money、company_name、tax_id、start_date。后台原有资料及单位接口仍保持camelCase及code=0。年度合计、计算明细保留文档指定的混合命名，如totalIncome、susuanCount、nowShenTax，并非擅自改名。

## 数据和一致性

迁移000017创建tax_business_scopes、tax_business_records、tax_business_audits，会员外键，业务payload为JSON，版本保护与审计同事务。金额输入和月度返回为非负十进制字符串，最多9位整数2位小数；计算使用整数分，不使用浮点累计。汇算JSON数字使用json.Number序列化。

| kind | 归属/初始数据 | 后台维护内容 |
| --- | --- | --- |
| monthly | 会员+年度；历史年12个月，当前年截至当前月 | 收入、税额、单位、所得小类、申报信息、各项扣除、模拟税率、速算扣除、类型 |
| applications | 会员+年度，一条 | 申报状态、退税金额 |
| openrecords | 会员，2条 | 开具时间、起止月份、验证码、图片地址 |
| messages | 会员，15条 | 标题、内容、时间 |

年度允许2019至当前年份。完全没有该会员该类/该年度记录才生成；已有数据读库，部分删除不补齐，整组删空后再次App读取重建。首次月度生成复用独立任职单位的名称及编号，后续保存快照，不随单位资料自动覆盖。

年度收入和税额合计从月度记录求和；汇算total_income/total_tax也使用同份合计。计算明细由同年度、同所得小类、截至请求月份的记录汇总；detail_list只返回本期记录。special_deduction为养老/医疗/失业/公积金之和，shenbao_end和nowShenTax对应月度tax。totalYingJiaoTax和totalJiaoTax为已保存模拟月税额累计，totalYiJiaoTax为扣除本期后的此前累计；不执行真实累进税率计算。后台可修改模拟展示rate和susuanCount，它们不会触发真实税额重算。生成默认10%仅为模拟展示。退税金额与个人资料tax_back_money是文档中的独立字段；客户端页面按原文使用个人资料中的对应年份金额。

## 后台

页面`/admin/tax-business.html`，4类表单CRUD、会员搜索选择、年度/关键词筛选、分页、详情及计算预览。月度记录中可编辑计算所需的扣除字段；计算结果不另存一份冲突数据，修改来源后App同步变化。

- GET `/api/admin/tax-business/schemas`：字段说明。
- GET `/api/admin/tax-business/{kind}`：userId、year、q、page、pageSize查询，不生成。
- GET `/api/admin/tax-business/{kind}/{recordID}`：详情。
- POST `/api/admin/tax-business/{kind}`：userId、year、payload；非年度业务year=0。
- PUT `/api/admin/tax-business/{kind}/{recordID}`：完整payload及expectedVersion，会员和年度不能更换。
- DELETE `/api/admin/tax-business/{kind}/{recordID}`：expectedVersion，保留审计。
- GET `/api/admin/tax-business/calculation?userId=...&date=YYYY-MM&type=...`：只预览已保存记录，不初始化。

管理员Cookie鉴权及同源校验，版本冲突409，参数无效400，记录/会员不存在404；JSON最大64KB。会员逻辑删除保留业务记录，但App禁止读取、后台排除已删除会员。
