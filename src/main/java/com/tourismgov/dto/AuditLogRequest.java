package com.tourismgov.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuditLogRequest {

    @NotBlank(message = "Action is required")
    private String action;

    @NotBlank(message = "Resource is required")
    private String resource;

    @NotNull(message = "Timestamp is required")
    private LocalDateTime timestamp;

    // We don't need Audit ID here because the database auto-generates it!
    
    @NotNull(message = "User ID is required")
    private Long userId; // MUST be Long to match the User entity
}