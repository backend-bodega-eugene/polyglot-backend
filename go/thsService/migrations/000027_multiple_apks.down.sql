DELETE FROM apk_download_state WHERE id IN (2,3,4);
ALTER TABLE apk_download_audits DROP COLUMN app_id;
