package com.tourismgov.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "resources")
@Getter
@Setter
@NoArgsConstructor
public class Resource extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resource_id")
    private Long resourceId;

    // The classification of the resource (e.g., FUNDS, VENUE, STAFF).
    @Column(name = "resource_type", nullable = false, length = 50)
    private String type;

    // The numerical value representing the resource amount (e.g., monetary value, number of staff).
    @Column(nullable = false)
    private Double quantity; 

    // The current state of the resource allocation (ALLOCATED, PENDING, EXHAUSTED).
    @Column(nullable = false, length = 50)
    private String status;

    // The overarching tourism program to which this resource is allocated.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private TourismProgram program;
}