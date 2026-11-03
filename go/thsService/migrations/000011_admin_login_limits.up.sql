CREATE TABLE admin_login_limits (
 key_hash CHAR(64) NOT NULL PRIMARY KEY,
 attempts VARCHAR(2048) NOT NULL DEFAULT '[]',
 locked_until DATETIME(3) NULL,
 expires_at DATETIME(3) NOT NULL,
 KEY idx_login_limits_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
