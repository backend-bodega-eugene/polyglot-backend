# Build stage
FROM golang:1.22 AS builder
WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -o /eugene-go-starter ./cmd/server

# Runtime stage
FROM gcr.io/distroless/base-debian12
ENV APP_ENV=prod HTTP_ADDR=:8080 APP_NAME=eugene-go-starter
COPY --from=builder /eugene-go-starter /eugene-go-starter
EXPOSE 8080
ENTRYPOINT ["/eugene-go-starter"]