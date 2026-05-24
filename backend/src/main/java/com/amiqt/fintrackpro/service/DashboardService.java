package com.amiqt.fintrackpro.service;

import com.amiqt.fintrackpro.enums.EmployeeStatus;
import com.amiqt.fintrackpro.enums.LeaveStatus;
import com.amiqt.fintrackpro.exception.ResourceNotFoundException;
import com.amiqt.fintrackpro.mapper.PayrollMapper;
import com.amiqt.fintrackpro.model.dto.response.DashboardResponse;
import com.amiqt.fintrackpro.model.dto.response.LeaveResponse;
import com.amiqt.fintrackpro.model.dto.response.PayrollResponse;
import com.amiqt.fintrackpro.model.dto.response.UserDashboardResponse;
import com.amiqt.fintrackpro.model.entity.Employee;
import com.amiqt.fintrackpro.model.entity.LeaveRequest;
import com.amiqt.fintrackpro.model.entity.Payroll;
import com.amiqt.fintrackpro.repository.EmployeeRepository;
import com.amiqt.fintrackpro.repository.LeaveRepository;
import com.amiqt.fintrackpro.repository.PayrollRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final PayrollRepository payrollRepository;
    private final LeaveRepository leaveRepository;
    private final PayrollMapper payrollMapper;

    @Cacheable(value = "dashboard-summary")
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardSummary() {
        log.debug("Building admin dashboard summary");
        LocalDate now = LocalDate.now();
        List<Employee> allEmployees = employeeRepository.findAll();

        // --- KPI: Headcount ---
        long totalEmployees = allEmployees.size();
        long activeEmployees = allEmployees.stream()
                .filter(e -> e.getStatus() == EmployeeStatus.ACTIVE)
                .count();

        // --- KPI: Pending Leaves ---
        List<LeaveRequest> allLeaves = leaveRepository.findAll();
        long pendingLeavesCount = allLeaves.stream()
                .filter(l -> l.getStatus() == LeaveStatus.PENDING)
                .count();

        // --- KPI: Leave Approval Rate ---
        long decidedLeaves = allLeaves.stream()
                .filter(l -> l.getStatus() == LeaveStatus.APPROVED || l.getStatus() == LeaveStatus.REJECTED)
                .count();
        long approvedLeaves = allLeaves.stream()
                .filter(l -> l.getStatus() == LeaveStatus.APPROVED)
                .count();
        double leaveApprovalRate = decidedLeaves == 0 ? 0.0
                : Math.round((approvedLeaves * 100.0 / decidedLeaves) * 10.0) / 10.0;

        // --- KPI: Current Month Payroll Cost ---
        List<Payroll> currentMonthPayrolls = payrollRepository.findByMonthAndYear(now.getMonthValue(), now.getYear());
        BigDecimal totalCost = currentMonthPayrolls.stream()
                .map(Payroll::getNetSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // --- KPI: Average Salary ---
        OptionalDouble avg = allEmployees.stream()
                .filter(e -> e.getBasicSalary() != null)
                .mapToDouble(e -> e.getBasicSalary().doubleValue())
                .average();
        BigDecimal avgSalary = avg.isPresent()
                ? BigDecimal.valueOf(avg.getAsDouble()).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // --- Chart: Department Headcount ---
        Map<String, Long> departmentHeadcount = allEmployees.stream()
                .filter(e -> e.getDepartment() != null)
                .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.counting()));

        // --- Chart: Leave Status Breakdown ---
        Map<String, Long> leaveStatusBreakdown = allLeaves.stream()
                .collect(Collectors.groupingBy(l -> l.getStatus().name(), Collectors.counting()));

        // --- Chart: Leave Type Breakdown ---
        Map<String, Long> leaveTypeBreakdown = allLeaves.stream()
                .filter(l -> l.getLeaveType() != null)
                .collect(Collectors.groupingBy(l -> l.getLeaveType().name(), Collectors.counting()));

        // --- Chart: Payroll Trend (last 6 months) ---
        List<DashboardResponse.PayrollTrend> payrollTrend = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            String monthName = date.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            BigDecimal cost = payrollRepository.findByMonthAndYear(date.getMonthValue(), date.getYear())
                    .stream().map(Payroll::getNetSalary).reduce(BigDecimal.ZERO, BigDecimal::add);
            payrollTrend.add(new DashboardResponse.PayrollTrend(monthName, cost));
        }

        // --- Chart: Salary Cost Trend — Gross vs Net (last 6 months) ---
        List<DashboardResponse.SalaryTrend> salaryCostTrend = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            String monthName = date.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            List<Payroll> monthPayrolls = payrollRepository.findByMonthAndYear(date.getMonthValue(), date.getYear());
            BigDecimal gross = monthPayrolls.stream().map(Payroll::getGrossSalary).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal net = monthPayrolls.stream().map(Payroll::getNetSalary).reduce(BigDecimal.ZERO, BigDecimal::add);
            salaryCostTrend.add(new DashboardResponse.SalaryTrend(monthName, gross, net));
        }

        // --- Table: Top 5 Earners (by latest net salary) ---
        List<DashboardResponse.TopEarner> topEarners = payrollRepository.findAll().stream()
                .sorted(Comparator.comparing(Payroll::getNetSalary).reversed())
                .filter(p -> p.getEmployee() != null && p.getEmployee().getDepartment() != null)
                .map(p -> new DashboardResponse.TopEarner(
                        p.getEmployee().getFullName(),
                        p.getEmployee().getDepartment(),
                        p.getNetSalary()))
                .distinct()
                .limit(5)
                .collect(Collectors.toList());

        log.debug("Dashboard summary built — {} total employees, {} active, {} pending leaves",
                totalEmployees, activeEmployees, pendingLeavesCount);
        return new DashboardResponse(
                totalEmployees,
                activeEmployees,
                totalCost,
                avgSalary,
                pendingLeavesCount,
                leaveApprovalRate,
                departmentHeadcount,
                leaveStatusBreakdown,
                leaveTypeBreakdown,
                payrollTrend,
                salaryCostTrend,
                topEarners
        );
    }

    @Transactional(readOnly = true)
    public UserDashboardResponse getMyDashboardSummary(String email) {
        log.debug("Building personal dashboard for: {}", email);
        Employee employee = employeeRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        List<Payroll> payrolls = payrollRepository.findByEmployeeIdOrderByYearDescMonthDesc(employee.getId());
        BigDecimal lastNetSalary = payrolls.isEmpty() ? BigDecimal.ZERO : payrolls.get(0).getNetSalary();

        List<LeaveRequest> leaves = leaveRepository.findByEmployeeId(employee.getId());
        long pendingLeavesCount = leaves.stream().filter(l -> l.getStatus() == LeaveStatus.PENDING).count();
        int totalLeavesTaken = leaves.stream()
                .filter(l -> l.getStatus() == LeaveStatus.APPROVED)
                .mapToInt(LeaveRequest::getTotalDays)
                .sum();

        List<PayrollResponse> recentPayrolls = payrolls.stream()
                .limit(5)
                .map(payrollMapper::toResponse)
                .collect(Collectors.toList());

        List<LeaveResponse> recentLeaves = leaves.stream()
                .sorted(Comparator.comparing(LeaveRequest::getAppliedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(this::mapToLeaveResponse)
                .collect(Collectors.toList());

        return new UserDashboardResponse(
                employee.getFullName(),
                lastNetSalary,
                totalLeavesTaken,
                pendingLeavesCount,
                recentPayrolls,
                recentLeaves
        );
    }

    private LeaveResponse mapToLeaveResponse(LeaveRequest l) {
        return new LeaveResponse(
                l.getId(),
                l.getEmployee().getId(),
                l.getEmployee().getFullName(),
                l.getLeaveType(),
                l.getStartDate(),
                l.getEndDate(),
                l.getTotalDays(),
                l.getReason(),
                l.getStatus(),
                l.getRejectionReason(),
                l.getAppliedAt()
        );
    }
}