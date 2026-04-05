package com.tourismgov.service;

import com.tourismgov.dto.ComplianceRecordDto;
import com.tourismgov.dto.ComplianceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ComplianceService {
    
    // Create a new compliance check
    ComplianceRecordDto createComplianceCheck(ComplianceRequest request);
    
    // Fetch all compliance records with pagination
    Page<ComplianceRecordDto> getAllComplianceRecords(Pageable pageable);
    
    // Fetch a specific record by its ID
    ComplianceRecordDto getComplianceRecordById(Long recordId);
    
    // Update the result (e.g., PASSED, FAILED) without needing the officer ID in the parameters
    ComplianceRecordDto updateComplianceResult(Long recordId, String result);
    
    // Delete a compliance record
    void deleteComplianceRecord(Long recordId);
}