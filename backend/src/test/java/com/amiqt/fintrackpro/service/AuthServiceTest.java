package com.amiqt.fintrackpro.service;

import com.amiqt.fintrackpro.enums.Role;
import com.amiqt.fintrackpro.model.dto.request.LoginRequest;
import com.amiqt.fintrackpro.model.dto.response.AuthResponse;
import com.amiqt.fintrackpro.model.entity.User;
import com.amiqt.fintrackpro.repository.UserRepository;
import com.amiqt.fintrackpro.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Should login successfully and return token")
    void loginSuccessTest() {
        LoginRequest request = new LoginRequest("admin@fintrack.com", "password");
        User user = User.builder().email("admin@fintrack.com").role(Role.ROLE_ADMIN).build();
        
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("mock-jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.token());
        assertEquals("admin@fintrack.com", response.email());
        verify(authenticationManager).authenticate(any());
    }

    @Test
    @DisplayName("Should throw exception when login with invalid user")
    void loginUserNotFoundTest() {
        LoginRequest request = new LoginRequest("nonexistent@fintrack.com", "password");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authService.login(request));
    }
}
