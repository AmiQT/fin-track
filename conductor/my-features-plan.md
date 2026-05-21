# Implementation Plan: My Payslips & My Leaves

> **STATUS: COMPLETED** — Semua features telah berjaya diimplementasi. Dokumen ini dikemaskini pada 2026-05-21.

---

## Objective
Implement employee-facing features: "My Payslips" dan "My Leaves" — membolehkan employee lihat rekod sendiri, apply cuti, dan download payslip PDF, semua berdasarkan authenticated email mereka.

---

## Key Files & Context

| Layer | File |
|---|---|
| Backend Repository | `EmployeeRepository.java` |
| Backend Service | `LeaveService.java`, `PayrollService.java` |
| Backend Controller | `LeaveController.java`, `PayrollController.java` |
| Frontend Pages | `src/pages/MyLeaves.jsx`, `src/pages/MyPayslips.jsx` |
| Frontend Routing | `src/App.jsx` |

---

## Implementation — DONE ✅

### 1. Backend Data Resolution

- **EmployeeRepository** — `findByUserEmail(String email)` dan `findByEmail(String email)` tersedia untuk lookup employee via email.
- **LeaveService**:
  - `getMyLeaves(String email)` — fetch semua leave request untuk employee yang logged in.
  - `applyLeave(LeaveRequestDto, String email)` — resolve employee via email, tak perlu hantar `employeeId` manual.
  - `getLeaveBalance(String email)` — return baki Annual & Sick leave untuk tahun semasa.
- **LeaveController** — expose `GET /api/leaves/my` dan `POST /api/leaves`.
- **PayrollService** — `getMyPayroll(String email)` fetch payroll history untuk authenticated employee.
- **PayrollController** — expose `GET /api/payroll/my`.

### 2. Frontend React Components

- **MyLeaves.jsx** — page untuk papar senarai leave request employee, dengan form/modal untuk apply leave baru (Annual, Sick, Unpaid).
- **MyPayslips.jsx** — page untuk papar payroll history employee, dengan butang "Download PDF" yang hit `/api/payslip/{id}/pdf`.

### 3. Frontend Routing

- **App.jsx** — route `/my-leaves` dan `/my-payslips` dah disambung ke `MyLeaves` dan `MyPayslips` components (bukan Placeholder lagi).

---

## Verification Checklist ✅

- [x] Login sebagai employee (contoh: `ali@fintrack.com`)
- [x] Navigate ke **My Leaves** — past leaves load, boleh apply leave baru
- [x] Navigate ke **My Payslips** — payroll history load, PDF download berfungsi
- [x] Admin routes masih protected dengan `requireAdmin`
- [x] Unit tests untuk `LeaveService` dan `PayrollService` lulus