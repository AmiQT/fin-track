package com.amiqt.fintrackpro.model.dto.response;

public record LeaveBalanceResponse(
        Integer annualEntitlement,
        Integer annualTaken,
        Integer annualBalance,
        Integer sickEntitlement,
        Integer sickTaken,
        Integer sickBalance
) {}
