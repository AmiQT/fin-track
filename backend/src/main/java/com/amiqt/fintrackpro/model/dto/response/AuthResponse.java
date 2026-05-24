package com.amiqt.fintrackpro.model.dto.response;

import com.amiqt.fintrackpro.enums.Role;

public record AuthResponse(
        String token,
        String refreshToken,
        String email,
        Role role
) {}
