package com.tourismgov.service;

import com.tourismgov.dto.AuditLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface AuditLogService {
    // Internal System Logging
    void logAction(Long userId, String action, String resource, String status);
    void logActionInCurrentTransaction(Long userId, String action, String resource, String status);
    
    // Reporting & Fetching
    Page<AuditLogResponse> getAllLogs(Pageable pageable);
    Page<AuditLogResponse> getLogsByUserId(Long userId, Pageable pageable);
    Page<AuditLogResponse> getLogsByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable);
    Page<AuditLogResponse> getLogsByAction(String action, Pageable pageable);
}