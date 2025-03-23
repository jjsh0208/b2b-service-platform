-- COM_DVL 데이터 추가(레코드 170)
INSERT INTO p_shipping_agent (
    id, hub_id, shipping_manager_id, shipping_manager_slack_id,
    type, shipping_sequence, is_transit, created_at, created_by,
    updated_at, updated_by, deleted_at, deleted_by
)
SELECT
    gen_random_uuid() AS id,
    (SELECT '11111111-1111-1111-1111-' || LPAD(to_hex(mod(gs, 17) + 1), 12, '1') ) ::uuid AS hub_id,
    u.id AS shipping_manager_id,
    u.slack_id AS shipping_manager_slack_id,
    'COM_DVL' AS type,
    gs AS shipping_sequence,
    CASE WHEN gs % 3 = 0 THEN TRUE ELSE FALSE END AS is_transit,
    now() - (random() * interval '365 days') AS created_at,
    '사용자' || LPAD(gs::TEXT, 3, '0') AS created_by,
    NULL AS updated_at,
    NULL AS updated_by,
    NULL AS deleted_at,
    NULL AS deleted_by
FROM
    generate_series(1, 170) AS gs
        JOIN p_user u ON 'slack' || LPAD(gs::TEXT, 3, '0') = u.slack_id;

-- HUB_DVL 데이터 추가(레코드 10)
INSERT INTO p_shipping_agent (
    id, hub_id, shipping_manager_id, shipping_manager_slack_id,
    type, shipping_sequence, is_transit, created_at, created_by,
    updated_at, updated_by, deleted_at, deleted_by
)
SELECT
    gen_random_uuid() AS id,
    (SELECT '11111111-1111-1111-1111-' || LPAD(to_hex(mod(gs, 17) + 1), 12, '1') ) ::uuid AS hub_id,
    u.id AS shipping_manager_id,
    u.slack_id AS shipping_manager_slack_id,
    'HUB_DVL' AS type,
    gs + 170 AS shipping_sequence,
    CASE WHEN gs % 3 = 0 THEN TRUE ELSE FALSE END AS is_transit,
    now() - (random() * interval '365 days') AS created_at,
    '사용자' || LPAD((gs+170)::TEXT, 3, '0') AS created_by,
    NULL AS updated_at,
    NULL AS updated_by,
    NULL AS deleted_at,
    NULL AS deleted_by
FROM
    generate_series(1, 10) AS gs
        JOIN p_user u ON 'slack' || LPAD((gs + 170)::TEXT, 3, '0') = u.slack_id;