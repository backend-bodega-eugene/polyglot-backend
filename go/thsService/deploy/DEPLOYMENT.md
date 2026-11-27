# 测试与正式环境配置

同一部署包用于两个环境。在服务器部署目录（例如 /opt/ths-service）修改已有的 `.env.production`，保留原来的密码和JWT密钥行，只新增或修改以下配置。

测试环境：

```ini
THS_APP_ENVIRONMENT=testing
THS_SWAGGER_ENABLED=true
```

正式环境：

```ini
THS_APP_ENVIRONMENT=production
THS_SWAGGER_ENABLED=false
```

不要把示例文件覆盖到已有的 `.env.production`。首次部署脚本才会生成该文件；后续发布不会覆盖。旧文件未配置这两项时，Compose默认保持测试环境且开启Swagger，兼容当前测试服务器。

解压部署包并执行 `./deploy/production-deploy.sh` 即可。只修改环境开关时，在部署目录执行：

```sh
docker compose --env-file .env.production -f compose.production.yaml up -d --no-deps --force-recreate api
```

修改环境变量需要重建容器生效，单纯 `docker restart` 不会重新加载该文件。首次升级到本包先运行完整部署脚本，之后才使用上面的仅更新配置命令。

关闭Swagger会让 `/swagger/index.html`、`/swagger/doc.json` 等全部Swagger地址返回404。健康检查独立使用 `/health/ready`，正常为200、数据库不可用为503，与Swagger开关无关。后台、App接口和密码均不因切换该开关而改变。

## 后台数据库备份

左侧菜单最后的“数据备份”（`/admin/data-backup.html`）仅允许eugene超级管理员导出。点击“导出全部数据”直接下载`thsbackup年月日时分秒.sql`，包含当前连接数据库的全部表结构、数据、逻辑删除记录、图片二进制素材和数据库迁移版本，以及数据库中的触发器、事件和存储程序。后续新增表自动包含，不依赖固定业务表列表。不导出MySQL系统账号，不包含APK文件和.env配置。

导出使用InnoDB事务快照；备份期间不要运行迁移或其他表结构变更。客户端读取数据库账号只使用权限为0600的临时配置文件，不把密码放命令行。SQL先在私有临时目录生成，成功后下载，请求结束清理；生成失败不返回半截SQL。同一时间只允许一个备份，生成超时10分钟。SQL包含敏感业务数据和密码哈希，需由管理员自行妥善保存。此功能无恢复入口；恢复应在目标服务器通过MySQL客户端导入，SQL带有原数据库名，目标实例应使用相同数据库名和匹配程序版本。

镜像自带MariaDB导出客户端和MySQL8认证插件，使用当前服务相同的数据库连接。数据库采用当前项目的非TLS内网连接配置；自定义TLS连接会明确拒绝。本地直接运行Go时，需安装mariadb-dump或MySQL8的mysqldump并加入PATH。重新部署本功能时使用完整部署脚本，同时更新镜像和Nginx备份接口超时配置。外层代理若有更短的请求超时，大库导出需另行调整代理限制。
