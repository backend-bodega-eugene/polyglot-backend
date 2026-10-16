CREATE TABLE users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    phone VARCHAR(20) NULL,
    email VARCHAR(254) NULL,
    real_name VARCHAR(64) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status TINYINT UNSIGNED NOT NULL DEFAULT 1,
    last_login_at DATETIME(3) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_phone (phone),
    UNIQUE KEY uq_users_email (email),
    CONSTRAINT chk_users_login_account CHECK (phone IS NOT NULL OR email IS NOT NULL),
    CONSTRAINT chk_users_status CHECK (status IN (1, 2))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
