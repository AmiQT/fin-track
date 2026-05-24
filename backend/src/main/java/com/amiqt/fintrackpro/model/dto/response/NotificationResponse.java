package com.amiqt.fintrackpro.model.dto.response;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String title,
        String message,
        boolean isRead,
        LocalDateTime createdAt
) {}
