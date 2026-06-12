# GoalHub App Web

GoalHub App Web 是面向用户端的移动优先 Web 应用，使用原生 HTML、CSS、JavaScript 实现，不依赖前端框架。项目主要承载注册登录、赛事投注、订单记录、钱包资金、客服反馈、设置和 CMS 内容展示等功能。

## 运行方式

```bash
cd d:\Eugene\polyglot-backend\java\goalhub\appweb
node server.js
```

默认监听：

- 本机：`http://localhost:80`
- 局域网：`http://192.168.1.104:80`

可通过环境变量覆盖：

```bash
$env:PORT=8080
$env:HOST="0.0.0.0"
node server.js
```

## 后端地址

接口基础地址在 [js/config.js](d:/Eugene/polyglot-backend/java/goalhub/appweb/js/config.js) 配置：

```javascript
window.GoalHubConfig = {
    API_BASE_URL: 'http://192.168.1.104:8000'
};
```

如果没有加载该配置，脚本会回退到 `http://localhost:8000`。

## 项目结构

```text
appweb/
├── *.html          # 各业务页面
├── css/
│   ├── main.css    # 注册、登录等基础页面样式
│   ├── index.css   # 首页赛事和底部布局样式
│   └── app.css     # App 内页通用样式
├── js/
│   ├── config.js   # API_BASE_URL 配置
│   ├── app.js      # 登录态、鉴权请求、底部导航、余额公共逻辑
│   └── *.js        # 各页面业务脚本
└── server.js       # 静态文件服务器
```

## 页面功能

| 页面 | 功能 |
| --- | --- |
| `register.html` | 用户注册、验证码、表单校验 |
| `login.html` | 用户登录、验证码、登录态写入 |
| `forgotpassword.html` | 忘记密码、发送验证码、重置密码 |
| `index.html` | 赛事列表、筛选、关注、赔率弹层、下注 |
| `tutorial.html` | CMS 盘口教程展示 |
| `service.html` | 客服入口 |
| `usercomments.html` | 用户留言提交和历史回复 |
| `bettings.html` | 未结注单、已结注单查询 |
| `myprofile.html` | 我的页面、钱包入口、个人中心入口 |
| `setting.html` | 设置入口：资料、密码、资金密码、关于、退出 |
| `about.html` | CMS 关于内容展示 |
| `editprofile.html` | 修改昵称等个人资料 |
| `changepassword.html` | 修改登录密码 |
| `fundpassword.html` | 设置或修改资金密码 |
| `deposit.html` / `withdraw.html` | 存款、取款申请 |
| `deposithistory.html` / `withdrawhistory.html` | 存取款记录 |
| `transactions.html` | 账户流水查询 |

## 公共机制

[js/app.js](d:/Eugene/polyglot-backend/java/goalhub/appweb/js/app.js) 是所有 App 内页的公共入口，负责：

- 维护登录态：`authToken`、`userId`、`currentUsername`、`currentNickname`
- 非公开页面自动校验登录，未登录跳转 `login.html`
- 提供 `GoalHubApp.apiFetch()`，自动拼接 API 地址、附带 Bearer Token、处理 401
- 渲染底部导航并根据当前页面设置 active 状态
- 缓存和刷新默认钱包余额
- 绑定退出登录按钮

公开页面包括：

- `login.html`
- `register.html`
- `forgotpassword.html`

其他页面默认需要登录。

## 主要接口

### 用户

- `POST /api/user/register`
- `POST /api/user/login`
- `GET /api/user/captcha`
- `GET /api/user/profile/me`
- `PUT /api/user/profile/me`
- `POST /api/user/change-password`
- `POST /api/user/forgotpassword/sendcode`
- `POST /api/user/forgotpassword/reset`
- `POST /api/user/security/fundpassword/set`
- `POST /api/user/security/fundpassword/change`

### 钱包和资金

- `GET /api/user/account/me/defaultbalance`
- `GET /api/user/account/me/transactions`
- `POST /api/order/depositorder/create`
- `POST /api/order/depositorder/page`
- `POST /api/order/withdraworder/create`
- `POST /api/order/withdraworder/page`

### 赛事和投注

- `GET /api/soccer/leagues`
- `GET /api/soccer/matches`
- `GET /api/soccer/matches/today`
- `GET /api/soccer/matches/upcoming`
- `GET /api/soccer/matches/hot`
- `POST /api/soccer/matches/results/page`
- `GET /api/soccer/matches/{matchId}/odds`
- `GET /api/soccer/follow/my`
- `POST /api/soccer/follow/{matchId}`
- `DELETE /api/soccer/follow/{matchId}`
- `POST /api/order/bet/orders/place`
- `POST /api/order/bet/orders/page`

### 内容和客服

- `GET /api/soccer/contents/articles/handicaptutorial`
- `GET /api/soccer/contents/articles/about`
- `POST /api/user/usercomments/add`
- `POST /api/user/usercomments/page`

## CMS 内容说明

`tutorial.html` 和 `about.html` 都从内容管理系统读取 HTML 正文：

- 盘口教程：`/api/soccer/contents/articles/handicaptutorial`
- 关于：`/api/soccer/contents/articles/about`

App 前端不向“关于”接口传参数。后台发布内容时，需要由后台管理前端保存正确的内容类型；App 只负责展示接口返回的 `data.contentHtml`。

## 开发约定

- 新增 App 内页时，引入顺序保持为 `js/config.js`、`js/app.js`、页面脚本。
- 需要登录的接口优先使用 `GoalHubApp.apiFetch()`。
- 页面内金额显示优先复用公共余额缓存和 `refreshBalance()`。
- 底部导航由 `app.js` 统一渲染，页面 HTML 中的 `.index-footer` 会被替换。
- 当前项目边界是 `appweb`；后台管理前端在同级 `web` 项目，后端在 `goalhub` 项目。

## 常见问题

### 页面跳到登录页

非公开页面需要 `localStorage.authToken`。登录过期或接口返回 401 时，公共脚本会清理登录态并跳转登录页。

### 接口请求到错误环境

检查 [js/config.js](d:/Eugene/polyglot-backend/java/goalhub/appweb/js/config.js) 的 `API_BASE_URL` 是否指向当前后端。

### 端口 80 启动失败

Windows 下端口 80 可能需要管理员权限。可以用 `PORT=8080` 启动，或关闭占用端口的程序。

### 关于或盘口教程显示为空

先确认后台 CMS 已发布对应内容，并且接口返回的 `data.contentHtml` 不为空。App 前端只展示接口返回内容。
