package com.amiqt.fintrackpro.model.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardResponse(
        long totalEmployees,
        long activeEmployees,
        BigDecimal totalPayrollCost,
        BigDecimal avgSalary,
        long pendingLeaves,
        double leaveApprovalRate,
        Map<String, Long> departmentHeadcount,
        Map<String, Long> leaveStatusBreakdown,
        Map<String, Long> leaveTypeBreakdown,
        List<PayrollTrend> payrollTrend,
        List<SalaryTrend> salaryCostTrend,
        List<TopEarner> topEarners
) implements Serializable {

    public record PayrollTrend(String name, BigDecimal cost) implements Serializable {}

    public record SalaryTrend(String name, BigDecimal gross, BigDecimal net) implements Serializable {}

    public record TopEarner(String name, String department, BigDecimal netSalary) implements Serializable {}
}