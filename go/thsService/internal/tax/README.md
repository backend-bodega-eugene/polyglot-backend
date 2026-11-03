# 模拟所得税资料

2026-10-19协议更新：App个人资料、单位均改为POST及文档下划线字段，成功code=1/msg/data。文档原路径/v1/tax-user/info、/v1/tax-user/company/list已接入，原/api/app/tax/profile及companies保留POST别名，GET移除。后台协议保持原样。后续业务和完整说明见[所得税业务](../taxbusiness/README.md)，以下原始说明中的App GET/camelCase/code=0约定已被本更新替代。

本模块实现《所得税接口说明.md》第2节个人资料和第3节任职受雇单位。沿用现有会员登录和响应规范（code=0/message/data/requestId），不接入外部税务服务，不新增登录、年度明细或计算接口。

App：`GET /api/app/tax/profile`，现有Bearer Token鉴权，不传用户ID，只读本人。缺少记录时，在会员行锁和事务内生成一条模拟资料并持久化；已有记录直接返回。并发首次查询不会重复生成。会员停用或逻辑删除后不能访问。后台删除资料后App再次查询会生成新记录。

Swagger单独分类：App“所得税资料”，后台“后台所得税资料”。字段采用项目camelCase规范：

| 文档字段 | 当前字段 |
| --- | --- |
| name / sex / birth | name / sex / birth |
| card_type / id_card | cardType / idCard |
| phone / email | phone / email |
| tax_number / bank_num | taxNumber / bankNum |
| bank_type | bankType（银行类型，自定义文本，最多64字符，可空） |
| tax_back_money | taxBackMoney |
| huji_city / huji_address | hujiCity / hujiAddress |
| usually_city / usually_address | usuallyCity / usuallyAddress |
| contact_city / contact_address | contactCity / contactAddress |
| education / nation / country | education / nation / country |

额外返回userId、simulated=true、version、createdAt、updatedAt。日期为YYYY-MM-DD，时间为东八区RFC3339毫秒。

独立表tax_profiles以user_id主键和外键与users一对一。手机号、邮箱、姓名均为税务资料，不修改会员账号及登录密码。模拟生成姓名和联系方式初始参考会员，其余生成广州地区模拟展示内容；证件号、纳税人识别号、银行卡号生成12位随机纯数字字符串，首位非0，无固定前缀，不套用真实证件/银行卡的编码或校验规则。生成后落库复用，后台手工数据和已有记录不自动修改。证件类型仍为类别文本。资料无手机号/邮箱时允许空值，其余字段必填。

taxBackMoney为2019、2020、2021、2022、2023顺序五项非负金额的逗号分隔字符串；其他年份展示时使用第五项。输入最多9位整数2位小数，输出固定2位小数，五项分别存入DECIMAL字段refund2019至refund2023。后台使用五个年份输入框而不是手工拼字符串。

后台入口 `/admin/tax-profile.html`，使用管理员Cookie：

- GET `/api/admin/tax/profiles?q=...&userId=...&page=1&pageSize=20`：分页查询，不自动生成。
- GET `/api/admin/tax/profiles/{userID}`：详情，缺失404。
- POST `/api/admin/tax/profiles/{userID}`：手动新建，已存在409。
- PUT `/api/admin/tax/profiles/{userID}`：提交完整字段及expectedVersion=详情version。
- DELETE `/api/admin/tax/profiles/{userID}`：提交expectedVersion，只删除税务资料，保留会员和审计。
- POST `/api/admin/tax/profiles/{userID}/generate`：不存在传空对象，已存在需expectedVersion，替换生成并保存。

版本冲突409，参数错误400。后台写入与tax_profile_audits前后快照审计同事务，更新不会覆盖其他资料模块；所有写入共享会员行锁以协调初始化与会员逻辑删除。migration000014创建独立资料和审计表。只在本地更新数据库，不自动部署或重打部署包。

## 任职受雇单位

App `GET /api/app/tax/companies` 使用会员Token，只查询本人，不传会员ID。每位会员一个单位，`data` 返回单元素数组。没有记录时在事务中生成并保存，有记录直接返回数据库结果，并发首次查询不会重复生成。

字段对应文档：company_name→companyName、tax_id→taxId、company_num→companyNum、post→post、start_date→startDate、end_date→endDate。日期采用YYYY-MM-DD，endDate为空表示在职，离职日期不能早于任职日期。额外返回userId、simulated、version和创建/更新时间。

独立表tax_employments与会员一对一，不修改会员或个人所得税资料。模拟单位名称与编号带模拟标记。migration000015创建单位及tax_employment_audits审计表。

后台页面 `/admin/employment.html`，Swagger分类“任职受雇单位”和“后台任职受雇单位”：

- GET `/api/admin/tax/companies`：按q、userId分页查询，不生成记录。
- GET/POST/PUT/DELETE `/api/admin/tax/companies/{userID}`：详情、新建、修改、删除。
- POST `/api/admin/tax/companies/{userID}/generate`：生成或替换模拟单位。

修改、删除及替换需expectedVersion，冲突返回409。写入和审计同事务。删除单位后App再次读取会生成；逻辑删除会员后禁止访问，保留单位历史记录。
