package com.tourismgov.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static com.tourismgov.exception.ErrorMessages.RECORD_NOT_FOUND;
import com.tourismgov.dto.ComplianceRecordDto;
import com.tourismgov.dto.ComplianceRequest;
import com.tourismgov.model.ComplianceRecord;
import com.tourismgov.repository.ComplianceRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

        ComplianceRecord saved = complianceRepository.save(record);
        
        // PROPER LOGGING: Now it accurately records WHICH officer created the check
        auditLogService.recordAction(request.getOfficerId(), "COMPLIANCE_CREATED", "REF_" + saved.getReferenceNumber());
        
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, RECORD_NOT_FOUND));
    }

    @Override
    @Transactional
    public ComplianceRecordDto updateComplianceResult(Long recordId, String result, Long officerId) {
        ComplianceRecord record = complianceRepository.findById(recordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, RECORD_NOT_FOUND));
                
        record.setResult(result.toUpperCase());
        ComplianceRecord updated = complianceRepository.save(record);
        
        // PROPER LOGGING
        auditLogService.recordAction(officerId, "COMPLIANCE_UPDATED", "REF_" + record.getReferenceNumber());
        
        return mapToComplianceDto(updated);
    }
    
    @Override
    @Transactional
    public void deleteComplianceRecord(Long recordId) {
        ComplianceRecord record = complianceRepository.findById(recordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,RECORD_NOT_FOUND));
        complianceRepository.delete(record);
    }

    // --- Private Mapper Method ---
    private ComplianceRecordDto mapToComplianceDto(ComplianceRecord r) {
        return ComplianceRecordDto.builder()
                .complianceId(r.getComplianceId())
                .referenceNumber(r.getReferenceNumber())
                .entityId(r.getEntityId())
                .entityType(r.getType())
                .result(r.getResult())
                // BaseEntity automatically handles the date, so we pull it from getCreatedAt()
                .date(r.getCreatedAt()) 
                .notes(r.getNotes())
                .build();
    }
}