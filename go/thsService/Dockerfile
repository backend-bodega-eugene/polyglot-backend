FROM golang:1.26.4-alpine AS builder

WORKDIR /src

COPY go.mod go.sum ./
RUN go mod download

COPY . .

RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 \
    go build -trimpath -ldflags="-s -w" -o /out/ths-service ./cmd/server

FROM alpine:3.23

RUN apk add --no-cache ca-certificates tzdata \
    && addgroup -S app \
    && adduser -S -G app app

WORKDIR /app

RUN mkdir -p /app/data/apks && chown -R app:app /app/data

COPY --from=builder /out/ths-service /app/ths-service
COPY configs/config.production.yaml /app/configs/config.production.yaml
COPY internal/pdfreport/assets/OFL.txt /app/licenses/ReportSans-OFL.txt

USER app

EXPOSE 9527

ENTRYPOINT ["/app/ths-service"]
CMD ["-config", "/app/configs/config.production.yaml"]
