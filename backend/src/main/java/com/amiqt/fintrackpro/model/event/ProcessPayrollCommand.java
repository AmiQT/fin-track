package com.amiqt.fintrackpro.model.event;

import java.util.UUID;

public record ProcessPayrollCommand(
    Long employeeId,
    Integer month,
    Integer year,
    UUID transactionId
) {}
