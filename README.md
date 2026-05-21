# 🏦 FinTrack Pro: Next-Gen Payroll & HR Management

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![React](https://img.shields.io/badge/React-18-blue.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)

**FinTrack Pro** is a premium, enterprise-grade Payroll and HR Management System designed to streamline employee management, salary processing, and leave tracking. Built with a robust **Spring Boot** backend and a stunning **React** frontend, it offers a seamless experience for HR administrators and employees alike.

---

## ✨ Key Features

### 👩‍💼 Employee Management
- **Centralized Directory**: Full CRUD operations for employee records.
- **Role-Based Access**: Specialized views for Admins, HR, and Employees.
- **Status Tracking**: Manage active and inactive personnel.

### 💰 Automated Payroll Engine
- **Precise Calculations**: Automated logic for Basic Salary, EPF (11%/13%), SOCSO, and PCB Tax.
- **Unpaid Leave Awareness**: Automatic salary deductions based on approved unpaid leaves and working days.
- **Interactive Analytics**: Monthly payroll trends and headcount distributions using Recharts.

### 📅 Leave Management Workflow
- **Application Portal**: Employees can apply for various leave types (Annual, Medical, Unpaid).
- **Balance Tracking**: Real-time deduction and visual tracking of Annual and Sick leave entitlements.
- **Approval System**: Streamlined workflow for HR/Managers to review, approve, or reject requests. Rejected requests automatically refund leave balances.

### 📄 Digital Payslips
- **PDF Generation**: Generate and download professional payslips powered by iText 7.
- **Secure Access**: Employees can only access their personal payment history.

---

## 🛠️ Technology Stack

### Backend
- **Core**: Java 21, Spring Boot 3.2
- **Security**: Spring Security, JWT (Stateless Auth)
- **Database**: PostgreSQL 16 (Relational data)
- **Cache**: Redis 7 (Dashboard & API performance)
- **PDF Engine**: iText 7
- **Testing**: JUnit 5, Mockito

### Frontend
- **Framework**: React 18 (Vite)
- **Styling**: Tailwind CSS (Premium Glassmorphism UI)
- **Charts**: Recharts
- **Icons**: Lucide React

---

## 🚀 Getting Started

### Prerequisites
- Docker & Docker Desktop
- JDK 21 & Maven (for manual build)
- Node.js LTS (for manual build)

### Quick Start with Docker
The easiest way to run the entire stack is using Docker Compose:

```bash
docker-compose up --build -d
```

Access the application:
- **Frontend**: [http://localhost:3000](http://localhost:3000)
- **Backend API**: [http://localhost:8080](http://localhost:8080)
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

*(Note: See `REQUIREMENTS.txt` for detailed manual setup instructions.)*

---

## 🧪 Testing & Validation

The backend is fortified with a comprehensive unit testing suite ensuring business logic and security components are robust, reliable, and production-ready.

```bash
cd backend
mvn clean test
```

**Fully Validated Services:** `AuthService`, `EmployeeService`, `LeaveService`, and `PayrollService`.

---

## 🛡️ Security & Stabilization

FinTrack Pro uses **JWT-based stateless authentication**.
Recent stabilizations include:
- Resolved login redirect loops for staff users.
- Fixed staff password hash corruption.
- Seamlessly aligned backend and frontend authentication logic for flawless access.

---

## 👤 Employee Self-Service Features

Employees can access their own data without admin privileges:

| Feature | Endpoint | Description |
|---|---|---|
| My Leaves | `GET /api/leaves/my` | View all personal leave requests |
| Apply Leave | `POST /api/leaves` | Apply for Annual, Sick, or Unpaid leave |
| My Payslips | `GET /api/payroll/my` | View personal payroll history |
| Download PDF | `GET /api/payslip/{id}/pdf` | Download payslip as PDF |
| My Profile | `/my-profile` | View and update personal profile |

Employee identity is resolved **securely via their authenticated JWT email** — no need to send `employeeId` manually.

---

## ✅ Project Status

> **COMPLETED** — All planned features have been implemented and tested.

| Module | Status |
|---|---|
| Authentication (JWT) | Done |
| Employee Management (Admin) | Done |
| Payroll Processing (Admin) | Done |
| Leave Management (Admin) | Done |
| My Leaves (Employee) | Done |
| My Payslips + PDF (Employee) | Done |
| My Profile (Employee) | Done |
| Unit Tests (Auth, Employee, Leave, Payroll) | Done |
| Docker Compose Full Stack | Done |

---

## 📄 License
This project is licensed under the MIT License - see the LICENSE file for details.

---
*Built with ❤️ for Modern HR Teams.*
