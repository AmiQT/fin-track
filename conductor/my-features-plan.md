# Implementation Plan: My Payslips & My Leaves

## Objective
Implement the missing employee-facing features: "My Payslips" and "My Leaves". This requires adding secure backend endpoints that resolve the employee via their authenticated email, and building the corresponding React components.

## Key Files & Context
- **Backend**: `LeaveController`, `LeaveService`, `PayrollController`, `PayrollService`, `EmployeeRepository`.
- **Frontend**: `App.jsx`, `src/pages/MyLeaves.jsx` (new), `src/pages/MyPayslips.jsx` (new).

## Implementation Steps

### 1. Backend Data Resolution
- **EmployeeRepository**: Add `Optional<Employee> findByUser_Email(String email);` to fetch the employee record linked to the logged-in user.
- **LeaveService & Controller**:
  - Update `getMyLeaves(String email)` to fetch leaves by the employee's email instead of `user.getId()`.
  - Update `applyLeave(LeaveRequestDto, String email)` to resolve the employee via email so employees don't need to send their `employeeId` manually.
- **PayrollService & Controller**:
  - Add `getMyPayroll(String email)` to fetch payroll history for the authenticated employee.
  - Expose `GET /api/payroll/my`.

### 2. Frontend React Components
- **MyLeaves.jsx**:
  - Create a page that fetches and displays the employee's leaves (`/api/leaves/my`).
  - Add a form/modal to apply for new leaves (Annual, Sick, Unpaid) calling `POST /api/leaves`.
- **MyPayslips.jsx**:
  - Create a page that fetches and displays the employee's payroll history (`/api/payroll/my`).
  - Include a "Download PDF" button that calls the existing `/api/payslip/{id}/pdf` endpoint.

### 3. Frontend Routing
- **App.jsx**: Replace the `Placeholder` mock components for `/my-payslips` and `/my-leaves` with the newly created `MyPayslips` and `MyLeaves` components.

## Verification & Testing
- Login as an employee (e.g., `ali@fintrack.com`).
- Navigate to "My Leaves" to ensure past leaves load and new leaves can be applied.
- Navigate to "My Payslips" to ensure payroll history loads and PDF generation works correctly.