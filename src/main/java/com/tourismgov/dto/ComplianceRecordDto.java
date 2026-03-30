package com.tourismgov.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class ComplianceRecordDto {
    private Long complianceId;
    private String referenceNumber;
    private Long entityId;     // ID of the Site/Event being checked
    private String entityType; // e.g., "SITE", "EVENT", "PROGRAM"
    private String result;     // PASSED, FAILED, PENDING
    private LocalDateTime date;
    private String notes;
}