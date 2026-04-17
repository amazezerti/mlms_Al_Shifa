-- TEST ORDERS
-- Created by Receptionist (DRAFT), confirmed by Doctor (CONFIRMED)
CREATE TABLE test_orders (
    id             BIGSERIAL    PRIMARY KEY,
    order_code     VARCHAR(20)  NOT NULL UNIQUE,
    patient_id     BIGINT       NOT NULL REFERENCES patients(id)  ON DELETE RESTRICT,
    status         VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    priority       VARCHAR(10)  NOT NULL DEFAULT 'NORMAL',
    clinical_note  TEXT,
    ordered_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_by     BIGINT REFERENCES users(id) ON DELETE SET NULL,
    confirmed_by   BIGINT REFERENCES users(id) ON DELETE SET NULL,
    confirmed_at   TIMESTAMP
);
-- order status values:  DRAFT, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED
-- order priority values: NORMAL, URGENT, STAT

-- ORDER ITEMS
-- One row per test inside an order
CREATE TABLE order_items (
    id           BIGSERIAL     PRIMARY KEY,
    order_id     BIGINT        NOT NULL REFERENCES test_orders(id)  ON DELETE CASCADE,
    test_id      BIGINT        NOT NULL REFERENCES test_catalog(id) ON DELETE RESTRICT,
    item_status  VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    unit_price   DECIMAL(10,2) NOT NULL DEFAULT 0.00
);
-- item_status values: PENDING, IN_PROGRESS, COMPLETED