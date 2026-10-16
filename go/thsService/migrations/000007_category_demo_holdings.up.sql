CREATE TABLE demo_category_holdings (
    user_id BIGINT UNSIGNED NOT NULL,
    category VARCHAR(32) NOT NULL,
    generation VARCHAR(32) NOT NULL,
    total_assets DECIMAL(20,2) NOT NULL,
    payload JSON NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY(user_id,category),
    CONSTRAINT fk_demo_category_holdings_user FOREIGN KEY(user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
