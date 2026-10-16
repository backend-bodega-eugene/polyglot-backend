#!/usr/bin/env sh

set -eu

PROJECT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
COMPOSE_FILE="$PROJECT_DIR/compose.production.yaml"
ENV_FILE="$PROJECT_DIR/.env.production"
CERT_FILE="$PROJECT_DIR/deploy/cloudflare/origin.pem"
KEY_FILE="$PROJECT_DIR/deploy/cloudflare/origin-key.pem"
IMAGE_ARCHIVE="$PROJECT_DIR/ths-service-api.tar"

cd "$PROJECT_DIR"

for COMMAND_NAME in docker openssl curl; do
    if ! command -v "$COMMAND_NAME" >/dev/null 2>&1; then
        echo "缺少命令: $COMMAND_NAME" >&2
        exit 1
    fi
done

if [ ! -f "$CERT_FILE" ]; then
    echo "缺少 Cloudflare Origin Certificate: $CERT_FILE" >&2
    exit 1
fi

if [ ! -f "$KEY_FILE" ]; then
    echo "缺少 Cloudflare Origin Private Key: $KEY_FILE" >&2
    exit 1
fi

chmod 644 "$CERT_FILE"
chmod 600 "$KEY_FILE"

if [ ! -f "$ENV_FILE" ]; then
    umask 077
    MYSQL_ROOT_PASSWORD=$(openssl rand -hex 24)
    MYSQL_PASSWORD=$(openssl rand -hex 24)
    JWT_SECRET=$(openssl rand -hex 32)

    cat >"$ENV_FILE" <<EOF
MYSQL_ROOT_PASSWORD=$MYSQL_ROOT_PASSWORD
MYSQL_PASSWORD=$MYSQL_PASSWORD
JWT_SECRET=$JWT_SECRET
EOF

    echo "已生成生产密码文件: $ENV_FILE"
else
    echo "使用已有生产密码文件: $ENV_FILE"
fi

chmod 600 "$ENV_FILE"

docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" config --quiet
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" pull mysql migrate nginx

if [ -f "$IMAGE_ARCHIVE" ]; then
    docker load --input "$IMAGE_ARCHIVE"
elif ! docker image inspect ths-service-api:latest >/dev/null 2>&1; then
    echo "缺少 Go 服务镜像和镜像归档: $IMAGE_ARCHIVE" >&2
    exit 1
fi

docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d --no-build

echo "等待服务健康检查..."
ATTEMPT=1
while [ "$ATTEMPT" -le 30 ]; do
    if curl --silent --show-error --fail --insecure \
        --resolve eugenecoming.com:443:127.0.0.1 \
        https://eugenecoming.com/swagger/index.html >/dev/null; then
        echo "HTTPS 和 API 健康检查通过"
        docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" ps
        exit 0
    fi

    ATTEMPT=$((ATTEMPT + 1))
    sleep 2
done

echo "服务未在预期时间内就绪，输出容器状态和最近日志：" >&2
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" ps >&2
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" logs --tail=100 api nginx mysql migrate >&2
exit 1
