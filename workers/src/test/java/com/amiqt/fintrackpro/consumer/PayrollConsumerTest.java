package com.amiqt.fintrackpro.consumer;

import com.amiqt.fintrackpro.model.event.ProcessPayrollCommand;
import com.amiqt.fintrackpro.service.PayrollService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayrollConsumerTest {

    @Mock
    private PayrollService payrollService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private PayrollConsumer payrollConsumer;

    private ProcessPayrollCommand command;
    private String lockKey;

    @BeforeEach
    void setUp() {
        UUID transactionId = UUID.randomUUID();
        command = new ProcessPayrollCommand(1L, 5, 2026, transactionId);
        lockKey = "payroll:lock:" + transactionId + ":" + command.employeeId();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Should process payroll successfully when lock is acquired (SETNX)")
    void testConsumeSuccess() {
        // Mock setIfAbsent returning true (lock acquired)
        when(valueOperations.setIfAbsent(eq(lockKey), eq("PROCESSING"), any(Duration.class))).thenReturn(true);

        // Execute
        payrollConsumer.consume(command);

        // Verify calculations were called
        verify(payrollService, times(1)).calculatePayroll(eq(command.employeeId()), eq(command.month()), eq(command.year()));
        
        // Verify success lock key updated
        verify(valueOperations, times(1)).set(eq(lockKey), eq("SUCCESS"), any(Duration.class));
    }

    @Test
    @DisplayName("Should discard event immediately if lock is already held (Idempotence active)")
    void testConsumeDuplicateEvent() {
        // Mock setIfAbsent returning false (lock held by another active/completed thread)
        when(valueOperations.setIfAbsent(eq(lockKey), eq("PROCESSING"), any(Duration.class))).thenReturn(false);

        // Execute
        payrollConsumer.consume(command);

        // Verify payroll Service calculations were skipped
        verify(payrollService, never()).calculatePayroll(any(), any(), any());
        verify(valueOperations, never()).set(any(), any(), any(Duration.class));
    }

    @Test
    @DisplayName("Should delete lock key to allow retry if worker execution throws exception")
    void testConsumeFailureDeletesLock() {
        // Mock setIfAbsent returning true
        when(valueOperations.setIfAbsent(eq(lockKey), eq("PROCESSING"), any(Duration.class))).thenReturn(true);
        
        // Mock service throwing error
        doThrow(new RuntimeException("DB Connection Timeout")).when(payrollService).calculatePayroll(any(), any(), any());

        // Execute
        payrollConsumer.consume(command);

        // Verify Redis lock is deleted for fail-safe retries
        verify(redisTemplate, times(1)).delete(eq(lockKey));
    }
}
