-- Replace only legacy generated management codes. Preserve manually maintained values.
CREATE TABLE alipay_management_code_backup (
 record_id BIGINT UNSIGNED PRIMARY KEY,
 manage_code VARCHAR(255) NOT NULL
);
START TRANSACTION;
INSERT INTO alipay_management_code_backup(record_id,manage_code)
SELECT id,JSON_UNQUOTE(JSON_EXTRACT(payload,'$.manageCode'))
FROM alipay_records WHERE kind='insurance'
AND JSON_UNQUOTE(JSON_EXTRACT(payload,'$.manageCode')) REGEXP '^SIM-I-[0-9]+$';
UPDATE alipay_records r JOIN alipay_management_code_backup b ON b.record_id=r.id
SET r.payload=JSON_SET(r.payload,'$.manageCode',CONCAT('110500',
 CAST(1 + MOD(CONV(SUBSTRING(SHA2(CONCAT('management-code-v1:', r.id),256),1,1),16,10),9) AS CHAR),
 CAST(1 + MOD(CONV(SUBSTRING(SHA2(CONCAT('management-code-v1:', r.id),256),2,1),16,10),9) AS CHAR),
 CAST(1 + MOD(CONV(SUBSTRING(SHA2(CONCAT('management-code-v1:', r.id),256),3,1),16,10),9) AS CHAR),
 CAST(1 + MOD(CONV(SUBSTRING(SHA2(CONCAT('management-code-v1:', r.id),256),4,1),16,10),9) AS CHAR),
 CAST(1 + MOD(CONV(SUBSTRING(SHA2(CONCAT('management-code-v1:', r.id),256),5,1),16,10),9) AS CHAR),
 CAST(1 + MOD(CONV(SUBSTRING(SHA2(CONCAT('management-code-v1:', r.id),256),6,1),16,10),9) AS CHAR),
 CAST(1 + MOD(CONV(SUBSTRING(SHA2(CONCAT('management-code-v1:', r.id),256),7,1),16,10),9) AS CHAR),
 CAST(1 + MOD(CONV(SUBSTRING(SHA2(CONCAT('management-code-v1:', r.id),256),8,1),16,10),9) AS CHAR),
 CAST(1 + MOD(CONV(SUBSTRING(SHA2(CONCAT('management-code-v1:', r.id),256),9,1),16,10),9) AS CHAR)
)),r.version=REPLACE(UUID(),'-','');
COMMIT;
