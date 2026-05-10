package com.amiqt.fintrackpro.model.dto.response;

import com.amiqt.fintrackpro.enums.LeaveStatus;
import com.amiqt.fintrackpro.enums.LeaveType;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record LeaveResponse(
        Long id,
        Long employeeId,
        String employeeName,
        LeaveType leaveType,
        LocalDate startDate,
        LocalDate endDate,
        Integer totalDays,
        String reason,
        LeaveStatus status,
        String rejectionReason,
        LocalDateTime appliedAt
) {}
