INSERT INTO administrators (username, password_hash, is_super_admin, status)
SELECT 'Asen', '$2a$10$QDo7VFp39A2PVhVjAmsX5Oyd5vFyg.vRtHISsWTV9DQfRM2qyYSh6', FALSE, 1
WHERE NOT EXISTS (SELECT 1 FROM administrators WHERE username = 'Asen');
