START TRANSACTION;
UPDATE alipay_records r JOIN alipay_management_code_backup b ON b.record_id=r.id
SET r.payload=JSON_SET(r.payload,'$.manageCode',b.manage_code),
r.version=REPLACE(UUID(),'-','');
COMMIT;
DROP TABLE alipay_management_code_backup;
