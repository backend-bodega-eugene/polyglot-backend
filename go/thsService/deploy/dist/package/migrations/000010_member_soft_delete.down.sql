DROP TABLE IF EXISTS admin_member_audits;
ALTER TABLE users DROP INDEX idx_users_deleted_at, DROP COLUMN deleted_at;
