package com.tourismgov.service;

import com.tourismgov.model.Audit;
import com.tourismgov.model.User;
import com.tourismgov.repository.AuditRepository;
import com.tourismgov.repository.UserRepository;
import com.tourismgov.dto.AuditDto;
import com.tourismgov.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuditServiceImpl implements AuditService {

    private final AuditRepository auditRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public AuditDto recordAudit(AuditDto dto) {
        log.info("Recording official audit for scope: {}", dto.getScope());

        // SECURE FIX: Get the officer ID from the Bearer Token, not the DTO
        Long officerId = SecurityUtils.getCurrentUserId();
        User officer = userRepository.findById(officerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Officer not found"));

        Audit audit = new Audit();
        audit.setOfficer(officer); 
        audit.setScope(dto.getScope());
        audit.setFindings(dto.getFindings());
        audit.setDate(dto.getDate());
        audit.setStatus(dto.getStatus() != null ? dto.getStatus() : "OPEN");

        Audit saved = auditRepository.save(audit);
        auditLogService.logAction(officerId, "OFFICIAL_AUDIT_CREATED", "AUDIT_ID_" + saved.getAuditId(), "SUCCESS");

        return mapToDto(saved);
    }

    @Override
    @Transactional
    public AuditDto updateAuditFindings(Long auditId, String findings, String status) {
        Audit audit = auditRepository.findById(auditId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Audit record not found"));

        if (findings != null) audit.setFindings(findings);
        if (status != null) audit.setStatus(status.toUpperCase());

        Audit updated = auditRepository.save(audit);
        
        Long officerId = SecurityUtils.getCurrentUserId();
        auditLogService.logAction(officerId, "OFFICIAL_AUDIT_UPDATED", "AUDIT_ID_" + auditId, "SUCCESS");

        return mapToDto(updated);
    }

    @Override
    public List<AuditDto> getAllAudits() {
        return auditRepository.findAll().stream().map(this::mapToDto).toList();
    }

    @Override
    public List<AuditDto> getAuditsByOfficer(Long officerId) {
        return auditRepository.findByOfficer_UserId(officerId).stream().map(this::mapToDto).toList();
    }

    private AuditDto mapToDto(Audit audit) {
        return AuditDto.builder()
                .auditId(audit.getAuditId())
                .officerId(audit.getOfficer().getUserId())
                .officerName(audit.getOfficer().getName())
                .scope(audit.getScope())
                .findings(audit.getFindings())
                .date(audit.getDate())
                .status(audit.getStatus())
                .build();
    }
}