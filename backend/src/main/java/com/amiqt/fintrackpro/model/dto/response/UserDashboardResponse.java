package com.amiqt.fintrackpro.model.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public record UserDashboardResponse(
        String fullName,
        BigDecimal lastNetSalary,
        int totalLeavesTaken,
        long pendingLeaves,
        List<PayrollResponse> recentPayrolls,
        List<LeaveResponse> recentLeaves
) implements Serializable {}