CREATE TABLE IF NOT EXISTS payroll (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT REFERENCES employees(id),
    month INTEGER NOT NULL,         -- 1-12
    year INTEGER NOT NULL,
    basic_salary DECIMAL(12,2),
    housing_allowance DECIMAL(12,2),
    transport_allowance DECIMAL(12,2),
    gross_salary DECIMAL(12,2),
    epf_employee DECIMAL(12,2),
    epf_employer DECIMAL(12,2),
    socso_employee DECIMAL(12,2),
    socso_employer DECIMAL(12,2),
    income_tax DECIMAL(12,2),
    unpaid_leave_deduction DECIMAL(12,2) DEFAULT 0,
    total_deductions DECIMAL(12,2),
    net_salary DECIMAL(12,2),
    processed_at TIMESTAMP DEFAULT NOW(),
    UNIQUE (employee_id, month, year)   -- Prevent duplicate payroll
);
