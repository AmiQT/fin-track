package com.amiqt.fintrackpro.service;

import com.amiqt.fintrackpro.model.entity.AuditLog;
import com.amiqt.fintrackpro.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void log(String actorEmail, String action, String entityType, String entityId, String detail, String ipAddress) {
        try {
            AuditLog entry = AuditLog.builder()
                    .actorEmail(actorEmail)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .detail(detail)
                    .ipAddress(ipAddress)
                    .build();
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to save audit log for actor={} action={}: {}", actorEmail, action, e.getMessage());
        }
    }

    public Page<AuditLog> getByActor(String actorEmail, Pageable pageable) {
        return auditLogRepository.findByActorEmailOrderByCreatedAtDesc(actorEmail, pageable);
    }

    public Page<AuditLog> getByEntity(String entityType, String entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId, pageable);
    }
}
