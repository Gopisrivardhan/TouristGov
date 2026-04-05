package com.tourismgov.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuditLogRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Action is required")
    @Size(max = 50, message = "Action cannot exceed 50 characters")
    private String action;

    @NotBlank(message = "Resource is required")
    private String resource;

    // NEW: We added this to the entity earlier to track SUCCESS/FAILED
    @NotBlank(message = "Status is required")
    @Size(max = 20, message = "Status cannot exceed 20 characters")
    private String status;
    
    // Notice: Timestamp is intentionally removed!
}