package com.tourismgov.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ComplianceRequest {
    @NotBlank(message = "Reference number is required")
    private String referenceNumber;

    @NotNull(message = "Entity ID is required")
    private Long entityId; // Link to Site/Event/Program

    @NotBlank(message = "Compliance type (SITE/EVENT/PROGRAM) is required")
    private String complianceType;

    private String description;
    
    //We must know who is creating this record for the AuditLog!
    @NotNull(message = "Officer ID is required")
    private Long officerId; 
}