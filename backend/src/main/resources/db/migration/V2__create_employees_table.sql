CREATE TABLE IF NOT EXISTS employees (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    employee_code VARCHAR(20) UNIQUE NOT NULL,  -- EMP001, EMP002
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    department VARCHAR(100) NOT NULL,
    position VARCHAR(100) NOT NULL,
    basic_salary DECIMAL(12,2) NOT NULL,
    housing_allowance DECIMAL(12,2) DEFAULT 0,
    transport_allowance DECIMAL(12,2) DEFAULT 0,
    join_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',  -- ACTIVE, INACTIVE
    created_at TIMESTAMP DEFAULT NOW()
);
