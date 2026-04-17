CREATE TABLE test_results (
    id             BIGSERIAL    PRIMARY KEY,
    order_item_id  BIGINT       NOT NULL REFERENCES order_items(id) ON DELETE CASCADE,
    sample_id      BIGINT       NOT NULL REFERENCES samples(id)     ON DELETE RESTRICT,
    result_value   VARCHAR(100),
    unit           VARCHAR(30),
    normal_range   VARCHAR(100),
    flag           VARCHAR(10),
    remarks        TEXT,
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    entered_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    entered_by     BIGINT REFERENCES users(id) ON DELETE SET NULL,
    verified_by    BIGINT REFERENCES users(id) ON DELETE SET NULL,
    verified_at    TIMESTAMP
);
-- result status values: PENDING, ENTERED, VERIFIED