-- Keep the previously published APK and its passcode in the first (南网) slot.
INSERT INTO apk_download_state(id,release_notes,passcode_hash)
SELECT 2,'','$2a$10$q.Qg4W59iszjKG3cZrf7fuFEL46J79a8Ga8jMFrRX5RaGigz/WQOm' FROM apk_download_state WHERE id=1;
INSERT INTO apk_download_state(id,release_notes,passcode_hash)
SELECT 3,'','$2a$10$q.Qg4W59iszjKG3cZrf7fuFEL46J79a8Ga8jMFrRX5RaGigz/WQOm' FROM apk_download_state WHERE id=1;
INSERT INTO apk_download_state(id,release_notes,passcode_hash)
SELECT 4,'','$2a$10$q.Qg4W59iszjKG3cZrf7fuFEL46J79a8Ga8jMFrRX5RaGigz/WQOm' FROM apk_download_state WHERE id=1;
ALTER TABLE apk_download_audits ADD COLUMN app_id BIGINT UNSIGNED NOT NULL DEFAULT 1 AFTER id;
