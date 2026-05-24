package com.amiqt.fintrackpro.model.dto.request;

import com.amiqt.fintrackpro.enums.MaritalStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeRequest(
        @NotBlank(message = "Employee code is required")
        String employeeCode,

        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        String phone,

        @NotBlank(message = "Department is required")
        String department,

        @NotBlank(message = "Position is required")
        String position,

        @NotNull(message = "Basic salary is required")
        @Positive(message = "Salary must be positive")
        BigDecimal basicSalary,

        BigDecimal housingAllowance,
        BigDecimal transportAllowance,

        @NotNull(message = "Join date is required")
        LocalDate joinDate,

        MaritalStatus maritalStatus,
        Integer numberOfChildren,
        BigDecimal epfRate,
        
        String password // For initial user creation
) {}
