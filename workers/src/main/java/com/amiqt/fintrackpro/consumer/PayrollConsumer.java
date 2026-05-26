package com.amiqt.fintrackpro.consumer;

import com.amiqt.fintrackpro.model.event.ProcessPayrollCommand;
import com.amiqt.fintrackpro.service.PayrollService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayrollConsumer {

    private final PayrollService payrollService;
    private final StringRedisTemplate redisTemplate;

    @KafkaListener(
            topics = "payroll-commands",
            groupId = "fintrack-worker-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ProcessPayrollCommand command) {
        // Retrieve thread name to verify we are running on Java 21 Virtual Threads!
        String threadName = Thread.currentThread().toString();
        log.info("Consumer received ProcessPayrollCommand for employee id: {} on thread: {}", command.employeeId(), threadName);

        String lockKey = "payroll:lock:" + command.transactionId() + ":" + command.employeeId();
        
        // Idempotency check: Set If Absent (SETNX) with 5 minutes TTL
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "PROCESSING", Duration.ofMinutes(5));
        
        if (Boolean.FALSE.equals(lockAcquired)) {
            log.warn("Idempotency guard active! Event discarded to prevent double calculation: Employee {} for Tx: {}", 
                    command.employeeId(), command.transactionId());
            return;
        }

        try {
            // Process the payroll calculations
            payrollService.calculatePayroll(command.employeeId(), command.month(), command.year());
            
            // Mark transaction as successful in Redis with a 10-minute expiry
            redisTemplate.opsForValue().set(lockKey, "SUCCESS", Duration.ofMinutes(10));
            log.info("Successfully completed processing for employee id: {} (TxID: {})", command.employeeId(), command.transactionId());
        } catch (Exception e) {
            log.error("Failed to process payroll for employee id: {} in transaction {}: {}", 
                    command.employeeId(), command.transactionId(), e.getMessage(), e);
            // Evict lock to allow retry if processing failed
            redisTemplate.delete(lockKey);
        }
    }
}
