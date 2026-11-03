CREATE TABLE administrators (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    is_super_admin BOOLEAN NOT NULL DEFAULT FALSE,
    status TINYINT UNSIGNED NOT NULL DEFAULT 1,
    last_login_at DATETIME(3) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_administrators_username (username),
    CONSTRAINT chk_administrators_status CHECK (status IN (1, 2))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO administrators (username, password_hash, is_super_admin)
VALUES ('eugene', '$2a$10$JVrUKXTjyKqPWvu3JGJF1OFHNfZSMueXHmjIK6P72fmbGFARxFjAC', TRUE);
