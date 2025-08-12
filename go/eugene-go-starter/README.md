# eugene-go-starter

一个开箱即用的 Go Web API 脚手架。

## 快速启动

```bash
go mod tidy
make run
# 访问: http://127.0.0.1:8080/health
```

## 统一返回结构

```json
{"code":0,"msg":"success","data":{"status":"ok"}}
```

## 目录说明
- `cmd/server`: 程序入口
- `internal/config`: 配置
- `internal/router`: 路由与中间件
- `pkg/response`: 统一返回

## Docker 运行
```bash
docker build -t eugene-go-starter:latest .
docker run -p 8080:8080 eugene-go-starter:latest