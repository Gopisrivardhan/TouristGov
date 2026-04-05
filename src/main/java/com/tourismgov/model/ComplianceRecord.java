package com.tourismgov.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "compliance_records")
@Getter 
@Setter 
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceRecord extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long complianceId;

    private String referenceNumber;
    private Long entityId; 
    private String type;   
    private String result; 
    private LocalDateTime date;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
}