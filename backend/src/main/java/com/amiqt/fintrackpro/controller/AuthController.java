package com.amiqt.fintrackpro.controller;

import com.amiqt.fintrackpro.model.dto.request.ChangePasswordRequest;
import com.amiqt.fintrackpro.model.dto.request.LoginRequest;
import com.amiqt.fintrackpro.model.dto.response.AuthResponse;
import com.amiqt.fintrackpro.model.entity.User;
import com.amiqt.fintrackpro.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(user.getEmail(), request);
        return ResponseEntity.ok().build();
    }
}
