CREATE TABLE tax_business_scopes (
 kind VARCHAR(24) NOT NULL,
 user_id BIGINT UNSIGNED NOT NULL,
 year INT NOT NULL DEFAULT 0,
 PRIMARY KEY(kind,user_id,year),
 CONSTRAINT fk_tax_business_scope_user FOREIGN KEY(user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE tax_business_records (
 id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
 kind VARCHAR(24) NOT NULL,
 user_id BIGINT UNSIGNED NOT NULL,
 year INT NOT NULL DEFAULT 0,
 payload JSON NOT NULL,
 version VARCHAR(32) NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
 KEY idx_tax_business_scope(kind,user_id,year,id),
 CONSTRAINT fk_tax_business_scope FOREIGN KEY(kind,user_id,year) REFERENCES tax_business_scopes(kind,user_id,year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE tax_business_audits (
 id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
 record_id BIGINT UNSIGNED NOT NULL,
 kind VARCHAR(24) NOT NULL,
 user_id BIGINT UNSIGNED NOT NULL,
 year INT NOT NULL DEFAULT 0,
 admin_id BIGINT UNSIGNED NULL,
 action VARCHAR(24) NOT NULL,
 before_payload JSON NULL,
 after_payload JSON NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 KEY idx_tax_business_audit(user_id,kind,created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
