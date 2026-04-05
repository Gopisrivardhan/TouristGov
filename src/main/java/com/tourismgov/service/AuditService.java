package com.tourismgov.service;

import com.tourismgov.dto.AuditDto;
import java.util.List;

public interface AuditService {
    
    // Record a new official government audit
    AuditDto recordAudit(AuditDto auditDto);
    
    // Update the findings or status of an ongoing audit
    AuditDto updateAuditFindings(Long auditId, String findings, String status);
    
    // Fetch all audits for the Management Dashboard
    List<AuditDto> getAllAudits();
    
    // Get audits conducted by a specific officer
    List<AuditDto> getAuditsByOfficer(Long officerId);
}