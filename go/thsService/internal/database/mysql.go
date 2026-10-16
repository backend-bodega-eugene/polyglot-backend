package database

import (
	"database/sql"
	"fmt"
	"net"
	"strconv"
	"time"

	driver "github.com/go-sql-driver/mysql"
	"gorm.io/driver/mysql"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"

	"ths-service/internal/config"
)

type Connection struct {
	GORM *gorm.DB
	SQL  *sql.DB
}

func Open(cfg config.Database) (*Connection, error) {
	dsnConfig := driver.Config{
		User:                 cfg.User,
		Passwd:               cfg.Password,
		Net:                  "tcp",
		Addr:                 net.JoinHostPort(cfg.Host, strconv.Itoa(cfg.Port)),
		DBName:               cfg.Name,
		Collation:            "utf8mb4_0900_ai_ci",
		ParseTime:            true,
		Loc:                  time.Local,
		AllowNativePasswords: true,
	}
	dsn := dsnConfig.FormatDSN()

	db, err := gorm.Open(mysql.Open(dsn), &gorm.Config{
		Logger: logger.Default.LogMode(logger.Silent),
	})
	if err != nil {
		return nil, fmt.Errorf("open mysql: %w", err)
	}

	sqlDB, err := db.DB()
	if err != nil {
		return nil, fmt.Errorf("get mysql connection pool: %w", err)
	}

	sqlDB.SetMaxOpenConns(cfg.MaxOpenConnections)
	sqlDB.SetMaxIdleConns(cfg.MaxIdleConnections)
	sqlDB.SetConnMaxLifetime(time.Duration(cfg.ConnectionMaxLifetimeSeconds) * time.Second)

	if err := sqlDB.Ping(); err != nil {
		sqlDB.Close()
		return nil, fmt.Errorf("ping mysql: %w", err)
	}

	return &Connection{GORM: db, SQL: sqlDB}, nil
}
