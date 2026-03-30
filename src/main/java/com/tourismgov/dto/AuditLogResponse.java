package com.tourismgov.dto;

import java.time.LocalDateTime;

public class AuditLogResponse {
    
    // Both IDs changed to Long
    private Long auditId; 
    private Long userId;  
    
    private String action;
    private String resource;
    private LocalDateTime timestamp;

    // Getters and Setters updated to expect Long
    public Long getAuditId() { return auditId; }
    public void setAuditId(Long auditId) { this.auditId = auditId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}