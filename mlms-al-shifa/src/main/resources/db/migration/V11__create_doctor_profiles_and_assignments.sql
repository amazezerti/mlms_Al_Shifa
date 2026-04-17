-- DOCTOR PROFILES
-- Extended info for users with ROLE_DOCTOR
CREATE TABLE doctor_profiles (
    id               BIGSERIAL    PRIMARY KEY,
    user_id          BIGINT       NOT NULL UNIQUE
                                  REFERENCES users(id) ON DELETE CASCADE,
    department_id    BIGINT       NOT NULL
                                  REFERENCES departments(id) ON DELETE RESTRICT,
    specialization   VARCHAR(150),
    qualification    VARCHAR(200),
    license_number   VARCHAR(80),
    bio              TEXT,
    profile_pic      VARCHAR(255),
    years_experience INT          DEFAULT 0,
    active           BOOLEAN      NOT NULL DEFAULT TRUE
);

-- DOCTOR AVAILABILITY SLOTS
-- Each row = one working slot for a doctor
CREATE TABLE doctor_availability (
    id            BIGSERIAL   PRIMARY KEY,
    doctor_id     BIGINT      NOT NULL
                               REFERENCES users(id) ON DELETE CASCADE,
    slot_date     DATE        NOT NULL,
    start_time    TIME        NOT NULL,
    end_time      TIME        NOT NULL,
    is_booked     BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- PATIENT ASSIGNMENTS
-- Links a patient to a doctor (via department load balancing)
CREATE TABLE patient_assignments (
    id             BIGSERIAL   PRIMARY KEY,
    patient_id     BIGINT      NOT NULL
                                REFERENCES patients(id) ON DELETE CASCADE,
    doctor_id      BIGINT      NOT NULL
                                REFERENCES users(id) ON DELETE RESTRICT,
    department_id  BIGINT      NOT NULL
                                REFERENCES departments(id) ON DELETE RESTRICT,
    assigned_at    TIMESTAMP   NOT NULL DEFAULT NOW(),
    assigned_by    BIGINT      REFERENCES users(id) ON DELETE SET NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    notes          TEXT
);
-- assignment status: ACTIVE, COMPLETED, CANCELLED

-- Update appointments table to link to availability slot and doctor
ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS doctor_id          BIGINT
        REFERENCES users(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS availability_slot_id BIGINT
        REFERENCES doctor_availability(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS department_id      BIGINT
        REFERENCES departments(id) ON DELETE SET NULL;