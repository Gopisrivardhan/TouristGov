package com.tourismgov.controller;

import com.tourismgov.dto.ComplianceRecordDto;
import com.tourismgov.dto.ComplianceRequest;
import com.tourismgov.service.ComplianceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/tourismgov/v1/compliance/records") 
@RequiredArgsConstructor
public class ComplianceController {

    private final ComplianceService complianceService;

    @PostMapping
    public ResponseEntity<ComplianceRecordDto> logComplianceRecord(@Valid @RequestBody ComplianceRequest request) {
        log.info("REST request to log compliance record: {}", request.getReferenceNumber());
        return new ResponseEntity<>(complianceService.createComplianceCheck(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<ComplianceRecordDto>> getComplianceRegister(Pageable pageable) {
        return ResponseEntity.ok(complianceService.getAllComplianceRecords(pageable));
    }

    @GetMapping("/{recordId}")
    public ResponseEntity<ComplianceRecordDto> getComplianceRecordById(@PathVariable Long recordId) {
        return ResponseEntity.ok(complianceService.getComplianceRecordById(recordId));
    }

    @PutMapping("/{recordId}/result")
    public ResponseEntity<ComplianceRecordDto> updateComplianceResult(
            @PathVariable Long recordId,
            @RequestParam String result) { 
        // Removed @RequestParam Long officerId for security!
        log.info("REST request to update compliance record {} to {}", recordId, result);
        return ResponseEntity.ok(complianceService.updateComplianceResult(recordId, result));
    }

    @DeleteMapping("/{recordId}")
    public ResponseEntity<String> deleteComplianceRecord(@PathVariable Long recordId) {
        complianceService.deleteComplianceRecord(recordId);
        return ResponseEntity.ok("Compliance record deleted successfully");
    }
}