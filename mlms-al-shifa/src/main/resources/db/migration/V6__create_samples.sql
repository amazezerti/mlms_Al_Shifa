CREATE TABLE samples (
    id            BIGSERIAL   PRIMARY KEY,
    barcode       VARCHAR(50) NOT NULL UNIQUE,
    order_id      BIGINT      NOT NULL REFERENCES test_orders(id) ON DELETE RESTRICT,
    department_id BIGINT      NOT NULL REFERENCES departments(id) ON DELETE RESTRICT,
    sample_type   VARCHAR(50),
    status        VARCHAR(20) NOT NULL DEFAULT 'COLLECTED',
    collected_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    collected_by  BIGINT REFERENCES users(id) ON DELETE SET NULL,
    received_at   TIMESTAMP,
    notes         TEXT
);
-- sample status values: COLLECTED, RECEIVED, PROCESSING, REJECTED