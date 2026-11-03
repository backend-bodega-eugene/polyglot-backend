package apidocs

import (
	"strings"
	"ths-service/internal/alipay"
	"ths-service/internal/nanwang"
	"ths-service/internal/taxbusiness"
)

// Field descriptions live beside the documentation renderer so regenerated Swagger
// automatically receives the same units and business labels as the admin forms.
var fieldNotes = map[string]string{
	"bankType":     "银行类型，自定义文本，最多64个字符；未填写返回空字符串。",
	"region":       "账单所属地区，例如深圳市。",
	"billing":      "结构化计费快照；App和后台读取均返回。后台写入detail.billing，金额均为十进制字符串。历史缺失日期返回空字符串，不自动重算历史金额。",
	"pricing_mode": "计费方式：tiered三档阶梯、flat单一电价或历史账单。", "tariff_name": "该账期保存的计价方案名称。", "usage_type": "用电类别。", "tiers": "按1、2、3档顺序排列；自动生成三条；手工数据按录入值保存，不校验档位合计。", "level": "阶梯档位，1至3。", "usage": "该档计费电量，kWh，十进制字符串。", "unit_price": "该档单价，元/kWh，最多8位小数。", "electricity_fee": "电费合计，元，十进制字符串，可独立录入，不强制与账单amount或各档合计一致。", "average_price": "平均电价，元/kWh，最多8位小数，按保存值返回。", "payment_status": "缴费状态：paid已缴清、unpaid未缴费、overdue逾期；历史未录入为空。", "due_date": "缴费截止日，YYYY-MM-DD，未录入为空。", "late_fee_start_date": "违约金起始日，YYYY-MM-DD，未录入为空，不校验与截止日先后关系。", "late_fee_as_of_date": "违约金计至日期，YYYY-MM-DD；金额为保存快照，不随查询日增长。", "arrears_principal": "欠费本金，元，非负字符串。", "late_fee_daily_rate": "日违约金率，0.001表示千分之一；仅保存展示，不自动执行收费。", "late_fee": "违约金，元，非负字符串。", "payable_amount": "账单应付总额，元，独立保存；不校验与电费、违约金的加总关系。",
	"startMonth": "起始月份，YYYY-MM，包含本月；社保取数范围从2000年起。", "endMonth": "结束月份，YYYY-MM，包含本月；社保取数不能晚于当前月份，区间最多120个月。",
	"billId": "本次选择的电费账单ID，必须属于所选会员，仅电费类型必填。", "columns": "可编辑明细表的列定义。", "tables": "报表明细表：schemas中是表结构数组，draft中是按表key保存的行数组对象；编辑仅用于本次导出。", "warnings": "草稿数据缺失或统计口径提示，不写入业务数据。",
	"slot": "固定素材位置，只允许3、4、5、6、7。", "activeId": "该位置当前生效的已通过版本ID，没有时为null。", "active": "当前已通过且生效的素材元数据，无素材时为null。", "versions": "该位置最近20个上传版本，倒序排列，不包含图片二进制。", "slots": "3至7号素材位置及各自生效图片、最近版本。", "canReview": "当前登录账号是否为eugene且是超级管理员；后端同时校验审核权限。", "originalName": "原始上传文件名，仅作展示，不作为存储路径。", "width": "图片宽度，单位像素。", "height": "图片高度，单位像素。", "sha256": "转存PNG后图片内容的SHA256摘要。", "uploadedBy": "上传管理员ID。", "reviewedBy": "审核管理员ID，未审核时为null。", "reviewNote": "审核备注或驳回理由。", "reviewedAt": "审核时间，RFC3339日期时间，未审核时为null。", "decision": "审核决定，只允许approve通过或reject驳回。", "note": "审核备注，最多500字符，驳回时必填。",
	"deletedAt": "会员逻辑删除时间，RFC3339日期时间；仅已删除会员列表返回。",
	"salary":    "月薪，单位元，正数十进制字符串；未单独填写险种基数时使用月薪。", "rules": "养老、医疗、失业、工伤、生育五项缴费规则；同一组规则应用整个起止月份区间。", "companyRate": "单位缴费比例，百分数十进制字符串，例如16表示16%。", "personalRate": "个人缴费比例，百分数十进制字符串，例如8表示8%；工伤、生育必须为0。", "months": "起止区间包含的月份数量，最多120个月。", "created": "实际新增条数；预览接口为预计新增条数。", "skipped": "已有缴费记录而跳过的月份数量；不覆盖任何已有单位的记录。", "breakdown": "五险每月逐项计算结果，各项四舍五入到分后再求和。",
	"id": "记录编号；详情查询使用此编号。", "userId": "所属会员ID；App从Token识别本人，后台维护时选择会员。", "username": "登录名；具体接口为管理员账号或待查询的会员手机号。", "account": "登录账号（中国大陆手机号或邮箱）；持仓响应中为账户汇总对象。", "password": "密码。会员新增/修改密码至少6个字符、最多72字节，不限制字符类型；普通资料编辑不接收此字段。", "realName": "会员真实姓名，2至64个字符。", "phone": "手机号字符串，保留完整号码；独立模拟资料修改不改变登录账号。", "email": "邮箱地址；允许为空的资料字段可传空字符串。",
	"accessToken": "公共会员登录返回的访问Token，放入Authorization请求头，格式Bearer加空格及Token。", "tokenType": "Token类型，固定Bearer。", "expiresIn": "会话或Token有效期，单位秒。", "user": "登录会员的基本资料。", "admin": "当前管理员信息。", "isSuperAdmin": "是否顶级管理员。", "lastLoginAt": "最近登录时间，RFC3339日期时间；从未登录可为null。", "createdAt": "创建时间，东八区RFC3339毫秒日期时间。", "updatedAt": "最后更新时间，东八区RFC3339毫秒日期时间。", "requestId": "请求追踪编号，用于服务日志排查。", "message": "提示信息；后台及项目标准响应成功通常为ok，南网业务成功为success。", "msg": "可直接展示的业务提示文案；成功通常为success。", "data": "业务返回数据，结构见所属接口的响应模型。", "sta": "南网业务状态码，字符串00表示成功，其他值表示失败。",
	"version": "当前记录版本。修改或删除时作为expectedVersion提交。", "expectedVersion": "乐观锁版本：修改、删除、覆盖生成时传入详情version，新建通常省略或传空字符串。冲突返回409。", "generation": "本套模拟持仓的生成批次标识。", "expectedGeneration": "编辑持仓时提交当前generation，防止覆盖其他操作产生的新批次。", "setVersion": "账单整组版本；重新生成整批账单时作为expectedVersion提交。", "simulated": "是否模拟展示数据；true表示模拟生成或后台维护的数据。", "kind": "业务类型，允许值及对应payload字段见接口说明和schemas接口。", "payload": "该kind的业务字段对象。完整字段表见对应后台写入接口说明；拒绝未知字段。", "year": "年度；参数通常为四位年份，社保详情中的year展示值为YYYYMM。具体类型和范围见接口说明。", "date": "所得税年份或所属期参数：汇算接口为年份，计算明细接口为YYYY-MM。",
	"page": "页码，从1开始。", "pageNo": "页码字符串，从1开始。", "pageSize": "每页条数，通常1至100，默认值见接口。", "limit": "每页条数；税务消息接口为1至100。", "total": "记录总数；社保详情中为单位缴纳与个人缴纳合计（元，两位小数字符串）。", "count": "总条数或请求条数字符串，含义见当前接口。税务消息响应中为分页前的总条数。", "items": "当前页记录数组，无记录时为空数组。", "list": "业务记录数组，无记录时为空数组。", "fields": "该业务的可编辑字段定义，用于后台表单及payload校验。", "key": "字段的JSON属性名。", "label": "字段中文名称。", "required": "此字段是否必填。", "public": "是否公共数据；true时owner为公共作用域，后台userId=0。", "yearly": "是否按会员与年度分别保存。", "singleton": "是否每个作用域只允许一条记录。",
	"name": "展示姓名或标的名称，具体含义见业务模型。", "displayName": "独立用电资料展示姓名。", "accountName": "账户或缴费户名，可为模拟脱敏姓名。", "idNo": "账户页展示证件号，可为脱敏/模拟字符串。", "address": "用电或户号地址。", "addressLine1": "水费地址第一行。", "addressLine2": "水费地址第二行，没有时返回空字符串。", "supplier": "供电或供水单位。", "payee": "模拟收款单位名称，不执行真实缴费。", "accountNo": "生活缴费、水费或电费户号，字符串，保留前导零及脱敏符号。", "left": "生活缴费左侧户对象，包含name和accountNo。", "right": "生活缴费右侧户对象；没有第二户时两个字段均为空字符串。", "meterReader": "抄表员姓名。", "meterNo": "表号，字符串，保留前导零。", "postalCode": "6位数字邮政编码字符串。", "userIdNumber": "用电户号，字符串，保留前导零。", "billMonth": "账期；后台为YYYY-MM，App下划线bill_month为YYYYMM。", "startDate": "业务起始日期：账单计费起始或任职日期，YYYY-MM-DD。", "endDate": "业务结束日期，YYYY-MM-DD；单位在职时可为空。", "totalUsage": "用电量，单位kWh。后台为十进制字符串，App total_usage为数字。", "detail": "账单明细JSON对象，App返回对象，不返回JSON字符串。",
	"manageCode": "参保个人编号，与该会员参保查询和缴费列表保持一致。", "cityName": "参保城市。", "firstDate": "首次参保日期，YYYYMMDD；由已保存最早缴费月份的月初生成。", "lastMonth": "最后参保月份，YYYYMM。", "lastCompany": "最后缴费月份对应的单位名称。", "monthCount": "已保存缴费记录的去重月份数，字符串。", "personalAmount": "个人缴费累计，元，两位小数字符串。", "companyAmount": "单位缴费累计，元，两位小数字符串。", "interestAmount": "后台维护的模拟利息，元，两位小数字符串。", "month": "社保费款所属期，YYYYMM，必须与查询年度一致。", "company": "缴费单位名称；查询详情时使用列表中的原值。", "unitName": "社保缴费单位名称。", "companyPay": "单位缴纳金额，元，两位小数字符串。", "personalPay": "个人缴纳金额，元，两位小数字符串。", "base": "模拟社保缴费基数，元，两位小数字符串。", "identity": "参保身份，例如城镇职工。",
	"companyName": "任职单位名称。", "companyNum": "单位编号字符串。", "taxId": "单位纳税人识别号，模拟数据可含SIM标记。", "post": "任职职务。", "sex": "性别展示文本。", "birth": "出生日期，YYYY-MM-DD。", "cardType": "证件类型。", "idCard": "证件号码字符串；新生成使用12位随机纯数字，首位非0，不采用真实证件编码规则。", "taxNumber": "个人纳税人识别号字符串；新生成使用12位随机纯数字，首位非0，不校验真实税务编号。", "bankNum": "银行卡号字符串；新生成使用12位随机纯数字，首位非0，不采用真实银行卡规则。", "taxBackMoney": "2019至2023共5项非负金额，逗号分隔；其他年份由客户端使用第五项。", "hujiCity": "户籍省市区展示文本。", "hujiAddress": "户籍详细地址。", "usuallyCity": "经常居住省市区。", "usuallyAddress": "经常居住详细地址。", "contactCity": "联系地址省市区。", "contactAddress": "联系详细地址。", "education": "学历展示文本。", "nation": "民族展示文本。", "country": "国籍展示文本。",
	"symbol": "标的代码，包含市场后缀或品种标识。", "market": "市场标识，例如CN/HK/US/GLOBAL。", "category": "行情/持仓分类代码，允许值见参数枚举。", "categoryName": "分类中文名称。", "currency": "币种代码，如CNY、HKD、USD。", "exchangeRate": "演示汇率：原币金额乘该值折算人民币。", "exchangeRateMode": "汇率来源模式；demo_fixed为固定演示汇率。", "latestPrice": "最新价格，原币十进制字符串。", "changeAmount": "行情涨跌额，原币十进制字符串。", "changePercent": "行情涨跌幅百分数，1.25表示1.25%。", "quoteTime": "行情时间，RFC3339日期时间。", "dataStatus": "行情数据状态，live等值以实际返回为准。", "position": "单个标的持仓资料。", "positions": "持仓股票数组。", "positionCount": "当前实际持仓标的数量。", "holdingDays": "持仓天数。", "quantity": "持有或成交数量，整数，按标的交易单位解释。", "availableQuantity": "当前可用数量。", "costPrice": "持仓成本价，原币十进制字符串。", "price": "成交价格，原币十进制字符串。", "side": "成交方向：buy买入、sell卖出。", "tradedAt": "成交时间，东八区RFC3339日期时间。", "trades": "该标的的模拟交易记录数组。", "fees": "单笔成交费用，原币金额字符串。", "transactionFees": "持仓相关累计交易费用，人民币金额字符串。", "realizedProfit": "已实现盈亏，人民币金额字符串。", "marketValue": "持仓市值，人民币金额字符串。", "profit": "持仓浮动盈亏，人民币金额字符串。", "profitPercent": "持仓收益率百分数。", "dailyProfit": "当日盈亏，人民币金额字符串。", "dailyProfitPercent": "当日收益率百分数。", "totalAssets": "账户总资产，人民币元，十进制字符串。重建金额范围见接口参数。", "totalMarketValue": "全部持仓市值合计，人民币元。", "floatingProfit": "账户浮动盈亏合计，人民币元。", "availableCash": "账户可用现金，人民币元。", "withdrawableCash": "模拟可取现金，人民币元。", "positionRatio": "持仓市值占总资产比例，百分数。", "generatedAt": "本套模拟数据生成时间。", "notice": "与该记录相关的展示提示。",
	"longitude": "经度字符串，-180至180。", "latitude": "纬度字符串，-90至90。", "areaCode": "地区代码字符串，南网默认050200。", "classId": "资讯分类编号，初始模拟分类1、2、3。", "issuedName": "资讯发布单位。", "source": "资讯来源。", "publishTime": "资讯发布时间，YYYY-MM-DD HH:mm:ss。", "createTime": "资讯创建时间，YYYY-MM-DD HH:mm:ss。", "title": "资讯或消息标题。", "content": "资讯正文或消息内容，纯文本。", "imgUrl": "资讯封面HTTP/HTTPS地址，可为空。", "coverUrl": "备用封面HTTP/HTTPS地址，可为空。", "link": "外链HTTP/HTTPS地址，可为空。", "readCount": "资讯阅读数，非负整数。", "userName": "用电户名。", "custName": "兼容户名字段。", "eleCustId": "用电户号；计量点查询时只筛选本人记录。", "custNo": "兼容户号字段。", "eleAddr": "兼容用电地址字段，可为空。", "arrears": "模拟欠费金额，元，非负十进制字符串。", "totalArrears": "模拟欠费总额，元，非负十进制字符串。", "eleCustNumberId": "户号记录标识字符串。", "hallName": "模拟营业厅名称。", "distance": "后台维护的模拟距离，单位公里，不实时计算。", "pointName": "模拟计量点名称。", "hasPile": "是否有模拟充电桩，布尔值。",
	"zhaiyao": "收入纳税明细摘要。", "expect_date": "税款所属期，YYYY-MM。", "shenbao_date": "申报日期，YYYY-MM-DD。", "shenbao_end": "申报税额，元，等于该月保存的tax。", "tax_name": "所得项目名称，如工资薪金。", "channel": "申报渠道展示文本。", "xiaolei": "所得小类，如正常工资薪金。", "obligor_name": "扣缴义务人名称。", "tax_num": "扣缴义务人识别号。", "tax_authority": "主管税务机关。", "tax_company": "任职税务单位展示名称。", "income": "收入金额，元，两位小数字符串。", "tax": "已保存的模拟已缴税额，元，两位小数字符串。", "rate": "模拟税率展示文本，如10%；不是实际纳税计算结果。", "susuanCount": "模拟速算扣除数，元，字符串。", "tax_free_income": "免税收入，元，字符串。", "deduction_money": "减除费用，元，字符串。", "special_deduction": "养老、医疗、失业、公积金之和，元，字符串。", "nowShenTax": "本期申报税额，元，字符串。", "yanglao": "养老保险金额，元，字符串。", "yiliao": "医疗保险金额，元，字符串。", "shiye": "失业保险金额，元，字符串。", "gjj": "公积金金额，元，字符串。", "other_deduction": "其他扣除，元，字符串。", "donate_project": "准予扣除的捐赠，元，字符串。", "additional_deduction": "专项附加扣除，元，字符串。", "personal_pension": "个人养老金，元，字符串。", "tax_reduction": "模拟减免税额，元，字符串。", "totalIncome": "年度或截至指定月份的累计收入，元，两位小数字符串。", "totalTax": "年度已缴税额合计，元，两位小数字符串。", "total_income": "汇算收入合计，元，JSON数字，由年度月度记录汇总。", "total_tax": "汇算已缴税额合计，元，JSON数字。", "refund_amount": "汇算模拟退税金额，元，JSON数字。", "images_data": "HTTP/HTTPS图片地址，多个以逗号分隔。初始为空，暂不生成图片。", "yzm": "开具记录验证码，取二维码-=后的文本，仅匹配本人记录。", "create_time": "开具或消息时间，东八区YYYY-MM-DD HH:mm:ss字符串，不是时间戳。", "income_type": "所得项目名称。", "subtype": "所得小类。", "detail_list": "本期对应月度明细数组。", "bqsbse": "本期申报税额，与nowShenTax一致，元，字符串。",
	"totalMianShuiIncome": "累计免税收入，元，字符串。", "totalFuMin": "累计减除费用，元，字符串。", "totalZhuanMin": "累计专项扣除，元，字符串。", "totalFuJiaMin": "累计专项附加扣除，元，字符串。", "totalOtherMin": "累计其他扣除，元，字符串。", "totalYanglao": "累计个人养老金，元，字符串。", "totalZhunJuan": "累计准予扣除的捐赠，元，字符串。", "totalYingNaShuiSuoDe": "累计模拟应纳税所得额，收入扣除免税及各项扣除后的非负值，元，字符串。", "totalYingJiaoTax": "已保存模拟月税额累计，元，字符串。", "totalJiaoTax": "累计应扣缴税额展示值，使用已保存税额累计，元，字符串。", "totalYiJiaoTax": "截至上期的已缴税额累计，元，字符串。", "totalMin": "累计扣除合计，元，字符串。", "totalJianMian": "累计减免税额，元，字符串。",
}

func annotateDefinitions(defs map[string]any) {
	notes := map[string]string{}
	for k, v := range fieldNotes {
		notes[k] = v
	}
	aliases := map[string]string{"display_name": "displayName", "user_id_number": "userIdNumber", "meter_reader": "meterReader", "meter_no": "meterNo", "postal_code": "postalCode", "card_type": "cardType", "id_card": "idCard", "tax_number": "taxNumber", "bank_num": "bankNum", "bank_type": "bankType", "tax_back_money": "taxBackMoney", "huji_city": "hujiCity", "huji_address": "hujiAddress", "usually_city": "usuallyCity", "usually_address": "usuallyAddress", "contact_city": "contactCity", "contact_address": "contactAddress", "company_name": "companyName", "company_num": "companyNum", "tax_id": "taxId"}
	for a, k := range aliases {
		notes[a] = notes[k]
	}
	notes["bill_month"] = "账期，YYYYMM字符串。"
	notes["total_usage"] = "用电量，单位kWh，App返回JSON数字。"
	notes["start_date"] = "起始日期；任职/账单为YYYY-MM-DD，开具记录所属期为YYYY-MM。"
	notes["end_date"] = "结束日期；任职在职时可为空，开具记录为YYYY-MM。"
	for _, s := range nanwang.Schemas {
		for _, f := range s.Fields {
			if notes[f.Key] == "" {
				notes[f.Key] = f.Label
			}
		}
	}
	for _, s := range taxbusiness.Schemas {
		for _, f := range s.Fields {
			if notes[f.Key] == "" {
				notes[f.Key] = f.Label
			}
		}
	}
	for _, s := range alipay.Schemas {
		for _, f := range s.Fields {
			if notes[f.Key] == "" {
				notes[f.Key] = f.Label
			}
		}
	}
	for name, raw := range defs {
		def := object(raw)
		for key, v := range object(def["properties"]) {
			p := object(v)
			if p == nil {
				continue
			}
			if text := notes[key]; text != "" {
				p["description"] = text
			}
			if strings.Contains(name, "ContributionRule") || strings.Contains(name, "ContributionCalculation") {
				if key == "kind" {
					p["description"] = "险种：pension养老、medical医疗、unemployment失业、injury工伤、maternity生育。"
				}
				if key == "base" {
					p["description"] = "该险种核定缴费基数，元，最多两位小数；请求中空字符串使用月薪，须自行按适用年度上下限核定。"
				}
			}
			if strings.Contains(name, "GenerateContributions") && (key == "startMonth" || key == "endMonth") {
				p["description"] = "起止月份，YYYY-MM，包含两端；范围2000年起至本月，一次最多120个月。"
			}
			if key == "code" {
				p["description"] = "业务结果码：成功0，失败为项目标准错误码。HTTP状态码另行判断。"
				if strings.Contains(name, "internal_alipay.") || strings.Contains(name, "internal_taxbusiness.") || (strings.Contains(name, "internal_tax.") && strings.Contains(name, ".App")) {
					p["description"] = "业务结果码：成功1，业务失败0；登录失效另返回HTTP401。"
					p["example"] = 1
				}
				if strings.Contains(name, "ErrorEnvelope") {
					p["description"] = "项目标准业务错误码；HTTP状态码独立返回，message包含可读错误提示。"
				}
			}
			if key == "amount" {
				p["description"] = "金额，单位元。字符串时固定两位小数，不含货币符号。"
				if p["type"] == "number" {
					p["description"] = "电费账单金额，单位元，JSON数字，客户端可直接调用toFixed。"
				}
			}
			if key == "status" {
				p["description"] = "当前业务的状态展示值。"
				if strings.Contains(name, "Member") {
					p["description"] = "会员状态：1正常，2停用。"
					p["enum"] = []int{1, 2}
				}
			}
			if key == "type" {
				p["description"] = "业务类型：税务明细为整数类型编号，社保为核定类型文本，字段定义模型中为表单输入类型。"
			}
			if key == "name" && strings.Contains(name, "alipay.Profile") {
				p["description"] = "我的页面展示姓名，独立于会员登录资料。"
			}
			if key == "account" && strings.Contains(name, "auth.UserResponse") {
				p["description"] = "会员脱敏展示账号；不是用于再次登录的完整手机号或邮箱。"
			}
			if key == "sta" {
				p["example"] = "00"
			}
			if key == "account" && strings.Contains(name, "portfolio") {
				p["description"] = "当前分类的模拟账户汇总信息。"
			}
			if key == "date" && strings.Contains(name, "ApplyRequest") {
				p["description"] = "汇算年度，2019至当前年份，支持数字或字符串。"
			}
			if (key == "start_date" || key == "end_date") && strings.Contains(name, "OpenRecord") {
				p["description"] = "纳税记录开具所属期，YYYY-MM。"
			}
			if key == "side" {
				p["enum"] = []string{"buy", "sell"}
			}
		}
	}
}
