-- ROLES
CREATE TABLE roles (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(255)
);

INSERT INTO roles (name, description) VALUES
    ('ROLE_ADMIN',          'Full system access and user management'),
    ('ROLE_RECEPTIONIST',   'Patient registration, billing, appointments, print results'),
    ('ROLE_LAB_TECHNICIAN', 'Sample collection and result entry'),
    ('ROLE_DOCTOR',         'Order confirmation, result verification, print results');

-- USERS
CREATE TABLE users (
    id           BIGSERIAL    PRIMARY KEY,
    full_name    VARCHAR(150) NOT NULL,
    username     VARCHAR(100) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    email        VARCHAR(150),
    phone        VARCHAR(20),
    enabled      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- USER_ROLES join table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id)  ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id)  ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- USER_DEPARTMENTS
-- Links lab technicians to their department
-- Doctors and receptionists may also be linked optionally
CREATE TABLE user_departments (
    user_id       BIGINT NOT NULL REFERENCES users(id)       ON DELETE CASCADE,
    department_id BIGINT NOT NULL REFERENCES departments(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, department_id)
);