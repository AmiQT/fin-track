package com.amiqt.fintrackpro.model.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardResponse(
        long totalEmployees,
        BigDecimal totalPayrollCost,
        long pendingLeaves,
        Map<String, Long> departmentHeadcount,
        List<PayrollTrend> payrollTrend
) implements Serializable {
    public record PayrollTrend(String month, BigDecimal cost) implements Serializable {}
}