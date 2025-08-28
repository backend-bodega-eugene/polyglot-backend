// internal/db/mongo.go
package db

import (
	"context"
	"crypto/tls"
	"os"
	"time"

	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

type MongoHolder struct {
	Client *mongo.Client
	DB     *mongo.Database
}

// NewMongoFromEnv 初始化 Mongo 连接（与 NewGORMFromEnv 风格一致：返回“实例+清理函数+错误”）
func NewMongoFromEnv() (*MongoHolder, func(context.Context) error, error) {
	// 1) 读取环境变量
	// 优先使用标准 URI，其次根据分散变量拼 URI
	uri := os.Getenv("MONGO_URI")
	dbName := os.Getenv("MONGO_DB") // 目标数据库名（必填或给默认）

	if uri == "" {
		user := os.Getenv("MONGO_USER")
		pass := os.Getenv("MONGO_PASS")
		host := os.Getenv("MONGO_HOST")
		if host == "" {
			host = "127.0.0.1"
		}
		port := os.Getenv("MONGO_PORT")
		if port == "" {
			port = "27017"
		}
		authDB := os.Getenv("MONGO_AUTH_DB")
		if authDB == "" {
			authDB = "admin"
		}

		// mongodb://user:pass@host:port/?authSource=admin
		if user != "" {
			uri = "mongodb://" + user + ":" + pass + "@" + host + ":" + port + "/?authSource=" + authDB
		} else {
			uri = "mongodb://" + host + ":" + port
		}
	}
	if dbName == "" {
		dbName = "eugene"
	}

	// 2) 基础 Client 选项（池参数、超时）
	//   - 与 GORM 版保持“合理默认 + 外部覆盖”的思路
	clientOpts := options.Client().
		ApplyURI(uri).
		SetMaxPoolSize(50).
		SetMinPoolSize(5).
		SetConnectTimeout(10 * time.Second).
		SetServerSelectionTimeout(10 * time.Second)

	// 可选：TLS 开关（MONGO_TLS=true）
	if os.Getenv("MONGO_TLS") == "true" {
		clientOpts.SetTLSConfig(&tls.Config{}) // 使用系统 CA；如需自定义证书可自行扩展
	}

	// 3) 建立连接 + Ping
	ctx, cancel := context.WithTimeout(context.Background(), 15*time.Second)
	defer cancel()

	client, err := mongo.Connect(ctx, clientOpts)
	if err != nil {
		return nil, nil, err
	}
	if err = client.Ping(ctx, nil); err != nil {
		_ = client.Disconnect(context.Background())
		return nil, nil, err
	}

	holder := &MongoHolder{
		Client: client,
		DB:     client.Database(dbName),
	}

	// 4) 返回与 GORM 版一致的 cleanup 形式
	cleanup := func(ctx context.Context) error {
		return holder.Client.Disconnect(ctx)
	}
	return holder, cleanup, nil
}
