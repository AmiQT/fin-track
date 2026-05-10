package com.amiqt.fintrackpro.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PayrollRequest(
        @NotNull(message = "Month is required")
        @Min(1) @Max(12)
        Integer month,

        @NotNull(message = "Year is required")
        @Min(2000)
        Integer year
) {}
