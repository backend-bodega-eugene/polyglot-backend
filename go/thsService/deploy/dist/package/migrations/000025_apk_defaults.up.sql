UPDATE apk_download_state
SET passcode_hash='$2a$10$q.Qg4W59iszjKG3cZrf7fuFEL46J79a8Ga8jMFrRX5RaGigz/WQOm'
WHERE id=1 AND passcode_hash='';
UPDATE apk_download_state
SET version_name=DATE_FORMAT(uploaded_at,'%Y%m%d%H%i%s')
WHERE id=1 AND uploaded_at IS NOT NULL AND version_name NOT REGEXP '^[0-9]{14}$';
