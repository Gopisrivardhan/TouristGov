package com.tourismgov.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime; // Don't forget this import!

@Entity
@Table(name = "audit_logs")
@Getter             
@Setter             
@NoArgsConstructor  
public class AuditLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    // Many-to-One: Many logs belong to one User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "action_performed", nullable = false)
    private String action;

    @Column(name = "resource_accessed")
    private String resource;

    // Added the timestamp field back!
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
}