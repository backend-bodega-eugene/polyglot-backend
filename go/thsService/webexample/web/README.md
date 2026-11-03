# GoalHub 管理后台

GoalHub 管理后台是一个基于静态 HTML、AdminLTE、Bootstrap 的后台管理前端，用于对 GoalHub 平台的用户、赛事、投注盘口、订单、资金、内容、权限和日志进行统一管理。前端通过 `js/config.js` 中配置的后端网关地址调用 `/admin/...` 管理接口，并使用登录后返回的 Token 进行鉴权。

## 主要功能

- 登录与鉴权：管理员登录、Token 保存、登录失效跳转、退出登录。
- 后台工作台：`index.html` 提供统一后台框架，动态加载后端菜单，并通过 iframe 打开各功能页面。
- 管理员与权限：管理员用户管理、菜单管理、角色/菜单授权、个人资料查看。
- App 用户管理：App 用户资料、账户余额、账户交易流水查询与管理。
- 赛事数据管理：联赛、队伍、比赛、比赛结果，以及联赛/队伍/比赛多语言内容维护。
- 投注市场管理：基础盘口、盘口选项、比赛盘口选项关联维护。
- 订单管理：投注订单查询、订单明细查看、订单审核、订单结算、订单冻结、历史订单查询。
- 资金订单审核：充值订单审核、提现订单审核，支持状态、链类型、交易哈希等条件筛选。
- 内容管理：内容列表、内容编辑、用户评论管理。
- 日志查询：系统日志、业务日志、错误日志分页筛选和查看。

## 页面模块

| 模块 | 页面 |
| --- | --- |
| 登录与首页 | `login.html`, `index.html`, `profile.html` |
| 管理员与权限 | `adminuser.html`, `menus.html`, `roles.html` |
| App 用户 | `appuser.html`, `appuseraccount.html`, `appuseraccounttransaction.html` |
| 赛事管理 | `leagues.html`, `teams.html`, `matchs.html`, `matchresult.html` |
| 多语言管理 | `leaguei18n.html`, `teami18n.html`, `matchi18n.html` |
| 投注市场 | `betmarket.html`, `betmarketoption.html`, `betmarketmatch.html` |
| 投注订单 | `ordermanagement.html`, `ordermanagementhistory.html` |
| 资金订单 | `deposit.html`, `withdraw.html` |
| 内容与评论 | `contents.html`, `contentedit.html`, `usercomments.html` |
| 日志 | `logs_sys.html`, `logs_biz.html`, `logs_err.html` |

## 运行方式

本项目是静态前端项目，提供了一个简单的 Node.js 静态文件服务器。

```bash
npm start
```

默认访问地址：

```text
http://localhost:5173/login.html
```

也可以通过环境变量调整端口和监听地址：

```bash
PORT=8080 HOST=0.0.0.0 npm start
```

Windows 下也可以直接运行：

```bat
start-web.cmd
```

## 后端接口配置

后端地址在 `js/config.js` 中配置：

```js
window.GoalHubConfig = {
  apiBase: 'http://localhost:8000',
  loginPath: '/admin/auth/login',
  loginPage: 'login.html',
  nextUrl: 'index.html',
};
```

- `apiBase`：后端管理接口基础地址。
- `loginPath`：管理员登录接口。
- `loginPage`：未登录或登录失效时跳转的页面。
- `nextUrl`：登录成功后的默认入口。

如果前端和后端部署在同一域名下，可以将 `apiBase` 改为空字符串。

## 目录结构

```text
web/
├── assets/              # 图片资源
├── css/                 # AdminLTE 样式
├── js/                  # 公共配置、鉴权、页面通用逻辑和业务脚本
├── *.html               # 各后台功能页面
├── server.js            # 本地静态文件服务器
├── package.json         # npm 启动脚本
└── start-web.cmd        # Windows 启动脚本
```

## 技术说明

- UI 基于 AdminLTE、Bootstrap 5、Bootstrap Icons。
- 菜单由 `/admin/auth/menus` 动态加载。
- 认证请求统一通过 `GoalHubAuth.authFetch` 添加 `Authorization: Bearer <token>`。
- 多数列表页支持分页、筛选、刷新、编辑、新增、删除等通用操作。
- 业务页面接口主要集中在 `/admin/...` 路径下，需要配套 GoalHub 后端服务运行。
