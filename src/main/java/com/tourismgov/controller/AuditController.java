package com.tourismgov.controller;

import com.tourismgov.dto.AuditDto;
import com.tourismgov.dto.AuditLogResponse;
import com.tourismgov.service.AuditLogService;
import com.tourismgov.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tourismgov/audits")
@CrossOrigin(origins = "*")
public class AuditController {

    private final AuditLogService auditLogService;
    private final AuditService auditService;

    public AuditController(AuditLogService auditLogService, AuditService auditService) {
        this.auditLogService = auditLogService;
        this.auditService = auditService;
    }

    // --- SYSTEM LOGS (Identity & Access Module) ---

    @GetMapping("/logs")
    public ResponseEntity<Page<AuditLogResponse>> getAllAuditLogs(Pageable pageable) {
        return ResponseEntity.ok(auditLogService.getAllLogs(pageable));
    }
    
    // --- OFFICIAL GOVERNMENT AUDITS (Compliance Module) ---

    // NEW: Fetch all official audits for the Government Auditor Dashboard
    @GetMapping("/official")
    public ResponseEntity<List<AuditDto>> getAllOfficialAudits() {
        return ResponseEntity.ok(auditService.getAllAudits());
    }

    // NEW: Fetch official audits conducted by a specific officer
    @GetMapping("/official/officer/{officerId}")
    public ResponseEntity<List<AuditDto>> getAuditsByOfficer(@PathVariable Long officerId) {
        return ResponseEntity.ok(auditService.getAuditsByOfficer(officerId));
    }

    @PostMapping("/official")
    public ResponseEntity<AuditDto> recordOfficialAudit(@Valid @RequestBody AuditDto auditDto) {
        AuditDto savedAudit = auditService.recordAudit(auditDto);
        return new ResponseEntity<>(savedAudit, HttpStatus.CREATED);
    }

    @PutMapping("/official/{id}")
    public ResponseEntity<AuditDto> updateAuditFindings(
            @PathVariable("id") Long auditId,
            @RequestBody Map<String, String> updates) {
        
        String findings = updates.get("findings");
        String status = updates.get("status");
        
        AuditDto updatedAudit = auditService.updateAuditFindings(auditId, findings, status);
        return ResponseEntity.ok(updatedAudit);
    }
}