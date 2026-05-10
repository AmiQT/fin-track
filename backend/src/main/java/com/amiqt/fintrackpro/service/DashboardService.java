package com.amiqt.fintrackpro.service;

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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
        long totalEmployees = employeeRepository.count();
        long pendingLeavesCount = leaveRepository.findByStatus(LeaveStatus.PENDING).size();

        // Calculate total payroll for current month
        LocalDate now = LocalDate.now();
        List<Payroll> currentMonthPayrolls = payrollRepository.findByMonthAndYear(now.getMonthValue(), now.getYear());
        
        BigDecimal totalCost = currentMonthPayrolls.stream()
                .map(Payroll::getNetSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Headcount per department
        Map<String, Long> departmentHeadcount = employeeRepository.findAll().stream()
                .collect(Collectors.groupingBy(e -> e.getDepartment(), Collectors.counting()));

        // Payroll Trend (Last 6 Months)
        List<DashboardResponse.PayrollTrend> trend = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            int m = date.getMonthValue();
            int y = date.getYear();
            String monthName = date.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            
            BigDecimal cost = payrollRepository.findByMonthAndYear(m, y).stream()
                    .map(Payroll::getNetSalary)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            trend.add(new DashboardResponse.PayrollTrend(monthName, cost));
        }

        return new DashboardResponse(totalEmployees, totalCost, pendingLeavesCount, departmentHeadcount, trend);
    }

    @Transactional(readOnly = true)
    public UserDashboardResponse getMyDashboardSummary(String email) {
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