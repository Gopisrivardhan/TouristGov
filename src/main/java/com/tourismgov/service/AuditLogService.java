package com.tourismgov.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.tourismgov.dto.AuditLogRequest;
import com.tourismgov.dto.AuditLogResponse;
import com.tourismgov.model.AuditLog;

public interface AuditLogService {
    AuditLog create(AuditLogRequest request);
    List<AuditLog> createAll(List<AuditLogRequest> requests);
    
    // Fixed the return type to return a Page of DTOs for the Administrator Panel
    Page<AuditLogResponse> getAllLogs(Pageable pageable);
    
    // NEW: The helper method we used in AuditServiceImpl to automatically log actions
    void recordAction(Long userId, String action, String resource);
}