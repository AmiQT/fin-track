package com.amiqt.fintrackpro.service;

import com.amiqt.fintrackpro.model.dto.request.ChangePasswordRequest;
import com.amiqt.fintrackpro.model.dto.request.LoginRequest;
import com.amiqt.fintrackpro.model.dto.response.AuthResponse;
import com.amiqt.fintrackpro.model.entity.User;
import com.amiqt.fintrackpro.repository.UserRepository;
import com.amiqt.fintrackpro.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        log.info("User logged in: {} (role: {})", user.getEmail(), user.getRole());
        return new AuthResponse(token, refreshToken, user.getEmail(), user.getRole());
    }

    public AuthResponse refresh(String refreshToken) {
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        if (jwtService.isTokenBlacklisted(refreshToken)) {
            throw new IllegalArgumentException("Refresh token has been revoked");
        }

        String email = jwtService.extractUsername(refreshToken);
        User user = (User) userDetailsService.loadUserByUsername(email);

        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        // Rotate: blacklist the used refresh token
        jwtService.blacklistToken(refreshToken);

        log.info("Token refreshed for: {}", email);
        return new AuthResponse(newAccessToken, newRefreshToken, user.getEmail(), user.getRole());
    }

    public void logout(String accessToken, String refreshToken) {
        jwtService.blacklistToken(accessToken);
        if (refreshToken != null && !refreshToken.isBlank()) {
            jwtService.blacklistToken(refreshToken);
        }
        String email = jwtService.extractUsername(accessToken);
        log.info("User logged out: {}", email);
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            log.warn("Failed password change attempt for: {}", email);
            throw new IllegalArgumentException("Incorrect old password");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("Password changed for: {}", email);
    }
}
