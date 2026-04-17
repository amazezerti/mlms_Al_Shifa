CREATE TABLE reagents (
    id                BIGSERIAL    PRIMARY KEY,
    reagent_code      VARCHAR(30)  NOT NULL UNIQUE,
    name              VARCHAR(150) NOT NULL,
    department_id     BIGINT REFERENCES departments(id) ON DELETE SET NULL,
    category          VARCHAR(80),
    unit              VARCHAR(20),
    quantity_in_stock INT          NOT NULL DEFAULT 0,
    minimum_stock     INT          NOT NULL DEFAULT 5,
    expiry_date       DATE,
    supplier          VARCHAR(150),
    updated_at        TIMESTAMP    NOT NULL DEFAULT NOW()
);