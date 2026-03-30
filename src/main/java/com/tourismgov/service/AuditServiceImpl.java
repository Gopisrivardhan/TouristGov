package com.tourismgov.service;

import com.tourismgov.model.Audit;
import com.tourismgov.model.User;
import com.tourismgov.repository.AuditRepository;
import com.tourismgov.repository.UserRepository;
import com.tourismgov.dto.AuditDto;
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
public class AuditServiceImpl implements AuditService {

    private final AuditRepository auditRepository;
    private final UserRepository userRepository; // Added to fetch the linked User entity
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public AuditDto recordAudit(AuditDto dto) {
        log.info("Recording official audit for scope: {}", dto.getScope());

        // PROPER LINKING: Fetch the actual User object from the database using the provided ID
        User officer = userRepository.findById(dto.getOfficerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Officer not found with ID: " + dto.getOfficerId()));

        Audit audit = new Audit();
        audit.setOfficer(officer); // Attach the linked entity
        audit.setScope(dto.getScope());
        audit.setFindings(dto.getFindings());
        audit.setDate(dto.getDate());
        audit.setStatus(dto.getStatus() != null ? dto.getStatus() : "OPEN");

        Audit saved = auditRepository.save(audit);

        auditLogService.recordAction(dto.getOfficerId(), "OFFICIAL_AUDIT_CREATED", "AUDIT_ID_" + saved.getAuditId());

        return mapToDto(saved);
    }

    @Override
    @Transactional
    public AuditDto updateAuditFindings(Long auditId, String findings, String status) {
        Audit audit = auditRepository.findById(auditId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Audit record not found"));

        audit.setFindings(findings);
        audit.setStatus(status != null ? status.toUpperCase() : audit.getStatus());

        Audit updated = auditRepository.save(audit);
        
        // Use the linked officer's ID for the log
        auditLogService.recordAction(audit.getOfficer().getUserId(), "OFFICIAL_AUDIT_UPDATED", "AUDIT_ID_" + auditId);

        return mapToDto(updated);
    }

    @Override
    public List<AuditDto> getAllAudits() {
        return auditRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public List<AuditDto> getAuditsByOfficer(Long officerId) {
        return auditRepository.findByOfficer_UserId(officerId).stream()
                .map(this::mapToDto)
                .toList();
    }

    // Helper method to convert the Entity back to a flat DTO for the frontend
    private AuditDto mapToDto(Audit audit) {
        return AuditDto.builder()
                .auditId(audit.getAuditId())
                .officerId(audit.getOfficer().getUserId()) // Extract ID from linked entity
                .officerName(audit.getOfficer().getName()) // Extract Name from linked entity
                .scope(audit.getScope())
                .findings(audit.getFindings())
                .date(audit.getDate())
                .status(audit.getStatus())
                .build();
    }
}