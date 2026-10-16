CREATE TABLE demo_portfolio_accounts (
    user_id BIGINT UNSIGNED NOT NULL,
    generation VARCHAR(32) NOT NULL,
    currency VARCHAR(8) NOT NULL,
    total_assets DECIMAL(20,2) NOT NULL,
    floating_profit DECIMAL(20,2) NOT NULL,
    daily_profit DECIMAL(20,2) NOT NULL,
    daily_profit_percent DECIMAL(12,2) NOT NULL,
    total_market_value DECIMAL(20,2) NOT NULL,
    available_cash DECIMAL(20,2) NOT NULL,
    withdrawable_cash DECIMAL(20,2) NOT NULL,
    position_ratio DECIMAL(12,2) NOT NULL,
    generated_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (user_id),
    CONSTRAINT fk_demo_portfolio_account_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE demo_portfolio_positions (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    generation VARCHAR(32) NOT NULL,
    symbol VARCHAR(32) NOT NULL,
    name VARCHAR(64) NOT NULL,
    market_value DECIMAL(20,2) NOT NULL,
    profit DECIMAL(20,2) NOT NULL,
    profit_percent DECIMAL(12,2) NOT NULL,
    daily_profit DECIMAL(20,2) NOT NULL,
    quantity BIGINT NOT NULL,
    available_quantity BIGINT NOT NULL,
    cost_price DECIMAL(20,4) NOT NULL,
    latest_price DECIMAL(20,4) NOT NULL,
    quote_time DATETIME(3) NOT NULL,
    data_status VARCHAR(16) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY(id),
    UNIQUE KEY uq_demo_position_user_generation_symbol (user_id,generation,symbol),
    CONSTRAINT fk_demo_position_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE demo_portfolio_trades (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    generation VARCHAR(32) NOT NULL,
    symbol VARCHAR(32) NOT NULL,
    name VARCHAR(64) NOT NULL,
    side VARCHAR(8) NOT NULL,
    quantity BIGINT NOT NULL,
    price DECIMAL(20,4) NOT NULL,
    amount DECIMAL(20,2) NOT NULL,
    traded_at DATETIME(3) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY(id),
    KEY idx_demo_trade_user_generation_time (user_id,generation,traded_at,id),
    CONSTRAINT fk_demo_trade_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
