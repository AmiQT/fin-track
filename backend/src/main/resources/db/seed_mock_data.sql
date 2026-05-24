-- Mock data for FinTrack Pro

-- 1. Insert Employees
INSERT INTO employees (employee_code, full_name, email, phone, department, position, basic_salary, housing_allowance, transport_allowance, join_date, status)
VALUES 
('EMP001', 'Ali Bin Ahmad', 'ali@fintrack.com', '012-3456789', 'IT', 'Senior Developer', 5500.00, 500.00, 200.00, '2024-01-15', 'ACTIVE'),
('EMP002', 'Siti Nurhaliza', 'siti@fintrack.com', '013-9876543', 'HR', 'HR Manager', 7500.00, 800.00, 300.00, '2023-06-10', 'ACTIVE'),
('EMP003', 'Abu Bakar', 'abu@fintrack.com', '017-1112223', 'Sales', 'Sales Executive', 4200.00, 400.00, 500.00, '2025-02-01', 'ACTIVE');

-- 2. Insert Payroll Data (Jan - Apr 2026) to show trend
-- Assuming employee IDs are 1, 2, 3
INSERT INTO payroll (employee_id, month, year, basic_salary, housing_allowance, transport_allowance, gross_salary, epf_employee, socso_employee, income_tax, total_deductions, net_salary)
VALUES 
(1, 1, 2026, 5500, 500, 200, 6200, 682, 30, 150, 862, 5338),
(2, 1, 2026, 7500, 800, 300, 8600, 946, 40, 350, 1336, 7264),
(1, 2, 2026, 5500, 500, 200, 6200, 682, 30, 150, 862, 5338),
(2, 2, 2026, 7500, 800, 300, 8600, 946, 40, 350, 1336, 7264),
(3, 2, 2026, 4200, 400, 500, 5100, 561, 25, 80, 666, 4434),
(1, 3, 2026, 5500, 500, 200, 6200, 682, 30, 150, 862, 5338),
(2, 3, 2026, 7500, 800, 300, 8600, 946, 40, 350, 1336, 7264),
(3, 3, 2026, 4200, 400, 500, 5100, 561, 25, 80, 666, 4434),
(1, 4, 2026, 5500, 500, 200, 6200, 682, 30, 150, 862, 5338),
(2, 4, 2026, 7500, 800, 300, 8600, 946, 40, 350, 1336, 7264),
(3, 4, 2026, 4200, 400, 500, 5100, 561, 25, 80, 666, 4434);

-- 3. Insert Leave Requests
INSERT INTO leave_requests (employee_id, leave_type, start_date, end_date, total_days, reason, status)
VALUES 
(1, 'ANNUAL', '2026-05-20', '2026-05-22', 3, 'Balik kampung sat.', 'PENDING'),
(2, 'SICK', '2026-05-12', '2026-05-12', 1, 'Demam panas.', 'APPROVED'),
(3, 'ANNUAL', '2026-06-01', '2026-06-05', 5, 'Family vacation.', 'PENDING');
