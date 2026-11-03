# 南网模拟业务

此模块实现《南网接口说明.md》南网业务部分11个POST接口。保留文档完整`/mp/...`路径，不使用文档中的主机、端口、CAMSID或外部服务，所有接口沿用现有会员Bearer Token。Swagger分类为“南网业务”和“后台南网业务”。成功响应`sta="00",message="success",data`，参数错误400、缺失详情404；业务错误sta不是00。鉴权错误沿用项目统一401响应。

## 数据归属与生成

| kind | 业务 | 初始数据 | 归属 |
| --- | --- | --- | --- |
| news | 热点、分类、最新、详情 | 12条，分类1/2/3，地区050200 | 公共 |
| recommended | 推荐户号 | 1条 | 会员 |
| bound | 已绑定户号 | 1条 | 会员 |
| arrears | 欠费户号 | 1条，模拟欠费36.80 | 会员 |
| points | 计量点 | 1条 | 会员 |
| halls | 附近营业厅 | 2条 | 公共 |
| messages | 未读消息 | 2条 | 会员 |
| charging | 充电桩状态 | 1条，默认无充电桩 | 会员 |

App按整个kind和归属查询，完全无记录才生成并保存；已有记录直接读取，不补齐部分删除。条件筛选无结果返回空数组，不因筛选条件生成新记录。未知资讯详情返回404，不单独造资讯。公共数据所有会员共用；个人数据只取Token对应会员。后台查询不生成数据。会员逻辑删除保留业务数据和审计，但App禁止访问、后台排除已删除会员。

户号相关数据首次生成时复用用电资料里的姓名、户号、地址，资料缺失先创建。推荐、绑定、欠费、计量点分别保存，后台可独立维护；修改用电资料不会自动覆盖这些已保存记录。欠费不是由电费账单推算，属于独立模拟展示值。不提供文档没有列出的绑定、解绑、缴费或消息已读操作。

资讯按publishTime倒序。热点默认3条，最新默认10条，count最大100；分类列表pageNo默认1、pageSize默认10且最大100。areaCode默认050200，可后台录入其他地区；classId可后台维护，未知分类/地区返回空数组。详情id取列表返回的字符串编号。正文为纯文本；图片、链接可空，仅接受http/https URL。

营业厅经纬度须为字符串，范围正确；返回后台维护的模拟distance，不连接地图或计算实时距离。计量点可用eleCustId筛选本人记录。充电桩每位会员最多一条。App对公开业务字段补充字符串id，未知字段客户端可忽略。

## 后台管理

页面`/admin/nanwang.html`，切换8种类型，支持关键词、会员ID筛选、分页、详情、新增、编辑、删除，会员数据新增时搜索选择会员。使用中文字段表单，无须手写JSON。营业厅距离、欠费等十进制值保持文档字符串；阅读数为整数，充电桩标志为布尔值。

- GET `/api/admin/nanwang/schemas`：类型、归属与表单字段。
- GET `/api/admin/nanwang/{kind}`：q、userId、page、pageSize查询。
- GET `/api/admin/nanwang/{kind}/{recordID}`：详情。
- POST `/api/admin/nanwang/{kind}`：`{userId,payload}`，公共数据userId=0。
- PUT `/api/admin/nanwang/{kind}/{recordID}`：完整payload和expectedVersion，不可更换所属会员。
- DELETE `/api/admin/nanwang/{kind}/{recordID}`：expectedVersion。

所有后台接口管理员Cookie鉴权及同源校验，冲突409，输入无效400，记录缺失404。JSON限64KB，不接受未知字段。业务写入和nanwang_audits快照审计同一事务。后台删除业务记录，保留会员及审计；整类删空后App会重新生成。

迁移000016创建nanwang_scopes、nanwang_records、nanwang_audits；公共owner_id=0，会员owner_id取users.id。用户存在性在事务内检查并锁定用户行；scope唯一键和行锁协调并发首次生成与后台写入。无须更改现有资料或账单表。测试使用隔离MySQL数据库，覆盖11路由、8类CRUD、并发生成、空集合再生、部分删除、版本冲突、鉴权与数据隔离。
