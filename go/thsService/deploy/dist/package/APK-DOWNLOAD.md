# App安装包与口令下载

后台入口：`/admin/app-package.html`，菜单“App安装包管理”。所有有效管理员账号均可操作，无会员登录要求。

1. 选择APK即可上传，不输入版本号。服务端按北京时间生成精确到秒的 `releaseKey`（YYYYMMDDHHmmss），只能读取、不能手动指定或修改；同一秒重复发布返回409，请稍后重试。
2. 默认下载口令为 `eugenecoming`。可修改为5—64个Unicode字符、最多256个UTF-8字节，支持中文、字母、数字和符号；两次输入须一致。不自动去掉首尾空格，不允许全空格或控制字符。口令用SHA-256预哈希后再bcrypt保存，避免中文口令受到bcrypt的72字节限制，不提供找回/回显。
3. 公开页面固定为 `/app-download`；无需会员登录，输入口令后通过POST直接下载APK附件。
4. 修改口令立即影响新的下载请求；已经开始传输的下载不中断。

## 接口

| 方法及路径 | 权限 / 内容 |
| --- | --- |
| GET `/api/admin/app-package` | 管理员Cookie，读取当前版本及口令设置状态 |
| POST `/api/admin/app-package/upload` | 管理员Cookie，multipart字段file；服务端生成releaseKey，拒绝手工指定编号或在上传接口设置口令 |
| PUT `/api/admin/app-package/passcode` | 管理员Cookie，JSON `{"passcode":"中文下载口令"}` |
| GET `/api/app-download/info` | 公开，只返回版本信息，无路径和口令 |
| POST `/api/app-download/download` | 公开，JSON或urlencoded表单passcode；成功返回application/vnd.android.package-archive附件 |

成功的JSON响应沿用code=0/message/data。下载错误返回JSON，401口令错误、404未上传/文件不存在、429冻结并带Retry-After。口令不得放在查询参数中，不产生可绕过口令的下载直链。

## 存储及部署

- 迁移 `000024_apk_download` 保存元数据和审计；`000025_apk_defaults` 仅为尚未设置口令的记录初始化默认哈希，并将历史手填版本转换为上传时间编号，不覆盖已有自定义口令。内部沿用version_name数据库列，对外只返回releaseKey。审计不记录口令或哈希。
- APK本体放在 `THS_APK_STORAGE_DIR`，本地默认 `data/apks`，不在静态文件目录，已从Git与Docker构建上下文排除。
- 正式Compose挂载命名卷 `apk_data` 至 `/app/data/apks`，镜像预设app用户的目录权限，兼容容器只读根文件系统。正常部署/重建容器不会删除安装包。
- 数据库和APK卷需一并备份。不要运行 `docker compose down -v`，该命令会删除命名卷。
- 文件小于100MB。已为Nginx的上传接口单独设置100MiB请求体上限和300秒超时，其他接口仍保留原10MiB限制。CDN/外层代理的大小限制仍可能更低，需按实际代理配置调整。
- APK基本结构检查参考[Android官方APK Analyzer说明](https://developer.android.com/studio/debug/apk-analyzer)：APK使用ZIP容器，包含AndroidManifest.xml，清单通常为二进制格式。
- 上传先写临时文件、计算SHA256、检查ZIP和Android二进制清单，之后原子改名，并在数据库事务中切换当前版本。失败不会覆盖当前元数据；完整上传才会生效。该检查不替代Android安装时的签名验证，不是恶意软件检测。
- 历史APK文件保留在私有目录，避免影响已开始的下载。当前没有历史版本下载/回滚或自动清理入口；随版本数增长应规划磁盘空间。
- 口令错误按真实客户端IP计数，滚动一分钟5次错误冻结10分钟；正确口令清空该IP失败计数。计数使用独立命名空间，复用已有数据库限流表，重启后保留，不影响后台登录计数。代理需沿用现有可信代理配置。

## 页面行为

公开页面不通过Blob缓冲整个安装包，使用POST表单提交口令、浏览器直接下载附件；错误结果在隐藏的同源iframe中读取并显示。口令不进入URL或localStorage。APK上传显示进度；发布前提示替换当前版本。

下载页的HTTP头与HTML meta均使用 `Referrer-Policy: same-origin`，保留同源POST表单的Origin。不要设为no-referrer，否则浏览器可能提交Origin:null，被后端来源校验拒绝。后端继续拒绝null来源及不同主机的来源，不放宽口令校验。
