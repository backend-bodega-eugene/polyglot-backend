package apidocs

import (
	"encoding/json"
	"fmt"
	"sort"
	"strings"

	"ths-service/internal/alipay"
	"ths-service/internal/nanwang"
	"ths-service/internal/taxbusiness"
)

type category struct{ name, description string }

var categories = []category{
	{"后台 · App安装包", "管理员选择APK上传、修改下载口令，发布编号由服务端自动生成且不可编辑，口令不回显。上传成功后切换当前安装包，文件通过持久化目录保留。"},
	{"App端 · 安装包下载", "公开下载页面与接口，无需会员登录；提交下载口令后直接返回APK附件，不公开文件地址。口令错误按IP限流。"},
	{"后台 · PDF生成", "选择会员已有数据生成本次编辑草稿，确认后直接返回attachment PDF，不回写业务数据，不保存文件或提供公开链接。使用当前已通过素材，保留演示标识及导出审计。"},
	{"后台 · PDF素材", "固定3至7号图片位置，上传为待审核，只有eugene账号且为超级管理员可审核。审核通过后替换生效图片，保留历史和审计。导出在独立PDF生成菜单执行，仅使用审核生效图片。"},
	{"App端 · 公共登录", "所有 App 共用会员登录。成功 code=0，取 data.accessToken，以 Authorization: Bearer <accessToken> 调用受保护接口。会员由后台创建，注册接口已停用。"},
	{"App端 · 证券行情", "按分类查询行情。响应 code=0/message/data；行情与模拟持仓分别查询。分类取值见 category 参数。"},
	{"App端 · 模拟持仓", "按会员及分类独立保存模拟账户、8—12个实际持仓和每个40—50条交易记录。未传资产时复用历史，缺失生成；传入资产会重建当前分类。code=0/message/data。"},
	{"App端 · 南网资料与账单", "共用会员 Token，只查询本人。资料缺失生成一条，账单缺失生成20条，已有直接读库。成功 code=0/msg/data；字段按南网文档为下划线，账单金额与用电量为数字。手机号查询使用 keyword，兼容 username。"},
	{"App端 · 南网业务", "文档中的 /mp 路径，均为 POST JSON。成功 sta=\"00\"/message/data。资讯与营业厅为公共数据，其他按会员隔离；完全缺失时生成保存，已有数据读库。无业务参数时提交 {}。"},
	{"App端 · 所得税个人资料", "与会员一对一，缺失生成保存，有记录读库。POST，无用户ID参数；返回文档下划线字段，成功 code=1/msg/data。原 /api/app/tax/profile 是 POST 兼容别名。"},
	{"App端 · 所得税任职单位", "每会员一个单位，返回单元素数组；POST，无用户ID参数。成功 code=1/msg/data，离职日期为空表示在职。"},
	{"App端 · 所得税业务", "年度明细、汇算、开具记录、扫码、消息及计算明细。成功 code=1/msg/data；业务失败 HTTP200/code=0/msg。缺失生成保存，已有读库。开具时间为 YYYY-MM-DD HH:mm:ss，图片生成暂未实现。年度合计及计算明细由已保存月度记录汇总。"},
	{"App端 · 支付宝模拟业务", "沿用公共登录，7个GET接口只查询本人。成功 code=1/msg/data，失败 code=0/msg，未登录 HTTP401/msg。金额为两位小数字符串；日期按 YYYYMMDD 或 YYYYMM。社保列表、详情与参保累计复用缴费记录。"},
	{"后台 · 管理员认证", "管理员独立于会员。先调用后台登录，浏览器自动保存 HttpOnly 会话 Cookie 并用于后续请求；会员 Bearer Token 不能替代后台会话。包含登录频率及失败冻结限制。"},
	{"后台 · 会员管理", "管理员会话鉴权；新增必填至少6字符密码，普通编辑不接受密码，修改密码使用独立接口。删除为逻辑删除，停用/删除后 App 无法继续访问。"},
	{"后台 · 持仓管理", "查询、生成及手动维护分类账户、持仓股票和交易记录。更新使用版本保护，金额规则和审计要求见具体接口。"},
	{"后台 · 南网用电资料", "独立用电资料，与会员一对一。支持查询、创建、编辑、删除、生成；后台只读接口不会自动生成。更新/删除/替换传 expectedVersion。"},
	{"后台 · 南网电费账单", "按会员一对多维护电费账单。金额和用电量用十进制字符串写入；行操作使用 version，整批生成使用 setVersion。"},
	{"后台 · 南网业务", "按 kind 管理资讯、户号、欠费、计量点、营业厅、消息和充电桩。news/halls 公共，其他按会员保存。payload字段表见写接口说明，schemas可供表单读取。"},
	{"后台 · 所得税个人资料", "独立个人所得税资料，与会员一对一；后台保持驼峰字段及标准 code=0/message/data。taxBackMoney固定2019—2023五项金额。"},
	{"后台 · 所得税任职单位", "每会员一个单位，支持生成及CRUD。离职日期可空；修改、删除及替换生成需 expectedVersion。"},
	{"后台 · 所得税业务", "kind为monthly/applications/openrecords/messages。通过月度记录编辑计算来源；合计与计算预览不另存冲突数据。开具图片地址可手工填写，不生成图片。"},
	{"后台 · 支付宝模拟业务", "kind为profile/insurance/records/accounts/electric/water。records按会员+年度保存，其他每会员一条；参保累计值由缴费记录汇总。"},
	{"后台 · 服务监控", "服务就绪检查，确认数据库可访问。此类接口供部署监控调用，不使用管理员会话。"},
}

func classify(path string) string {
	switch {
	case strings.HasPrefix(path, "/api/admin/app-package"):
		return "后台 · App安装包"
	case strings.HasPrefix(path, "/api/app-download/"):
		return "App端 · 安装包下载"
	case strings.HasPrefix(path, "/api/admin/pdf-reports"):
		return "后台 · PDF生成"
	case strings.HasPrefix(path, "/api/admin/pdf-assets"):
		return "后台 · PDF素材"
	case strings.HasPrefix(path, "/health/"):
		return "后台 · 服务监控"
	case strings.HasPrefix(path, "/api/admin/auth/"):
		return "后台 · 管理员认证"
	case strings.HasPrefix(path, "/api/admin/members") || strings.HasPrefix(path, "/api/admin/deleted-members"):
		return "后台 · 会员管理"
	case strings.HasPrefix(path, "/api/admin/holdings") || path == "/api/admin/holding-members":
		return "后台 · 持仓管理"
	case strings.HasPrefix(path, "/api/admin/electricity/profiles"):
		return "后台 · 南网用电资料"
	case strings.HasPrefix(path, "/api/admin/electricity/bills"):
		return "后台 · 南网电费账单"
	case strings.HasPrefix(path, "/api/admin/nanwang"):
		return "后台 · 南网业务"
	case strings.HasPrefix(path, "/api/admin/tax/profiles"):
		return "后台 · 所得税个人资料"
	case strings.HasPrefix(path, "/api/admin/tax/companies"):
		return "后台 · 所得税任职单位"
	case strings.HasPrefix(path, "/api/admin/tax-business"):
		return "后台 · 所得税业务"
	case strings.HasPrefix(path, "/api/admin/alipay"):
		return "后台 · 支付宝模拟业务"
	case path == "/api/app/auth/login":
		return "App端 · 公共登录"
	case path == "/api/app/market/quotes":
		return "App端 · 证券行情"
	case path == "/api/app/market/holdings" || strings.HasPrefix(path, "/api/app/portfolio/"):
		return "App端 · 模拟持仓"
	case strings.HasPrefix(path, "/api/app/electricity/") || path == "/api/app/users":
		return "App端 · 南网资料与账单"
	case strings.HasPrefix(path, "/mp/"):
		return "App端 · 南网业务"
	case path == "/api/app/tax/profile" || path == "/v1/tax-user/info":
		return "App端 · 所得税个人资料"
	case path == "/api/app/tax/companies" || path == "/v1/tax-user/company/list":
		return "App端 · 所得税任职单位"
	case strings.HasPrefix(path, "/v1/tax-user/"):
		return "App端 · 所得税业务"
	case strings.HasPrefix(path, "/api/insurance/") || strings.HasPrefix(path, "/api/utility/") || path == "/api/user/profile" || path == "/api/life/accounts":
		return "App端 · 支付宝模拟业务"
	}
	return ""
}

var descriptions = map[string]string{}

func init() {
	for _, c := range categories {
		descriptions[c.name] = c.description
	}
}
func object(v any) map[string]any { m, _ := v.(map[string]any); return m }
func Build(source []byte, group string) ([]byte, error) {
	if group != "app" && group != "admin" {
		return nil, fmt.Errorf("unknown documentation group")
	}
	var doc map[string]any
	if e := json.Unmarshal(source, &doc); e != nil {
		return nil, e
	}
	paths := object(doc["paths"])
	usedTags := map[string]bool{}
	for path, raw := range paths {
		tag := classify(path)
		isAdmin := strings.HasPrefix(tag, "后台")
		if tag == "" {
			return nil, fmt.Errorf("unclassified Swagger path: %s", path)
		}
		if isAdmin != (group == "admin") {
			delete(paths, path)
			continue
		}
		item := object(raw)
		for method, value := range item {
			if method == "parameters" || strings.HasPrefix(method, "x-") {
				continue
			}
			op := object(value)
			if op == nil {
				continue
			}
			op["tags"] = []string{tag}
			usedTags[tag] = true
			old, _ := op["description"].(string)
			note := descriptions[tag]
			if !strings.Contains(old, note) {
				op["description"] = note + "\n\n" + old
			}
			if strings.HasPrefix(path, "/api/admin/") && (method == "put" || method == "post") {
				op["description"] = op["description"].(string) + payloadGuide(path)
			}
			if path == "/api/app/auth/login" {
				op["summary"] = "公共会员登录（所有App共用）"
			}
			if path == "/api/app/tax/profile" || path == "/api/app/tax/companies" {
				op["deprecated"] = true
				op["description"] = "POST兼容别名，推荐调用 /v1/tax-user/info 或 /v1/tax-user/company/list。\n\n" + op["description"].(string)
			}
		}
	}
	tags := []any{}
	for _, c := range categories {
		if usedTags[c.name] {
			tags = append(tags, map[string]any{"name": c.name, "description": c.description})
		}
	}
	doc["tags"] = tags
	info := object(doc["info"])
	if info == nil {
		info = map[string]any{}
		doc["info"] = info
	}
	if group == "app" {
		info["title"] = "THS · App端接口"
		info["description"] = "仅展示 App 接口，所有App共用会员登录。先调用公共登录，再点击 Authorize 输入 `Bearer <accessToken>`。App只访问本人，不传会员ID。\n\n成功结构按业务保留：证券/公共登录 `code=0,message,data`；南网资料与账单 `code=0,msg,data`；南网业务 `sta=00,message,data`；所得税/支付宝 `code=1,msg,data`。接口字段、金额类型及日期格式以各分类和模型说明为准。各业务缺失时生成模拟数据保存，已有读取数据库；部分删除不自动补齐。"
	} else {
		info["title"] = "THS · 后台接口"
		info["description"] = "管理员使用独立登录及会话Cookie。请先执行管理员登录，随后浏览器自动携带Cookie。会员Token不适用。\n\n后台成功统一 `code=0,message,data,requestId`。查询不触发模拟数据生成。写操作按接口校验版本，冲突返回409；删除会员为逻辑删除，其他业务数据删除后App可能在完全缺失时重新生成。后台写入记录审计。服务监控无需管理员登录。"
	}
	annotateDefinitions(object(doc["definitions"]))
	pruneDefinitions(doc)
	if group == "admin" {
		delete(doc, "securityDefinitions")
		delete(doc, "security")
	}
	return json.MarshalIndent(doc, "", "  ")
}
func pruneDefinitions(doc map[string]any) {
	defs := object(doc["definitions"])
	needed := map[string]bool{}
	var walk func(any)
	walk = func(v any) {
		switch t := v.(type) {
		case map[string]any:
			if ref, ok := t["$ref"].(string); ok && strings.HasPrefix(ref, "#/definitions/") {
				name := strings.TrimPrefix(ref, "#/definitions/")
				if !needed[name] {
					needed[name] = true
					walk(defs[name])
				}
			}
			for _, v := range t {
				walk(v)
			}
		case []any:
			for _, v := range t {
				walk(v)
			}
		}
	}
	walk(doc["paths"])
	for name := range defs {
		if !needed[name] {
			delete(defs, name)
		}
	}
}
func payloadGuide(path string) string {
	var chunks []string
	add := func(kind, label, scope string, fields []string) {
		chunks = append(chunks, "### "+kind+" · "+label+"（"+scope+"）\n\n| payload字段 | 说明 | 类型 | 必填 |\n|---|---|---|---|\n"+strings.Join(fields, "\n"))
	}
	row := func(key, label, typ string, required bool) string {
		need := "否"
		if required {
			need = "是"
		}
		return "| `" + key + "` | " + label + " | " + typ + " | " + need + " |"
	}
	if strings.HasPrefix(path, "/api/admin/alipay") {
		for _, s := range alipay.Schemas {
			f := []string{}
			for _, v := range s.Fields {
				f = append(f, row(v.Key, v.Label, v.Type, v.Required))
			}
			scope := "每会员一条"
			if s.Yearly {
				scope = "按会员与年度一对多，同月同单位唯一"
			}
			add(s.Kind, s.Label, scope, f)
		}
	}
	if strings.HasPrefix(path, "/api/admin/tax-business") {
		for _, s := range taxbusiness.Schemas {
			f := []string{}
			for _, v := range s.Fields {
				f = append(f, row(v.Key, v.Label, v.Type, v.Required))
			}
			scope := "每会员多条"
			if s.Kind == "monthly" {
				scope = "按会员与年度一对多，同月同所得小类唯一"
			}
			if s.Kind == "applications" {
				scope = "每会员每年度一条"
			}
			add(s.Kind, s.Label, scope, f)
		}
	}
	if strings.HasPrefix(path, "/api/admin/nanwang") {
		for _, s := range nanwang.Schemas {
			f := []string{}
			for _, v := range s.Fields {
				f = append(f, row(v.Key, v.Label, v.Type, v.Required))
			}
			scope := "每会员多条"
			if s.Public {
				scope = "公共数据，多条记录，userId=0"
			}
			if s.Singleton {
				scope = "每会员一条"
			}
			add(s.Kind, s.Label, scope, f)
		}
	}
	if len(chunks) == 0 {
		return ""
	}
	sort.Strings(chunks)
	return "\n\n## 按kind填写payload\n\n金额money/decimal为非负十进制字符串，最多9位整数2位小数；integer为JSON整数，boolean为JSON布尔值。month/date/datetime格式见字段说明。新建不传expectedVersion，修改和删除传详情version。删除仅需expectedVersion，无需完整payload。\n\n" + strings.Join(chunks, "\n\n")
}
