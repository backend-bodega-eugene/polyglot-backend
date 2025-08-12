APP?=eugene-go-starter
PORT?=8080

.PHONY: run tidy test build docker

run:
	go run ./cmd/server

tidy:
	go mod tidy

test:
	go test ./...

build:
	CGO_ENABLED=0 go build -o bin/$(APP) ./cmd/server

docker:
	docker build -t $(APP):latest .