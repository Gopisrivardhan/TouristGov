package com.tourismgov.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.tourismgov.dto.ComplianceRecordDto;
import com.tourismgov.dto.ComplianceRequest;
import com.tourismgov.model.ComplianceRecord;
import com.tourismgov.repository.ComplianceRecordRepository;
import com.tourismgov.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ComplianceServiceImpl implements ComplianceService {

    private final ComplianceRecordRepository complianceRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public ComplianceRecordDto createComplianceCheck(ComplianceRequest request) {
        log.info("Creating compliance record for: {}", request.getReferenceNumber());

        ComplianceRecord record = new ComplianceRecord();
        record.setReferenceNumber(request.getReferenceNumber());
        record.setEntityId(request.getEntityId());
        record.setType(request.getComplianceType().toUpperCase());
        record.setNotes(request.getDescription());
        record.setResult("PENDING");
        record.setDate(LocalDateTime.now()); // Set official inspection date

        ComplianceRecord saved = complianceRepository.save(record);

        // SECURE FIX: Track exactly who created the compliance check
        Long officerId = SecurityUtils.getCurrentUserId();
        auditLogService.logAction(officerId, "COMPLIANCE_CREATED", "REF_" + saved.getReferenceNumber(), "SUCCESS");

        return mapToComplianceDto(saved);
    }

    @Override
    public Page<ComplianceRecordDto> getAllComplianceRecords(Pageable pageable) {
        return complianceRepository.findAll(pageable).map(this::mapToComplianceDto);
    }

    @Override
    public ComplianceRecordDto getComplianceRecordById(Long recordId) {
        return complianceRepository.findById(recordId)
                .map(this::mapToComplianceDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found"));
    }

    @Override
    @Transactional
    public ComplianceRecordDto updateComplianceResult(Long recordId, String result) {
        ComplianceRecord record = complianceRepository.findById(recordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found"));

        record.setResult(result.toUpperCase());
        ComplianceRecord updated = complianceRepository.save(record);

        Long officerId = SecurityUtils.getCurrentUserId();
        auditLogService.logAction(officerId, "COMPLIANCE_UPDATED", "REF_" + record.getReferenceNumber(), "SUCCESS");

        return mapToComplianceDto(updated);
    }

    @Override
    @Transactional
    public void deleteComplianceRecord(Long recordId) {
        ComplianceRecord record = complianceRepository.findById(recordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found"));
        
        Long officerId = SecurityUtils.getCurrentUserId();
        auditLogService.logAction(officerId, "COMPLIANCE_DELETED", "REF_" + record.getReferenceNumber(), "SUCCESS");
        
        complianceRepository.delete(record);
    }

    private ComplianceRecordDto mapToComplianceDto(ComplianceRecord r) {
        return ComplianceRecordDto.builder()
                .complianceId(r.getComplianceId())
                .referenceNumber(r.getReferenceNumber())
                .entityId(r.getEntityId())
                .entityType(r.getType())
                .result(r.getResult())
                .date(r.getDate())
                .notes(r.getNotes())
                .build();
    }
}