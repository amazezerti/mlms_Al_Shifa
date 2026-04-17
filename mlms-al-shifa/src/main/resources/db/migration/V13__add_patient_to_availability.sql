-- V12__add_patient_to_availability.sql
-- Adds patient reference to doctor_availability slots
-- so doctors can see who booked their appointments

ALTER TABLE doctor_availability
    ADD COLUMN IF NOT EXISTS patient_id BIGINT,
    ADD COLUMN IF NOT EXISTS appointment_reason
        VARCHAR(200);

ALTER TABLE doctor_availability
    ADD CONSTRAINT fk_availability_patient
    FOREIGN KEY (patient_id)
    REFERENCES patients(id)
    ON DELETE SET NULL;