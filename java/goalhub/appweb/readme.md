# GoalHub 注册界面项目

## 项目结构

```
appweb/
├── server.js           # Node.js 服务器（端口 80）
├── register.html       # 注册页面
├── login.html          # 登录页面（占位符）
├── css/
│   └── main.css        # 主样式表
├── js/
│   └── register.js     # 注册页面脚本
└── README.md           # 项目说明
```

## 最近更新

### 目录结构优化
- ✅ CSS 文件移至 `css/` 文件夹
- ✅ JS 文件移至 `js/` 文件夹
- ✅ 品牌更换为 GoalHub
- ✅ 合作伙伴区域已移除
- ✅ 文件名标准化（register.html）
- ✅ 页面重新排版

## 功能特性

### ✅ 已实现的功能

1. **界面设计**
   - GoalHub 品牌标识
   - 清洁简约的注册表单
   - 完全响应式设计（手机+平板+桌面）

2. **表单字段**
   - 用户名输入框
   - 密码输入框
   - 确认密码输入框
   - 昵称输入框
   - 服务条款同意复选框

3. **表单验证**
   - 用户名：3-20字符，只能包含字母、数字、下划线
   - 密码：6-50字符
   - 密码确认：必须与密码一致
   - 昵称：2-30字符
   - 必须同意服务条款

4. **用户交互**
   - 实时错误提示（失焦时验证）
   - 表单提交时完整验证
   - 加载动画
   - 消息提示（成功/失败）

5. **底部导航**
   - 前往登录
   - 游客进入
   - 客服帮助

## API 集成

**端点**：`POST http://localhost:8000/api/user/register`

**请求体**：
```json
{
  "username": "zhangsan",
  "password": "123456",
  "nickname": "张三"
}
```

## 使用方法

### 启动服务器
```bash
cd d:\Eugene\polyglot-backend\java\goalhub\appweb
node server.js
```

服务器将在 `http://localhost` （端口 80）启动

### 访问页面
- 注册页面：`http://localhost/register.html` 或 `http://localhost`
- 登录页面：`http://localhost/login.html`

## 文件说明

### server.js
- Node.js HTTP 服务器
- 静态文件服务
- CORS 跨域支持
- 端口 80

### register.html
- 注册界面主文件
- 包含表单和导航按钮
- 引入 CSS 和 JS 资源

### css/main.css
- 响应式样式表
- 蓝色主题色
- 支持移动端和桌面端
- 动画效果

### js/register.js
- 表单验证逻辑
- API 请求处理
- 事件监听
- 错误提示和消息显示

### login.html
- 登录页面占位符
- 待实现功能

## 技术栈

- HTML5
- CSS3（响应式、渐变、动画）
- 纯 JavaScript ES6
- Node.js HTTP 服务器
- Fetch API

## 浏览器兼容性

- Chrome 60+
- Firefox 55+
- Safari 10.1+
- Edge 79+
- 移动浏览器

## 响应式断点

- 桌面：600px+
- 平板：600px - 400px
- 手机：< 400px

## 自定义配置

### 修改主题色
编辑 `css/main.css`，查找并替换：
- `#1e3c72` 和 `#2a5298`

### 修改 API 地址
编辑 `js/register.js`，修改：
```javascript
const API_URL = 'http://localhost:8000/api/user/register';
```

### 修改服务器端口
编辑 `server.js`，修改：
```javascript
const PORT = 80;
```

### 修改品牌信息
编辑 `register.html`，修改：
```html
<div class="logo">GoalHub</div>
<div class="subtitle">让每个目标都闪闪发光</div>
```

## 问题排查

### 端口 80 权限不足
- 使用管理员身份运行
- 或修改为其他端口（如 3000、8080）

### 注册请求失败
- 确保后端服务运行在 `http://localhost:8000`
- 检查浏览器开发者工具（F12）的网络选项卡
- 检查 CORS 配置

### 样式未应用
- 清除浏览器缓存
- 确保 CSS 文件路径正确

## 许可

本项目仅供学习和参考使用。

2. **表单字段**
   - 用户名输入框
   - 密码输入框
   - 确认密码输入框
   - 昵称输入框
   - 服务条款同意复选框

3. **表单验证**
   - 用户名：3-20字符，只能包含字母、数字、下划线
   - 密码：6-50字符
   - 密码确认：必须与密码一致
   - 昵称：2-30字符
   - 必须同意服务条款

4. **用户交互**
   - 实时错误提示（失焦时验证）
   - 表单提交时完整验证
   - 加载动画
   - 消息提示（成功/失败）

5. **底部导航**
   - 前往登录
   - 游客进入
   - 在线客服

6. **响应式设计**
   - 完全适配手机屏幕
   - 在600px、400px处有断点

### 🔗 API 集成
- **端点**：`POST http://localhost:8000/api/user/register`
- **请求体**：
  ```json
  {
    "username": "zhangsan",
    "password": "123456",
    "nickname": "张三"
  }
  ```

## 使用方法

### 1. 本地测试
1. 将 `index.html` 文件在浏览器中打开
2. 或使用本地服务器（如 Python 的 `http.server`）：
   ```bash
   # 在 appweb 目录下运行
   python -m http.server 3000
   # 然后访问 http://localhost:3000
   ```

### 2. 确保后端服务运行
- 确保后端服务运行在 `http://localhost:8000`
- 确保 `/api/user/register` 接口可用

### 3. 配置跨域（如果需要）
如果后端和前端不在同一端口，需要配置 CORS：
- 确保后端返回适当的 CORS 头
- 或在前端代码中添加代理

## 文件说明

### index.html
- HTML 结构
- 包含表单、导航按钮等元素
- 引入 styles.css 和 script.js

### styles.css
- 响应式设计
- 蓝色主题色：#1e3c72 和 #2a5298
- 支持移动端和桌面端
- 包含加载动画和消息提示样式

### script.js
- 表单验证逻辑
- API 请求处理
- 事件监听
- 错误提示和消息显示
- 不依赖任何框架（纯 JavaScript）

## 技术栈

- **HTML5**：语义化标签
- **CSS3**：渐变、动画、网格布局
- **纯 JavaScript ES6**：无框架依赖
- **Fetch API**：用于 HTTP 请求

## 浏览器兼容性

- Chrome 60+
- Firefox 55+
- Safari 10.1+
- Edge 79+

## 自定义选项

### 修改主题色
在 `styles.css` 中搜索 `#1e3c72` 或 `#2a5298`，替换为你想要的颜色。

### 修改 API 地址
在 `script.js` 中修改：
```javascript
const API_URL = 'http://localhost:8000/api/user/register';
```

### 修改验证规则
在 `script.js` 中编辑各个 `validate*` 函数。

### 修改合作伙伴卡片
在 `index.html` 中编辑 `.partners-section` 内的内容。

## 注意事项

1. **跨域问题**：如果前端和后端不在同一端口，需要配置 CORS
2. **本地资源**：所有资源都是本地的，没有外部依赖
3. **安全性**：密码在传输前进行了客户端验证，但生产环境应使用 HTTPS
4. **错误处理**：所有错误都会显示用户友好的提示信息

## 如何改进

如果你需要添加以下功能，请告诉我：
1. 真实的 Logo 图片替换 K8
2. 真实的合作伙伴 Logo 图片
3. 实际的条款和隐私政策链接
4. 邮箱验证功能
5. 验证码功能
6. 第三方登录集成
7. 数据库持久化

## 问题排查

### 注册请求失败
- 检查后端服务是否运行在 `http://localhost:8000`
- 检查浏览器控制台的网络请求
- 确保请求数据格式正确

### 样式未应用
- 确保 styles.css 文件在正确的位置
- 检查浏览器缓存（使用 Ctrl+Shift+Delete 清除）

### JavaScript 错误
- 打开浏览器开发者工具（F12）查看控制台
- 检查 script.js 文件是否加载成功

## 许可

本项目仅供学习和参考使用。
