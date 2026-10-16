package config

import (
	"fmt"
	"os"

	"gopkg.in/yaml.v3"
)

type Config struct {
	App      App      `yaml:"app"`
	Server   Server   `yaml:"server"`
	Database Database `yaml:"database"`
	Swagger  Swagger  `yaml:"swagger"`
	Log      Log      `yaml:"log"`
	JWT      JWT      `yaml:"jwt"`
	Market   Market   `yaml:"market"`
}

type App struct {
	Environment string `yaml:"environment"`
}

type Server struct {
	Address                  string   `yaml:"address"`
	ReadHeaderTimeoutSeconds int      `yaml:"readHeaderTimeoutSeconds"`
	ShutdownTimeoutSeconds   int      `yaml:"shutdownTimeoutSeconds"`
	TrustedProxies           []string `yaml:"trustedProxies"`
}

type Database struct {
	Host                         string `yaml:"host"`
	Port                         int    `yaml:"port"`
	Name                         string `yaml:"name"`
	User                         string `yaml:"user"`
	Password                     string `yaml:"password"`
	MaxOpenConnections           int    `yaml:"maxOpenConnections"`
	MaxIdleConnections           int    `yaml:"maxIdleConnections"`
	ConnectionMaxLifetimeSeconds int    `yaml:"connectionMaxLifetimeSeconds"`
}

type Swagger struct {
	Enabled bool `yaml:"enabled"`
}

type Log struct {
	Level string `yaml:"level"`
}

type JWT struct {
	Secret                string `yaml:"secret"`
	AccessTokenTTLSeconds int    `yaml:"accessTokenTTLSeconds"`
}

type Market struct {
	CacheTTLSeconds       int `yaml:"cacheTTLSeconds"`
	RequestTimeoutSeconds int `yaml:"requestTimeoutSeconds"`
}

func Load(path string) (Config, error) {
	file, err := os.Open(path)
	if err != nil {
		return Config{}, fmt.Errorf("open config: %w", err)
	}
	defer file.Close()

	var cfg Config
	decoder := yaml.NewDecoder(file)
	decoder.KnownFields(true)
	if err := decoder.Decode(&cfg); err != nil {
		return Config{}, fmt.Errorf("decode config: %w", err)
	}
	if address := os.Getenv("THS_SERVER_ADDRESS"); address != "" {
		cfg.Server.Address = address
	}
	if password := os.Getenv("THS_DATABASE_PASSWORD"); password != "" {
		cfg.Database.Password = password
	}
	if secret := os.Getenv("THS_JWT_SECRET"); secret != "" {
		cfg.JWT.Secret = secret
	}
	if cfg.Server.Address == "" {
		return Config{}, fmt.Errorf("server.address is required")
	}
	if cfg.Server.ReadHeaderTimeoutSeconds <= 0 {
		return Config{}, fmt.Errorf("server.readHeaderTimeoutSeconds must be greater than zero")
	}
	if cfg.Server.ShutdownTimeoutSeconds <= 0 {
		return Config{}, fmt.Errorf("server.shutdownTimeoutSeconds must be greater than zero")
	}
	if cfg.Database.Host == "" || cfg.Database.Port <= 0 || cfg.Database.Name == "" || cfg.Database.User == "" {
		return Config{}, fmt.Errorf("database host, port, name and user are required")
	}
	if cfg.Database.MaxOpenConnections <= 0 || cfg.Database.MaxIdleConnections < 0 {
		return Config{}, fmt.Errorf("database connection pool values are invalid")
	}
	if cfg.Database.ConnectionMaxLifetimeSeconds <= 0 {
		return Config{}, fmt.Errorf("database.connectionMaxLifetimeSeconds must be greater than zero")
	}
	if len(cfg.JWT.Secret) < 32 {
		return Config{}, fmt.Errorf("jwt.secret must contain at least 32 characters")
	}
	if cfg.JWT.AccessTokenTTLSeconds <= 0 {
		return Config{}, fmt.Errorf("jwt.accessTokenTTLSeconds must be greater than zero")
	}
	if cfg.Market.CacheTTLSeconds <= 0 || cfg.Market.RequestTimeoutSeconds <= 0 {
		return Config{}, fmt.Errorf("market cache and request timeout values must be greater than zero")
	}

	return cfg, nil
}
