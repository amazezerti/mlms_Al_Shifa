CREATE TABLE departments (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    active      BOOLEAN      NOT NULL DEFAULT TRUE
);

INSERT INTO departments (name, description) VALUES
    ('Hematology',    'Blood tests and disorders'),
    ('Biochemistry',  'Chemical analysis of body fluids'),
    ('Microbiology',  'Bacteria, viruses and infections'),
    ('Immunology',    'Immune system and antibody tests'),
    ('Urinalysis',    'Urine examination and analysis');