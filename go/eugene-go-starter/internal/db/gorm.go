package db

import (
	"context"
	"os"
	"time"

	"gorm.io/driver/mysql"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
	"gorm.io/gorm/schema"
)

func NewGORMFromEnv() (*gorm.DB, func(context.Context) error, error) {
	dsn := os.Getenv("DB_DSN")
	if dsn == "" {
		// 给个开发默认值；线上靠环境变量覆盖
		database_name := os.Getenv("DATABASE_NAME")
		database_port := os.Getenv("DATABASE_PORT")
		database_root := os.Getenv("DATABASE_ROOT")
		database_password := os.Getenv("DATABASE_PASSWORD")
		database_host := os.Getenv("DATABASE_HOST")
		database_chaset := os.Getenv("DATABASE_CHASET")
		if database_chaset == "" {
			database_chaset = "utf8mb4"
		}
		dsn = database_root + ":" + database_password + "@tcp(" +
			database_host + ":" + database_port + ")/" + database_name +
			"?parseTime=true&charset=" + database_chaset + "&loc=Local"
	}

	gdb, err := gorm.Open(mysql.Open(dsn), &gorm.Config{
		// 可选：设置 logger、命名策略
		Logger: logger.Default.LogMode(logger.Warn),
		NamingStrategy: schema.NamingStrategy{
			// 保持表名与 struct 一致，不强制复数
			SingularTable: true,
		},
	})
	if err != nil {
		return nil, nil, err
	}

	// 设置底层连接池参数
	sqlDB, err := gdb.DB()
	if err != nil {
		return nil, nil, err
	}
	sqlDB.SetMaxOpenConns(50)
	sqlDB.SetMaxIdleConns(10)
	sqlDB.SetConnMaxLifetime(2 * time.Hour)

	cleanup := func(ctx context.Context) error {
		return sqlDB.Close()
	}
	return gdb, cleanup, nil
}
