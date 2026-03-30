package com.tourismgov.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor
@Builder
public class AuditDto {
    private Long auditId;
    private Long officerId; 
    private String officerName;
    private String scope;
    private String findings;
    private LocalDateTime date;
    private String status;
}