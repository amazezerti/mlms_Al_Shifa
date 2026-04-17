CREATE TABLE patients (
    id             BIGSERIAL    PRIMARY KEY,
    patient_code   VARCHAR(20)  NOT NULL UNIQUE,
    full_name      VARCHAR(150) NOT NULL,
    date_of_birth  DATE,
    gender         VARCHAR(10),
    phone          VARCHAR(20),
    address        TEXT,
    blood_group    VARCHAR(5),
    registered_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    registered_by  BIGINT REFERENCES users(id) ON DELETE SET NULL
);

-- APPOINTMENTS
CREATE TABLE appointments (
    id             BIGSERIAL   PRIMARY KEY,
    patient_id     BIGINT      NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    scheduled_at   TIMESTAMP   NOT NULL,
    reason         VARCHAR(255),
    status         VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    notes          TEXT,
    created_by     BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at     TIMESTAMP   NOT NULL DEFAULT NOW()
);
-- appointment status values: SCHEDULED, COMPLETED, CANCELLED