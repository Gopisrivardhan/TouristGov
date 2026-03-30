package com.tourismgov.service;

import com.tourismgov.dto.ComplianceRecordDto;
import com.tourismgov.dto.ComplianceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ComplianceService {
    ComplianceRecordDto createComplianceCheck(ComplianceRequest request);
    Page<ComplianceRecordDto> getAllComplianceRecords(Pageable pageable);
    ComplianceRecordDto getComplianceRecordById(Long recordId);
    ComplianceRecordDto updateComplianceResult(Long recordId, String result, Long officerId);
    void deleteComplianceRecord(Long recordId);
}