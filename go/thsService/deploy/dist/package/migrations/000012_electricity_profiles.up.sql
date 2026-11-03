CREATE TABLE electricity_profiles (
 user_id BIGINT UNSIGNED NOT NULL PRIMARY KEY,
 display_name VARCHAR(64) NOT NULL,
 address VARCHAR(255) NOT NULL,
 user_id_number VARCHAR(32) NOT NULL,
 supplier VARCHAR(128) NOT NULL,
 meter_reader VARCHAR(64) NOT NULL,
 meter_no VARCHAR(32) NOT NULL,
 postal_code VARCHAR(6) NOT NULL,
 version VARCHAR(32) NOT NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
 CONSTRAINT fk_electricity_profile_user FOREIGN KEY(user_id) REFERENCES users(id),
 KEY idx_electricity_profile_updated(updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE electricity_profile_audits (
 id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
 user_id BIGINT UNSIGNED NOT NULL,
 admin_id BIGINT UNSIGNED NULL,
 action VARCHAR(24) NOT NULL,
 before_payload JSON NULL,
 after_payload JSON NULL,
 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
 KEY idx_electricity_audit_user(user_id,created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
