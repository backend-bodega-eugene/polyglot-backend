# ========= 构建阶段 =========
FROM golang:1.22 AS builder

# 设置工作目录
WORKDIR /app

# 复制 go.mod 和 go.sum 先下载依赖
COPY go.mod go.sum ./
RUN go mod download

# 复制整个项目源码
COPY . .

# 编译可执行文件（入口就是根目录的 main.go）
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 \
    go build -ldflags="-s -w" -o /eugene-go-starter ./main.go

# ========= 运行阶段 =========
FROM gcr.io/distroless/base-debian12

# 设置一些默认环境变量（可被 compose 覆盖）
ENV APP_ENV=prod \
    HTTP_ADDR=:9527 \
    APP_NAME=eugene-go-starter

# 拷贝编译好的二进制
COPY --from=builder /eugene-go-starter /eugene-go-starter

# 暴露端口
EXPOSE 9527

# 启动命令
ENTRYPOINT ["/eugene-go-starter"]
