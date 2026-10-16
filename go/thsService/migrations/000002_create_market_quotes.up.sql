CREATE TABLE market_instruments (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    category VARCHAR(32) NOT NULL,
    symbol VARCHAR(32) NOT NULL,
    source VARCHAR(16) NOT NULL,
    source_symbol VARCHAR(32) NOT NULL,
    name VARCHAR(64) NOT NULL,
    market VARCHAR(16) NOT NULL,
    currency VARCHAR(8) NOT NULL,
    sort_order INT UNSIGNED NOT NULL DEFAULT 0,
    fallback_price DECIMAL(24, 6) NOT NULL,
    fallback_change_amount DECIMAL(24, 6) NOT NULL DEFAULT 0,
    fallback_change_percent DECIMAL(12, 4) NOT NULL DEFAULT 0,
    enabled TINYINT UNSIGNED NOT NULL DEFAULT 1,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_market_instruments_category_symbol (category, symbol),
    KEY idx_market_instruments_category_enabled_sort (category, enabled, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE market_quote_cache (
    instrument_id BIGINT UNSIGNED NOT NULL,
    latest_price DECIMAL(24, 6) NOT NULL,
    previous_close DECIMAL(24, 6) NULL,
    change_amount DECIMAL(24, 6) NOT NULL,
    change_percent DECIMAL(12, 4) NOT NULL,
    quote_time DATETIME(3) NOT NULL,
    data_status VARCHAR(16) NOT NULL,
    raw_data TEXT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (instrument_id),
    CONSTRAINT fk_market_quote_cache_instrument
        FOREIGN KEY (instrument_id) REFERENCES market_instruments (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO market_instruments
    (category, symbol, source, source_symbol, name, market, currency, sort_order, fallback_price, fallback_change_amount, fallback_change_percent)
VALUES
    ('global', '000001.SH', 'tencent', 's_sh000001', '上证指数', 'CN', 'CNY', 10, 3800.86, 3.62, 0.10),
    ('global', 'HSI.HK', 'tencent', 's_hkHSI', '恒生指数', 'HK', 'HKD', 20, 24345.22, 74.02, 0.30),
    ('global', 'DJI.US', 'tencent', 's_usDJI', '道琼斯', 'US', 'USD', 30, 51752.61, -97.94, -0.19),
    ('global', 'IXIC.US', 'tencent', 's_usIXIC', '纳斯达克', 'US', 'USD', 40, 27122.09, 599.54, 2.26),
    ('global', 'SPX.US', 'tencent', 's_usSPX', '标普500', 'US', 'USD', 50, 7764.70, 114.20, 1.49),
    ('global', 'GC', 'tencent', 'hf_GC', '纽约黄金', 'GLOBAL', 'USD', 60, 4185.62, -4.98, -0.12),

    ('a_stock', '600519.SH', 'tencent', 'sh600519', '贵州茅台', 'CN', 'CNY', 10, 1274.89, -13.11, -1.02),
    ('a_stock', '300750.SZ', 'tencent', 'sz300750', '宁德时代', 'CN', 'CNY', 20, 289.10, -9.28, -3.11),
    ('a_stock', '002594.SZ', 'tencent', 'sz002594', '比亚迪', 'CN', 'CNY', 30, 108.00, 0.50, 0.47),
    ('a_stock', '600036.SH', 'tencent', 'sh600036', '招商银行', 'CN', 'CNY', 40, 42.00, 0.20, 0.48),
    ('a_stock', '601318.SH', 'tencent', 'sh601318', '中国平安', 'CN', 'CNY', 50, 58.00, -0.10, -0.17),

    ('hk_stock', '00700.HK', 'tencent', 'hk00700', '腾讯控股', 'HK', 'HKD', 10, 425.20, -0.60, -0.14),
    ('hk_stock', '09988.HK', 'tencent', 'hk09988', '阿里巴巴-W', 'HK', 'HKD', 20, 107.20, -1.00, -0.92),
    ('hk_stock', '01810.HK', 'tencent', 'hk01810', '小米集团-W', 'HK', 'HKD', 30, 27.20, -0.38, -1.38),
    ('hk_stock', '03690.HK', 'tencent', 'hk03690', '美团-W', 'HK', 'HKD', 40, 102.00, 1.00, 0.99),
    ('hk_stock', '00941.HK', 'tencent', 'hk00941', '中国移动', 'HK', 'HKD', 50, 88.00, 0.30, 0.34),

    ('us_stock', 'AAPL.US', 'tencent', 'usAAPL', '苹果', 'US', 'USD', 10, 345.45, 4.48, 1.31),
    ('us_stock', 'NVDA.US', 'tencent', 'usNVDA', '英伟达', 'US', 'USD', 20, 224.57, -1.75, -0.77),
    ('us_stock', 'MSFT.US', 'tencent', 'usMSFT', '微软', 'US', 'USD', 30, 520.00, 2.00, 0.39),
    ('us_stock', 'TSLA.US', 'tencent', 'usTSLA', '特斯拉', 'US', 'USD', 40, 430.00, -3.00, -0.69),
    ('us_stock', 'AMZN.US', 'tencent', 'usAMZN', '亚马逊', 'US', 'USD', 50, 220.00, 1.10, 0.50),

    ('etf', '510300.SH', 'tencent', 'sh510300', '沪深300ETF', 'CN', 'CNY', 10, 4.345, -0.015, -0.34),
    ('etf', '510050.SH', 'tencent', 'sh510050', '上证50ETF', 'CN', 'CNY', 20, 2.90, 0.01, 0.35),
    ('etf', '159915.SZ', 'tencent', 'sz159915', '创业板ETF', 'CN', 'CNY', 30, 2.974, -0.009, -0.30),
    ('etf', '588000.SH', 'tencent', 'sh588000', '科创50ETF', 'CN', 'CNY', 40, 1.20, 0.02, 1.69),
    ('etf', '512100.SH', 'tencent', 'sh512100', '中证1000ETF', 'CN', 'CNY', 50, 2.60, 0.01, 0.39),

    ('future', 'CU0', 'sina', 'nf_CU0', '沪铜主连', 'CN', 'CNY', 10, 110720.00, -180.00, -0.16),
    ('future', 'SC0', 'sina', 'nf_SC0', '原油主连', 'CN', 'CNY', 20, 747.20, 15.20, 2.08),
    ('future', 'RB0', 'sina', 'nf_RB0', '螺纹钢主连', 'CN', 'CNY', 30, 3100.00, 12.00, 0.39),
    ('future', 'M0', 'sina', 'nf_M0', '豆粕主连', 'CN', 'CNY', 40, 3000.00, -8.00, -0.27),
    ('future', 'IF0', 'sina', 'nf_IF0', '沪深300主连', 'CN', 'CNY', 50, 4508.80, 48.40, 1.09),

    ('gold', 'AU0', 'sina', 'nf_AU0', '沪金主连', 'CN', 'CNY', 10, 900.44, -2.38, -0.26),
    ('gold', 'GC', 'tencent', 'hf_GC', '纽约黄金', 'GLOBAL', 'USD', 20, 4185.62, -4.98, -0.12),
    ('gold', 'XAU', 'tencent', 'hf_XAU', '伦敦金', 'GLOBAL', 'USD', 30, 4164.02, -7.46, -0.18),
    ('gold', 'AG0', 'sina', 'nf_AG0', '沪银主连', 'CN', 'CNY', 40, 14463.00, -225.00, -1.53),

    ('convertible_bond', '110081.SH', 'tencent', 'sh110081', '闻泰转债', 'CN', 'CNY', 10, 97.494, -0.506, -0.52),
    ('convertible_bond', '113050.SH', 'tencent', 'sh113050', '南银转债', 'CN', 'CNY', 20, 144.967, 0.00, 0.00),
    ('convertible_bond', '127049.SZ', 'tencent', 'sz127049', '希望转2', 'CN', 'CNY', 30, 112.00, 0.20, 0.18),
    ('convertible_bond', '123107.SZ', 'tencent', 'sz123107', '温氏转债', 'CN', 'CNY', 40, 124.00, -0.30, -0.24),
    ('convertible_bond', '113044.SH', 'tencent', 'sh113044', '大秦转债', 'CN', 'CNY', 50, 119.00, 0.10, 0.08),

    ('other', 'CL', 'tencent', 'hf_CL', '纽约原油', 'GLOBAL', 'USD', 10, 94.09, 0.35, 0.38),
    ('other', 'SI', 'tencent', 'hf_SI', '纽约白银', 'GLOBAL', 'USD', 20, 59.59, -0.92, -1.53),
    ('other', '000001.SH', 'tencent', 's_sh000001', '上证指数', 'CN', 'CNY', 30, 3800.86, 3.62, 0.10),
    ('other', '399001.SZ', 'tencent', 's_sz399001', '深证成指', 'CN', 'CNY', 40, 12353.70, -31.17, -0.25);
