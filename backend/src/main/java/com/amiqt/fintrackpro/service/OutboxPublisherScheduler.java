package com.amiqt.fintrackpro.service;

import com.amiqt.fintrackpro.model.entity.OutboxMessage;
import com.amiqt.fintrackpro.model.event.ProcessPayrollCommand;
import com.amiqt.fintrackpro.repository.OutboxMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class OutboxPublisherScheduler {

    private final OutboxMessageRepository outboxMessageRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishOutboxMessages() {
        List<OutboxMessage> pendingMessages = outboxMessageRepository.findPendingMessagesForWrite(PageRequest.of(0, 50));
        
        if (pendingMessages.isEmpty()) {
            return;
        }

        log.debug("Found {} pending outbox messages to publish", pendingMessages.size());

        for (OutboxMessage message : pendingMessages) {
            try {
                ProcessPayrollCommand command = objectMapper.readValue(message.getPayload(), ProcessPayrollCommand.class);
                
                CompletableFuture<?> future = kafkaTemplate.send(message.getTopic(), command.employeeId().toString(), command);
                
                future.whenComplete((result, ex) -> {
                    if (ex == null) {
                        handleSuccess(message.getId());
                    } else {
                        handleFailure(message.getId(), ex.getMessage());
                    }
                });
                
            } catch (Exception e) {
                log.error("Failed to process outbox message {}: {}", message.getId(), e.getMessage());
                handleFailure(message.getId(), e.getMessage());
            }
        }
    }

    @Transactional
    public void handleSuccess(UUID messageId) {
        outboxMessageRepository.deleteById(messageId);
        log.debug("Successfully published outbox message: {} and evicted record", messageId);
    }

    @Transactional
    public void handleFailure(UUID messageId, String errorMessage) {
        outboxMessageRepository.findById(messageId).ifPresent(message -> {
            int retries = message.getRetryCount() + 1;
            message.setRetryCount(retries);
            message.setUpdatedAt(LocalDateTime.now());
            
            if (retries >= 3) {
                message.setStatus("FAILED");
                log.error("Outbox message {} failed permanently after {} retries. Error: {}", messageId, retries, errorMessage);
            } else {
                log.warn("Retrying outbox message {} (Attempt {}/3). Error: {}", messageId, retries, errorMessage);
            }
            outboxMessageRepository.save(message);
        });
    }
}
