-- 유저 기본 180개(배송담당자) 데이터 삽입 (임의의 UUID 사용)
CREATE TABLE IF NOT EXISTS p_user AS
SELECT
    gen_random_uuid() AS id,
    '사용자' || LPAD(gs::TEXT, 3, '0') AS username,
    '$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx' AS password,
    'tester' || LPAD(gs::TEXT, 3, '0') || '@example.com' AS email,
    'slack' || LPAD(gs::TEXT, 3, '0') AS slack_id,
    'DVL_AGENT' AS role,
    now() - (random() * interval '365 days') AS created_at,
    '사용자' || LPAD(gs::TEXT, 3, '0') AS created_by,
    NULL AS updated_at,
    NULL AS updated_by,
    NULL AS deleted_at,
    NULL AS deleted_by
FROM
    generate_series(1, 180) AS gs;