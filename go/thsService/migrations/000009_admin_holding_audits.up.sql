CREATE TABLE admin_holding_audits (
 id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
 admin_id BIGINT UNSIGNED NOT NULL,
 user_id BIGINT UNSIGNED NOT NULL,
 category VARCHAR(32) NOT NULL,
 action VARCHAR(16) NOT NULL,
 before_payload JSON NULL,
 after_payload JSON NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 KEY idx_holding_audit_user (user_id,category,created_at),
 KEY idx_holding_audit_admin (admin_id,created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
