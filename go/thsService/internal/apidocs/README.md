# Swagger分组和说明维护

入口保持`/swagger/index.html`，默认App端；顶部切换到`?group=admin`查看后台。业务分类默认折叠，支持搜索。底部全量Models隐藏，单个接口中仍可查看模型及字段说明。

- `/swagger/app.json`：App接口及其实际引用的模型；不包含后台专属模型。
- `/swagger/admin.json`：后台管理和服务监控接口，管理员会话说明，不展示会员Bearer授权框。
- `/swagger/doc.json`：兼容旧入口，返回App分组。

沿用既有THS_SWAGGER_ENABLED配置。关闭时页面、JSON和Swagger静态资源均不可访问；不新增生产环境例外。

接口原始注释和模型由swag正常生成到docs。分类统一为“App端 · 业务名”和“后台 · 业务名”；handler的@Tags使用同样名称。页面展示时由Build按路径分组，保留本组引用的definitions，验证分类无遗漏并补充说明。新增接口必须在classify中归类，未归类会在文档测试中失败。

分类说明在spec.go的categories集中维护；通用字段的单位、格式、版本及响应码说明在fields.go维护；后台通用payload写接口的字段表从nanwang/taxbusiness/alipay的Schemas读取，和实际后台校验及表单同源，避免手工文档漂移。特殊业务字段仍应写在对应handler和DTO注释中。

分类协议保留实际业务差异，不统一code：公共会员登录/证券为code=0/message；南网资料账单code=0/msg；南网业务sta=00/message；所得税和支付宝code=1/msg。后台为code=0/message，管理员登录保存HttpOnly Cookie。模型注释说明金额数字/字符串区别，日期YYYYMMDD、YYYYMM、YYYY-MM或RFC3339等格式。

重新生成：`go run github.com/swaggo/swag/cmd/swag@v1.16.6 init -g cmd/server/main.go -o docs --parseDependency --parseInternal`。

验证：`go test ./internal/apidocs ./internal/router`。覆盖两组路径不重叠且无遗漏、App不包含后台模型、引用完整、所有字段有说明、payload字段表、UI默认折叠配置、静态资源以及Swagger开关。
