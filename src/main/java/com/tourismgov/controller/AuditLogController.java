package com.tourismgov.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.tourismgov.dto.AuditLogRequest;
import com.tourismgov.dto.AuditLogResponse;
import com.tourismgov.model.AuditLog;
import com.tourismgov.repository.AuditLogRepository;
import com.tourismgov.service.AuditLogService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/tourismgov/audit-logs")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogRepository auditLogRepository, AuditLogService auditLogService) {
        this.auditLogRepository = auditLogRepository;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public List<AuditLogResponse> all() {
        return auditLogRepository.findAll().stream().map(this::toResponse).toList();
    }

    @PostMapping
    public ResponseEntity<AuditLogResponse> create(@Valid @RequestBody AuditLogRequest request) {
        // We save it, then immediately convert it to a Response DTO before sending it to the user
        AuditLog savedLog = auditLogService.create(request);
        return ResponseEntity.ok(toResponse(savedLog));
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