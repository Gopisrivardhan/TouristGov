package com.tourismgov.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tourismgov.dto.AuditLogRequest;
import com.tourismgov.dto.AuditLogResponse;
import com.tourismgov.model.AuditLog;
import com.tourismgov.model.User;
import com.tourismgov.repository.AuditLogRepository;
import com.tourismgov.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }


    // Private methods bypass the Proxy warning and keep code DRY
    private AuditLog buildEntity(AuditLogRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getUserId()));

        AuditLog log = new AuditLog();
        log.setUser(user); 
        log.setAction(request.getAction());
        log.setResource(request.getResource());
        log.setTimestamp(request.getTimestamp() != null ? request.getTimestamp() : LocalDateTime.now());
        
        return log;
    }

    @Override
    @Transactional
    public AuditLog create(AuditLogRequest request) {
        AuditLog log = buildEntity(request);
        return auditLogRepository.save(log);
    }

    @Override
    @Transactional
    public List<AuditLog> createAll(List<AuditLogRequest> requests) {
        List<AuditLog> toSave = new ArrayList<>();
        for (AuditLogRequest r : requests) {
            // Uses the helper method instead of calling create(r)
            toSave.add(buildEntity(r)); 
        }
        // Saves them all to the database efficiently in one batch!
        return auditLogRepository.saveAll(toSave); 
    }

    @Override
    public Page<AuditLogResponse> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public void recordAction(Long userId, String action, String resource) {
        if (userId == null) return; 
        
        userRepository.findById(userId).ifPresent(user -> {
            AuditLog log = new AuditLog();
            log.setUser(user);
            log.setAction(action);
            log.setResource(resource);
            log.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(log);
        });
    }

    private AuditLogResponse toResponse(AuditLog log) {
        AuditLogResponse dto = new AuditLogResponse();
        dto.setAuditId(log.getAuditId());
        if (log.getUser() != null) {
            dto.setUserId(log.getUser().getUserId());
        }
        dto.setAction(log.getAction());
        dto.setResource(log.getResource());
        dto.setTimestamp(log.getTimestamp());
        return dto;
    }
}