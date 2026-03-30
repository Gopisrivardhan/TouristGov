package com.tourismgov.model;

import com.tourismgov.enums.ReportScope;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")  
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @NotNull(message = "Report scope is mandatory (e.g., SITE, EVENT, PROGRAM)")
    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false)
    private ReportScope scope;

    @NotBlank(message = "Report metrics data cannot be empty or null")
    @Column(name = "metrics", columnDefinition = "TEXT")
    private String metrics;

    @NotNull(message = "Generated date must be recorded")
    @Column(name = "generated_date", nullable = false)
    private LocalDateTime generatedDate;

    @NotNull(message = "The User who generated this report must be identified")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by", nullable = false)
    private User generatedBy;
}