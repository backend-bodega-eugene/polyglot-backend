CREATE TABLE electricity_bills (
 id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
 user_id BIGINT UNSIGNED NOT NULL,
 code CHAR(6) NOT NULL,
 start_date CHAR(10) NOT NULL,
 end_date CHAR(10) NOT NULL,
 amount DECIMAL(20,2) NOT NULL,
 total_usage DECIMAL(20,2) NOT NULL,
 detail JSON NOT NULL,
 version VARCHAR(32) NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
 UNIQUE KEY uq_electricity_bill_month(user_id,code),
 CONSTRAINT fk_electricity_bill_user FOREIGN KEY(user_id) REFERENCES users(id),
 KEY idx_electricity_bill_updated(updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE electricity_bill_audits (
 id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
 user_id BIGINT UNSIGNED NOT NULL,
 admin_id BIGINT UNSIGNED NULL,
 bill_id BIGINT UNSIGNED NULL,
 action VARCHAR(24) NOT NULL,
 before_payload JSON NULL,
 after_payload JSON NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 KEY idx_electricity_bill_audit_user(user_id,created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
