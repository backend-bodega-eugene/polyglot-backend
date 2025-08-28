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
		database_name := os.Getenv("DATABASE_NAME")
		database_port := os.Getenv("DATABASE_PORT")
		database_root := os.Getenv("DATABASE_ROOT")
		database_password := os.Getenv("DATABASE_PASSWORD")
		database_host := os.Getenv("DATABASE_HOST")
		database_chaset := os.Getenv("DATABASE_CHASET")
		dsn = database_root + ":" + database_password + "@tcp(" + database_host + ":" + database_port + ")/" + database_name + "?parseTime=true&charset=" + database_chaset

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

