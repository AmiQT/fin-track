package com.amiqt.fintrackpro.service;

import com.amiqt.fintrackpro.enums.LeaveStatus;
import com.amiqt.fintrackpro.enums.LeaveType;
import com.amiqt.fintrackpro.model.entity.Employee;
import com.amiqt.fintrackpro.model.entity.LeaveRequest;
import com.amiqt.fintrackpro.model.entity.Payroll;
import com.amiqt.fintrackpro.repository.EmployeeRepository;
import com.amiqt.fintrackpro.repository.LeaveRepository;
import com.amiqt.fintrackpro.repository.PayrollRepository;
import com.amiqt.fintrackpro.service.payroll.EisCalculator;
import com.amiqt.fintrackpro.service.payroll.EpfCalculator;
import com.amiqt.fintrackpro.service.payroll.PcbCalculator;
import com.amiqt.fintrackpro.service.payroll.SocsoCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveRepository leaveRepository;

    private final EpfCalculator epfCalculator;
    private final SocsoCalculator socsoCalculator;
    private final EisCalculator eisCalculator;
    private final PcbCalculator pcbCalculator;

    @Transactional
    public void calculatePayroll(Long employeeId, Integer month, Integer year) {
        log.info("Worker calculating payroll for employee id: {} for {}/{}", employeeId, month, year);
        
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found for id: " + employeeId));

        // Skip if already processed to ensure database constraint is not breached
        if (payrollRepository.findByEmployeeIdAndMonthAndYear(employeeId, month, year).isPresent()) {
            log.warn("Payroll already exists in database for employee id: {} for {}/{}", employeeId, month, year);
            return;
        }

        BigDecimal basic = employee.getBasicSalary() != null ? employee.getBasicSalary() : BigDecimal.ZERO;
        BigDecimal housing = employee.getHousingAllowance() != null ? employee.getHousingAllowance() : BigDecimal.ZERO;
        BigDecimal transport = employee.getTransportAllowance() != null ? employee.getTransportAllowance() : BigDecimal.ZERO;

        BigDecimal gross = basic.add(housing).add(transport);

        long unpaidDays = calculateUnpaidLeaveDays(employeeId, month, year);
        int workingDays = calculateWorkingDays(month, year);
        BigDecimal unpaidDeduction = BigDecimal.ZERO;
        if (unpaidDays > 0) {
            unpaidDeduction = basic.divide(BigDecimal.valueOf(workingDays), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(unpaidDays));
        }

        BigDecimal adjustedGross = gross.subtract(unpaidDeduction);
        
        // Calculations using Strategy
        BigDecimal epfEmployee = epfCalculator.calculate(basic, employee);
        BigDecimal epfEmployer = epfCalculator.calculateEmployerContribution(basic);
        BigDecimal socsoEmployee = socsoCalculator.calculate(basic, employee);
        BigDecimal socsoEmployer = socsoCalculator.calculateEmployerContribution(basic);
        BigDecimal eisEmployee = eisCalculator.calculate(basic, employee);
        BigDecimal eisEmployer = eisCalculator.calculateEmployerContribution(basic);
        BigDecimal incomeTax = pcbCalculator.calculate(adjustedGross, employee);

        BigDecimal totalDeductions = epfEmployee.add(socsoEmployee).add(eisEmployee).add(incomeTax).add(unpaidDeduction);
        BigDecimal netSalary = adjustedGross.subtract(epfEmployee).subtract(socsoEmployee).subtract(eisEmployee).subtract(incomeTax);

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
                .eisEmployee(eisEmployee)
                .eisEmployer(eisEmployer)
                .incomeTax(incomeTax)
                .unpaidLeaveDeduction(unpaidDeduction)
                .totalDeductions(totalDeductions)
                .netSalary(netSalary)
                .build();

        payrollRepository.save(payroll);
        log.info("Worker successfully saved payroll for employee id: {} — net salary: RM {}", employeeId, netSalary);
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
                .mapToLong(LeaveRequest::getTotalDays)
                .sum();
    }
}
