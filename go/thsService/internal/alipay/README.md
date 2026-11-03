# 支付宝（天天）模拟业务

按《支付宝接口说明.md》实现7个GET接口。登录共用现有会员登录及Token，不新增`/api/login`或其他登录接口。不访问支付宝、社保、水电单位或文档中的外部地址，不执行支付。

App成功返回`{code:1,msg:"success",data}`，业务参数错误或详情无匹配返回HTTP200、code=0、msg。缺少/失效Token、停用或删除会员返回HTTP401及msg。使用现有TokenIssuer验签，会员状态从现有users表校验，不新增账号体系。只取Token中的会员ID，查询参数不能指定其他会员。

| 方法与路径 | 返回数据 |
| --- | --- |
| GET `/api/user/profile` | name、phone、accountName、idNo |
| GET `/api/insurance/summary` | manageCode、cityName、status、firstDate、lastMonth、lastCompany、monthCount、personalAmount、companyAmount、interestAmount |
| GET `/api/insurance/records?year=2025` | manageCode、cityName、year、list；每条month/company/amount/type |
| GET `/api/insurance/record?year=2025&month=202501&company=...` | year（YYYYMM）、unitName、type、base、total、identity、companyPay、personalPay |
| GET `/api/life/accounts` | left/right对象，每侧name/accountNo |
| GET `/api/utility/electric` | payee、amount、accountNo、accountName、address、supplier |
| GET `/api/utility/water` | amount、accountNo、accountName、addressLine1、addressLine2、supplier |

字段大小写按文档，不擅自换成下划线。全部金额为两位小数字符串，保存和累计采用整数分及十进制字符串，不用浮点累计。首次参保日期YYYYMMDD、最后参保月份与缴费所属期YYYYMM。年度允许2000至当前年份；year、month、company必填时不接受重复参数。

## 初始化和关联

六类数据：profile（我的与账户）、insurance（参保元数据）、records（缴费记录及详情）、accounts（生活户号）、electric、water。每位会员各类一条，records按会员+年度一对多，同月同单位唯一。历史年度初始12条，当前年度生成到当前月。完全缺失才生成并保存；已有数据直接读库，不补齐部分删除，整类/整年删空后App再次读取重新生成。详情先保证该年度有记录，再按年月和单位查找，未知单位不单独造记录。

列表与详情共用记录，amount/total=companyPay+personalPay。records列表和summary复用insurance的manageCode/cityName。summary从所有已保存年度的缴费记录汇总个人与单位累计金额、最早所属期的月初日期、最后月份/单位，以及去重月份数；interestAmount独立后台维护。不为计算累计值自动创建全部历史年度。如果一个缴费记录都没有，summary先生成当前年度。

profile首次参考会员姓名和手机号，但之后独立维护，不改users或密码。证件号、户号和个人编号含SIM标记；账户展示姓名使用模拟脱敏。左右生活户号和水电费初始使用一致的模拟户号与户名，后续独立维护，不相互覆盖；右侧户名、户号可同时留空。所有资料与南网、所得税模块独立，修改不会影响它们。

## 后台

页面`/admin/alipay.html`，左侧“支付宝模拟业务”下分别提供6个业务入口，点击自动选择业务；页面内保留业务下拉切换和地址栏kind选择。支持会员搜索选择、年度/关键词筛选、分页、详情、新增、编辑、删除，使用中文字段表单，不需要编辑JSON。

- GET `/api/admin/alipay/schemas`：字段定义。
- GET `/api/admin/alipay/{kind}`：userId、year、q、page、pageSize查询，不生成资料。
- GET `/api/admin/alipay/{kind}/{recordID}`：详情。
- POST `/api/admin/alipay/{kind}`：userId、year、payload；仅records需年度，其余year=0。
- PUT `/api/admin/alipay/{kind}/{recordID}`：完整payload及expectedVersion，不能更换所属会员/年度。
- DELETE `/api/admin/alipay/{kind}/{recordID}`：expectedVersion，保留审计。

管理员Cookie鉴权及同源校验，参数无效400、缺失404、版本/唯一性冲突409。JSON限64KB，不接受未知业务字段。参保汇总中的累计金额和日期通过修改缴费记录管理，不独立保存另一套冲突值。后台仍使用项目code=0/message/data协议。

迁移000018创建alipay_scopes、alipay_records、alipay_audits。用户外键、作用域行锁和会员行锁协调并发生成及后台写入；数据和快照审计同事务。会员逻辑删除保留资料及审计，但禁止App读取、后台查询排除已删除会员。

隔离MySQL集成测试覆盖7接口、6类CRUD、金额联动、重复读取稳定性、并发首次生成、部分删除不补齐、删空重建、公共Token鉴权、逻辑删除隔离及不影响会员与南网资料。
