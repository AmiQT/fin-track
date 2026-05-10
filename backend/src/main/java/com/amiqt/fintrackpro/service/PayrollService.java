package com.amiqt.fintrackpro.service;

import com.amiqt.fintrackpro.enums.LeaveType;
import com.amiqt.fintrackpro.enums.LeaveStatus;
import com.amiqt.fintrackpro.exception.PayrollAlreadyProcessedException;
import com.amiqt.fintrackpro.exception.ResourceNotFoundException;
import com.amiqt.fintrackpro.mapper.PayrollMapper;
import com.amiqt.fintrackpro.model.dto.request.PayrollRequest;
import com.amiqt.fintrackpro.model.dto.response.PayrollResponse;
import com.amiqt.fintrackpro.model.entity.Employee;
import com.amiqt.fintrackpro.model.entity.Payroll;
import com.amiqt.fintrackpro.model.entity.LeaveRequest;
import com.amiqt.fintrackpro.repository.EmployeeRepository;
import com.amiqt.fintrackpro.repository.PayrollRepository;
import com.amiqt.fintrackpro.repository.LeaveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveRepository leaveRepository;
    private final PayrollMapper payrollMapper;

    @Transactional
    @CacheEvict(value = "dashboard-summary", allEntries = true)
    public List<PayrollResponse> processPayroll(PayrollRequest request) {
        List<Employee> activeEmployees = employeeRepository.findAll().stream()
                .filter(e -> e.getStatus().name().equals("ACTIVE"))
                .toList();

        return activeEmployees.stream()
                .map(employee -> calculatePayroll(employee, request.month(), request.year()))
                .map(payrollMapper::toResponse)
                .collect(Collectors.toList());
    }

    public Payroll calculatePayroll(Employee employee, Integer month, Integer year) {
        // Check if already processed
        payrollRepository.findByEmployeeIdAndMonthAndYear(employee.getId(), month, year)
                .ifPresent(p -> {
                    throw new PayrollAlreadyProcessedException(
                            "Payroll already processed for employee " + employee.getEmployeeCode() + " for " + month + "/" + year
                    );
                });

        BigDecimal basic = employee.getBasicSalary();
        BigDecimal housing = employee.getHousingAllowance();
        BigDecimal transport = employee.getTransportAllowance();

        // 1. Gross Salary
        BigDecimal gross = basic.add(housing).add(transport);

        // 2. Unpaid Leave Deduction
        long unpaidDays = calculateUnpaidLeaveDays(employee.getId(), month, year);
        int workingDays = calculateWorkingDays(month, year);
        BigDecimal unpaidDeduction = BigDecimal.ZERO;
        if (unpaidDays > 0) {
            unpaidDeduction = basic.divide(BigDecimal.valueOf(workingDays), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(unpaidDays));
        }

        // 3. EPF Calculation
        BigDecimal epfEmployee = basic.multiply(new BigDecimal("0.11")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal epfEmployer = basic.multiply(new BigDecimal("0.13")).setScale(2, RoundingMode.HALF_UP);

        // 4. SOCSO Calculation (Simplified tier)
        BigDecimal socsoEmployee = calculateSocsoEmployee(basic);
        BigDecimal socsoEmployer = calculateSocsoEmployer(basic);

        // 5. Income Tax (PCB)
        BigDecimal adjustedGross = gross.subtract(unpaidDeduction);
        BigDecimal incomeTax = BigDecimal.ZERO;
        if (adjustedGross.compareTo(new BigDecimal("3000")) > 0) {
            incomeTax = adjustedGross.subtract(new BigDecimal("3000"))
                    .multiply(new BigDecimal("0.01"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // 6. Total Deductions
        BigDecimal totalDeductions = epfEmployee.add(socsoEmployee).add(incomeTax).add(unpaidDeduction);

        // 7. Net Salary
        BigDecimal netSalary = gross.subtract(totalDeductions);

        Payroll payroll = Payroll.builder()
                .employee(employee)
                .month(month)
                .year(year)
                .basicSalary(basic)
                .housingAllowance(housing)
                .transportAllowance(transport)
                .grossSalary(gross)
                .epfEmployee(epfEmployee)
                .epfEmployer(epfEmployer)
                .socsoEmployee(socsoEmployee)
                .socsoEmployer(socsoEmployer)
                .incomeTax(incomeTax)
                .unpaidLeaveDeduction(unpaidDeduction)
                .totalDeductions(totalDeductions)
                .netSalary(netSalary)
                .build();

        return payrollRepository.save(payroll);
    }

    private int calculateWorkingDays(int month, int year) {
        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();
        int workingDays = 0;
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = yearMonth.atDay(day);
            if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                workingDays++;
            }
        }
        return workingDays;
    }

    private long calculateUnpaidLeaveDays(Long employeeId, int month, int year) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = YearMonth.of(year, month).atEndOfMonth();

        return leaveRepository.findByEmployeeIdAndStatus(employeeId, LeaveStatus.APPROVED)
                .stream()
                .filter(l -> l.getLeaveType() == LeaveType.UNPAID)
                .filter(l -> !l.getStartDate().isAfter(endOfMonth) && !l.getEndDate().isBefore(startOfMonth))
                .mapToLong(LeaveRequest::getTotalDays) // Simplified: assuming entire leave falls within month
                .sum();
    }

    private BigDecimal calculateSocsoEmployee(BigDecimal salary) {
        // Mock SOCSO tier: roughly 0.5%
        return salary.multiply(new BigDecimal("0.005")).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateSocsoEmployer(BigDecimal salary) {
        // Mock SOCSO employer tier: roughly 1.75%
        return salary.multiply(new BigDecimal("0.0175")).setScale(2, RoundingMode.HALF_UP);
    }

    public List<PayrollResponse> getPayrollHistory(Long employeeId) {
        return payrollRepository.findByEmployeeIdOrderByYearDescMonthDesc(employeeId).stream()
                .map(payrollMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<PayrollResponse> getMyPayroll(String email) {
        Employee employee = employeeRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found for email: " + email));
        
        return getPayrollHistory(employee.getId());
    }

    @Cacheable(value = "payroll-summary", key = "#month + '-' + #year")
    public List<PayrollResponse> getMonthlySummary(Integer month, Integer year) {
        return payrollRepository.findByMonthAndYear(month, year).stream()
                .map(payrollMapper::toResponse)
                .collect(Collectors.toList());
    }

    public String exportPayrollToCsv(Integer month, Integer year) {
        StringBuilder csv = new StringBuilder();
        csv.append("Employee Name,Month,Year,Basic Salary,Housing,Transport,Gross Salary,EPF,SOCSO,Tax,Net Salary\n");

        payrollRepository.findByMonthAndYear(month, year).forEach(p -> {
            csv.append(String.format("%s,%d,%d,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f\n",
                    p.getEmployee().getFullName(),
                    p.getMonth(),
                    p.getYear(),
                    p.getBasicSalary(),
                    p.getHousingAllowance(),
                    p.getTransportAllowance(),
                    p.getGrossSalary(),
                    p.getEpfEmployee(),
                    p.getSocsoEmployee(),
                    p.getIncomeTax(),
                    p.getNetSalary()));
        });

        return csv.toString();
    }
}
