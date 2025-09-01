# ========= 构建阶段 =========
FROM golang:1.22 AS builder
WORKDIR /app

# 先拉依赖（利用缓存）
COPY go.mod go.sum ./
RUN go mod download

# 再拷源码
COPY . .

# 编译（main 在根目录）
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 \
    go build -ldflags="-s -w" -o /app/eugene-go-starter ./main.go

# ========= 运行阶段 =========
FROM gcr.io/distroless/base-debian12

# 固定运行时工作目录，保证相对路径如 ./web 生效
WORKDIR /app

# 默认环境（compose 可覆盖）
ENV APP_ENV=prod \
    HTTP_ADDR=:9527 \
    APP_NAME=eugene-go-starter

# 拷贝二进制与静态资源
COPY --from=builder /app/eugene-go-starter /app/eugene-go-starter
COPY web/ ./web/

EXPOSE 9527
ENTRYPOINT ["/app/eugene-go-starter"]
