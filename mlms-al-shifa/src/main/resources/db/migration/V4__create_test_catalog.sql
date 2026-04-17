CREATE TABLE test_catalog (
    id                BIGSERIAL     PRIMARY KEY,
    test_code         VARCHAR(20)   NOT NULL UNIQUE,
    test_name         VARCHAR(150)  NOT NULL,
    department_id     BIGINT        NOT NULL REFERENCES departments(id) ON DELETE RESTRICT,
    sample_type       VARCHAR(50),
    turnaround_hours  INT           NOT NULL DEFAULT 24,
    price             DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    normal_range      VARCHAR(100),
    unit              VARCHAR(30),
    active            BOOLEAN       NOT NULL DEFAULT TRUE
);