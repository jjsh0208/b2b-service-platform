-- 허브 테이블
CREATE TABLE IF NOT EXISTS p_hub (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by UUID NOT NULL,
    updated_at TIMESTAMP,
    updated_by UUID,
    deleted_at TIMESTAMP,
    deleted_by UUID
);

-- 허브간 연결 정보 테이블
CREATE TABLE IF NOT EXISTS p_hub_connection (
    id UUID PRIMARY KEY,
    hub_id UUID NOT NULL REFERENCES p_hub(id),
    connected_hub_id UUID NOT NULL REFERENCES p_hub(id),
    weight INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    created_by UUID NOT NULL,
    updated_at TIMESTAMP,
    updated_by UUID,
    deleted_at TIMESTAMP,
    deleted_by UUID
);
