package com.tourismgov.service;

import com.tourismgov.dto.AuditDto;
import java.util.List;

public interface AuditService {
    
    // Logic: Schedule or record an official audit
    AuditDto recordAudit(AuditDto auditDto);
    
    // Logic: Fetch all audits for the Management Dashboard (Section 7)
    List<AuditDto> getAllAudits();
    
    // Logic: Get audits by a specific officer
    List<AuditDto> getAuditsByOfficer(Long officerId);
    
    // Logic: Update findings for an ongoing audit
    AuditDto updateAuditFindings(Long auditId, String findings, String status);
}