CREATE TABLE IF NOT EXISTS leave_requests (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT REFERENCES employees(id),
    leave_type VARCHAR(50) NOT NULL,    -- ANNUAL, SICK, UNPAID
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days INTEGER NOT NULL,
    reason TEXT,
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    approved_by BIGINT REFERENCES users(id),
    rejection_reason TEXT,
    applied_at TIMESTAMP DEFAULT NOW(),
    reviewed_at TIMESTAMP
);
