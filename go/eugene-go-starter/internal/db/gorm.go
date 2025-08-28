package db

import (
	"context"
	"os"
	"time"

	"gorm.io/driver/mysql"
	"gorm.io/gorm"
)

func NewGormFromEnv() (*gorm.DB, func(context.Context) error, error) {
	dsn := os.Getenv("DB_DSN")
	if dsn == "" {
		databaseName := os.Getenv("DATABASE_NAME")
		databasePort := os.Getenv("DATABASE_PORT")
		databaseUser := os.Getenv("DATABASE_ROOT")
		databasePassword := os.Getenv("DATABASE_PASSWORD")
		databaseHost := os.Getenv("DATABASE_HOST")
		databaseCharset := os.Getenv("DATABASE_CHASET")
		dsn = databaseUser + ":" + databasePassword + "@tcp(" + databaseHost + ":" + databasePort + ")/" + databaseName + "?parseTime=true&charset=" + databaseCharset
	}

	gdb, err := gorm.Open(mysql.Open(dsn), &gorm.Config{})
	if err != nil {
		return nil, nil, err
	}
	sqlDB, err := gdb.DB()
	if err != nil {
		return nil, nil, err
	}
	sqlDB.SetMaxOpenConns(50)
	sqlDB.SetMaxIdleConns(10)
	sqlDB.SetConnMaxLifetime(2 * time.Hour)

	cleanup := func(ctx context.Context) error { return sqlDB.Close() }
	return gdb, cleanup, nil
}
