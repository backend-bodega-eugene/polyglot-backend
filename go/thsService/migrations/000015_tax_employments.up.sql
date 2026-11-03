CREATE TABLE tax_employments (
 user_id BIGINT UNSIGNED NOT NULL PRIMARY KEY,
 company_name VARCHAR(128) NOT NULL,
 tax_id VARCHAR(64) NOT NULL,
 company_num VARCHAR(64) NOT NULL,
 post VARCHAR(64) NOT NULL,
 start_date CHAR(10) NOT NULL,
 end_date CHAR(10) NOT NULL DEFAULT '',
 version VARCHAR(32) NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
 CONSTRAINT fk_tax_employment_user FOREIGN KEY(user_id) REFERENCES users(id),
 KEY idx_tax_employment_updated(updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE tax_employment_audits (
 id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
 user_id BIGINT UNSIGNED NOT NULL,
 admin_id BIGINT UNSIGNED NULL,
 action VARCHAR(24) NOT NULL,
 before_payload JSON NULL,
 after_payload JSON NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 KEY idx_tax_employment_audit_user(user_id,created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
