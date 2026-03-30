package com.tourismgov.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audits")
@Getter             
@Setter             
@NoArgsConstructor  
public class Audit extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officer_id", nullable = false)
    private User officer;

    @Column(length = 100)
    private String scope;

    @Column(columnDefinition = "TEXT")
    private String findings;

    @Column(name = "audit_date")
    private LocalDateTime date;

    @Column(length = 20)
    private String status; // e.g., OPEN, IN_PROGRESS, CLOSED
}