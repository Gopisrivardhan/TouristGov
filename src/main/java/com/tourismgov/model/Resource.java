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
public class Resource extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resource_id")
    private Long resourceId;

    // Links to TourismProgram
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private TourismProgram program;

    // Type: Funds, Venue, or Staff
    @Column(name = "resource_type", nullable = false, length = 50)
    private String type;

    // Quantity (Can represent amount of staff, or amount of funds)
    @Column(nullable = false)
    private Double quantity; 

    @Column(nullable = false, length = 50)
    private String status;
}