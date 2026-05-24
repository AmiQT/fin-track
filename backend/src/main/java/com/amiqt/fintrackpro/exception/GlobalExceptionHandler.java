package com.amiqt.fintrackpro.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, Object> buildBody(HttpStatus status, String message, String requestId) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", message);
        body.put("status", status.value());
        body.put("requestId", requestId);
        return body;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();
        log.warn("[{}] Resource not found: {} — {}", requestId, req.getRequestURI(), ex.getMessage());
        return new ResponseEntity<>(buildBody(HttpStatus.NOT_FOUND, ex.getMessage(), requestId), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();
        log.warn("[{}] Validation failed: {}", requestId, req.getRequestURI());
        Map<String, Object> body = buildBody(HttpStatus.BAD_REQUEST, "Validation failed", requestId);
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage()));
        body.put("errors", errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();
        log.warn("[{}] Bad credentials attempt at: {}", requestId, req.getRequestURI());
        return new ResponseEntity<>(buildBody(HttpStatus.UNAUTHORIZED, "Email atau password salah", requestId), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UsernameNotFoundException ex, HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();
        log.warn("[{}] User not found: {}", requestId, ex.getMessage());
        return new ResponseEntity<>(buildBody(HttpStatus.NOT_FOUND, ex.getMessage(), requestId), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();
        log.warn("[{}] Illegal argument at {}: {}", requestId, req.getRequestURI(), ex.getMessage());
        return new ResponseEntity<>(buildBody(HttpStatus.BAD_REQUEST, ex.getMessage(), requestId), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PayrollAlreadyProcessedException.class)
    public ResponseEntity<Map<String, Object>> handlePayrollAlreadyProcessed(PayrollAlreadyProcessedException ex, HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();
        log.warn("[{}] Payroll duplicate at {}: {}", requestId, req.getRequestURI(), ex.getMessage());
        return new ResponseEntity<>(buildBody(HttpStatus.CONFLICT, ex.getMessage(), requestId), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobal(Exception ex, HttpServletRequest req) {
        String requestId = UUID.randomUUID().toString();
        log.error("[{}] Unhandled exception at {}: {}", requestId, req.getRequestURI(), ex.getMessage(), ex);
        return new ResponseEntity<>(buildBody(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", requestId), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
