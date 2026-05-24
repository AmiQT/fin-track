package com.amiqt.fintrackpro.model.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PayrollResponse(
        Long id,
        Long employeeId,
        String employeeName,
        Integer month,
        Integer year,
        BigDecimal basicSalary,
        BigDecimal grossSalary,
        BigDecimal epfEmployee,
        BigDecimal epfEmployer,
        BigDecimal socsoEmployee,
        BigDecimal socsoEmployer,
        BigDecimal eisEmployee,
        BigDecimal eisEmployer,
        BigDecimal incomeTax,
        BigDecimal unpaidLeaveDeduction,
        BigDecimal totalDeductions,
        BigDecimal netSalary,
        LocalDateTime processedAt
) {}
