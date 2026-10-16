ALTER TABLE demo_portfolio_trades DROP COLUMN currency, DROP COLUMN fees;
ALTER TABLE demo_portfolio_positions DROP COLUMN currency, DROP COLUMN exchange_rate, DROP COLUMN holding_days, DROP COLUMN realized_profit, DROP COLUMN transaction_fees, DROP COLUMN daily_profit_percent;
