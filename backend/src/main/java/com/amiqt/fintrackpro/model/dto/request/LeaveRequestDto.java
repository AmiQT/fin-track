package com.amiqt.fintrackpro.model.dto.request;

import com.amiqt.fintrackpro.enums.LeaveType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record LeaveRequestDto(
        Long employeeId,

        @NotNull(message = "Leave type is required")
        LeaveType leaveType,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate,

        String reason
) {}
