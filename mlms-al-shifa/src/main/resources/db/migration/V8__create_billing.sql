-- INVOICES
CREATE TABLE invoices (
    id              BIGSERIAL     PRIMARY KEY,
    invoice_number  VARCHAR(30)   NOT NULL UNIQUE,
    order_id        BIGINT        NOT NULL REFERENCES test_orders(id) ON DELETE RESTRICT,
    patient_id      BIGINT        NOT NULL REFERENCES patients(id)    ON DELETE RESTRICT,
    total_amount    DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount        DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    net_amount      DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status          VARCHAR(20)   NOT NULL DEFAULT 'UNPAID',
    issued_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    issued_by       BIGINT REFERENCES users(id) ON DELETE SET NULL
);
-- invoice status values: UNPAID, PARTIAL, PAID, CANCELLED

-- PAYMENTS
CREATE TABLE payments (
    id               BIGSERIAL     PRIMARY KEY,
    invoice_id       BIGINT        NOT NULL REFERENCES invoices(id) ON DELETE RESTRICT,
    amount_paid      DECIMAL(10,2) NOT NULL,
    payment_method   VARCHAR(20)   NOT NULL DEFAULT 'CASH',
    reference_number VARCHAR(100),
    paid_at          TIMESTAMP     NOT NULL DEFAULT NOW(),
    received_by      BIGINT REFERENCES users(id) ON DELETE SET NULL
);
-- payment_method values: CASH, CARD, MOBILE, INSURANCE