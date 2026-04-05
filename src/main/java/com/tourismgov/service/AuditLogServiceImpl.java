package com.tourismgov.service;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.tourismgov.dto.AuditLogResponse;
import com.tourismgov.model.AuditLog;
import com.tourismgov.repository.AuditLogRepository;
import com.tourismgov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW) 
    public void logAction(Long userId, String action, String resource, String status) {
        if (userId == null) return; 

        userRepository.findById(userId).ifPresentOrElse(user -> {
            AuditLog auditLog = AuditLog.builder()
                .user(user)
                .action(action)
                .resource(resource)
                .status(status)
                .build();
            auditLogRepository.save(auditLog);
        }, () -> log.error("Audit fail: User ID {} not found for action {}", userId, action));
    }

    @Override
    @Transactional 
    public void logActionInCurrentTransaction(Long userId, String action, String resource, String status) {
        if (userId == null) return; 

        userRepository.findById(userId).ifPresentOrElse(user -> {
            AuditLog auditLog = AuditLog.builder()
                .user(user)
                .action(action)
                .resource(resource)
                .status(status)
                .build();
            auditLogRepository.save(auditLog);
        }, () -> log.error("Audit fail: User ID {} not found for action {}", userId, action));
    }
    

    @Override
    public Page<AuditLogResponse> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public Page<AuditLogResponse> getLogsByUserId(Long userId, Pageable pageable) {
        return auditLogRepository.findByUser_UserId(userId, pageable).map(this::toResponse);
    }

    @Override
    public Page<AuditLogResponse> getLogsByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return auditLogRepository.findByTimestampBetween(start, end, pageable).map(this::toResponse);
    }

    @Override
    public Page<AuditLogResponse> getLogsByAction(String action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable).map(this::toResponse);
    }

    private AuditLogResponse toResponse(AuditLog log) {
        AuditLogResponse dto = new AuditLogResponse();
        dto.setAuditId(log.getAuditId());
        if (log.getUser() != null) {
            dto.setUserId(log.getUser().getUserId());
        }
        dto.setAction(log.getAction());
        dto.setResource(log.getResource());
        dto.setStatus(log.getStatus());
        dto.setTimestamp(log.getTimestamp());
        return dto;
    }
}