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
