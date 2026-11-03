-- 恢复本次清理前的业务文本；生成新版本，避免恢复后接受旧客户端写入。
START TRANSACTION;

UPDATE `electricity_profiles` t JOIN data_text_cleanup_backup b ON b.table_name='electricity_profiles' AND b.row_key=CAST(t.`user_id` AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_0900_ai_ci
SET t.`display_name`=JSON_UNQUOTE(JSON_EXTRACT(b.original_data, '$.display_name')),
 t.`address`=JSON_UNQUOTE(JSON_EXTRACT(b.original_data, '$.address')),
 t.`supplier`=JSON_UNQUOTE(JSON_EXTRACT(b.original_data, '$.supplier')),
 t.`meter_reader`=JSON_UNQUOTE(JSON_EXTRACT(b.original_data, '$.meter_reader')),
 t.version=REPLACE(UUID(), '-', '');

UPDATE `tax_profiles` t JOIN data_text_cleanup_backup b ON b.table_name='tax_profiles' AND b.row_key=CAST(t.`user_id` AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_0900_ai_ci
SET t.`name`=JSON_UNQUOTE(JSON_EXTRACT(b.original_data, '$.name')),
 t.`card_type`=JSON_UNQUOTE(JSON_EXTRACT(b.original_data, '$.card_type')),
 t.`huji_city`=JSON_UNQUOTE(JSON_EXTRACT(b.original_data, '$.huji_city')),
 t.`huji_address`=JSON_UNQUOTE(JSON_EXTRACT(b.original_data, '$.huji_address')),
 t.`usually_city`=JSON_UNQUOTE(JSON_EXTRACT(b.original_data, '$.usually_city')),
 t.`usually_address`=JSON_UNQUOTE(JSON_EXTRACT(b.original_data, '$.usually_address')),
 t.`contact_city`=JSON_UNQUOTE(JSON_EXTRACT(b.original_data, '$.contact_city')),
 t.`contact_address`=JSON_UNQUOTE(JSON_EXTRACT(b.original_data, '$.contact_address')),
 t.`education`=JSON_UNQUOTE(JSON_EXTRACT(b.original_data, '$.education')),
 t.`nation`=JSON_UNQUOTE(JSON_EXTRACT(b.original_data, '$.nation')),
 t.`country`=JSON_UNQUOTE(JSON_EXTRACT(b.original_data, '$.country')),
 t.version=REPLACE(UUID(), '-', '');

UPDATE `tax_employments` t JOIN data_text_cleanup_backup b ON b.table_name='tax_employments' AND b.row_key=CAST(t.`user_id` AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_0900_ai_ci
SET t.`company_name`=JSON_UNQUOTE(JSON_EXTRACT(b.original_data, '$.company_name')),
 t.`post`=JSON_UNQUOTE(JSON_EXTRACT(b.original_data, '$.post')),
 t.version=REPLACE(UUID(), '-', '');

UPDATE `electricity_bills` t JOIN data_text_cleanup_backup b ON b.table_name='electricity_bills' AND b.row_key=CAST(t.`id` AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_0900_ai_ci
SET t.detail=JSON_EXTRACT(b.original_data, '$.detail'),
 t.version=REPLACE(UUID(), '-', '');

UPDATE `nanwang_records` t JOIN data_text_cleanup_backup b ON b.table_name='nanwang_records' AND b.row_key=CAST(t.`id` AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_0900_ai_ci
SET t.payload=JSON_EXTRACT(b.original_data, '$.payload'),
 t.version=REPLACE(UUID(), '-', '');

UPDATE `tax_business_records` t JOIN data_text_cleanup_backup b ON b.table_name='tax_business_records' AND b.row_key=CAST(t.`id` AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_0900_ai_ci
SET t.payload=JSON_EXTRACT(b.original_data, '$.payload'),
 t.version=REPLACE(UUID(), '-', '');

UPDATE `alipay_records` t JOIN data_text_cleanup_backup b ON b.table_name='alipay_records' AND b.row_key=CAST(t.`id` AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_0900_ai_ci
SET t.payload=JSON_EXTRACT(b.original_data, '$.payload'),
 t.version=REPLACE(UUID(), '-', '');
COMMIT;
DROP TABLE data_text_cleanup_backup;

