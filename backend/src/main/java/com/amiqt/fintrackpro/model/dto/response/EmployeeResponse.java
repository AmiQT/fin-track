package com.amiqt.fintrackpro.model.dto.response;

import com.amiqt.fintrackpro.enums.EmployeeStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeResponse(
        Long id,
        String employeeCode,
        String fullName,
        String email,
        String phone,
        String department,
        String position,
        BigDecimal basicSalary,
        BigDecimal housingAllowance,
        BigDecimal transportAllowance,
        LocalDate joinDate,
        EmployeeStatus status
) {}
