package db

import (
	"context"
	"os"
	"time"

	_ "github.com/go-sql-driver/mysql"
	"github.com/jmoiron/sqlx"
)

func NewSQLXFromEnv() (*sqlx.DB, func(context.Context) error, error) {
	dsn := os.Getenv("DB_DSN")
	if dsn == "" {
		// 给个开发默认值；线上靠环境变量覆盖
		dsn = "root:root@tcp(127.0.0.1:3306)/eugene?parseTime=true&charset=utf8mb4"
	}
	db, err := sqlx.Open("mysql", dsn)
	if err != nil {
		return nil, nil, err
	}
	db.SetMaxOpenConns(50)
	db.SetMaxIdleConns(10)
	db.SetConnMaxLifetime(2 * time.Hour)
	cleanup := func(ctx context.Context) error { return db.Close() }
	return db, cleanup, nil
}
