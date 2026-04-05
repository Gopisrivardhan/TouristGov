package com.tourismgov.controller;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tourismgov.dto.AuditLogResponse;
import com.tourismgov.service.AuditLogService;

import lombok.RequiredArgsConstructor;

@RestController
// Professional touch: Always version your APIs (e.g., /api/v1/...)
@RequestMapping("/tourismgov/v1/audit-logs") 
@RequiredArgsConstructor // Lombok creates the constructor for the final service
public class AuditLogController {

    // Notice: We completely removed the AuditLogRepository. 
    // The Controller delegates EVERYTHING to the Service.
    private final AuditLogService auditLogService;

    /**
     * Gets all logs with pagination.
     * Example URL: GET /api/v1/audit-logs?page=0&size=20&sort=timestamp,desc
     */
    @GetMapping
    public ResponseEntity<Page<AuditLogResponse>> getAllLogs(
            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable) {
        return ResponseEntity.ok(auditLogService.getAllLogs(pageable));
    }

    /**
     * Gets logs for a specific user.
     * Example URL: GET /api/v1/audit-logs/user/5
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<AuditLogResponse>> getLogsByUser(
            @PathVariable Long userId, 
            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable) {
        return ResponseEntity.ok(auditLogService.getLogsByUserId(userId, pageable));
    }

    /**
     * Gets logs for a specific action.
     * Example URL: GET /api/v1/audit-logs/action/LOGIN
     */
    @GetMapping("/action/{action}")
    public ResponseEntity<Page<AuditLogResponse>> getLogsByAction(
            @PathVariable String action, 
            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable) {
        return ResponseEntity.ok(auditLogService.getLogsByAction(action, pageable));
    }

    /**
     * Gets logs between two dates.
     * Example URL: GET /api/v1/audit-logs/dates?start=2026-04-01T00:00:00&end=2026-04-03T23:59:59
     */
    @GetMapping("/dates")
    public ResponseEntity<Page<AuditLogResponse>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable) {
        return ResponseEntity.ok(auditLogService.getLogsByDateRange(start, end, pageable));
    }
}