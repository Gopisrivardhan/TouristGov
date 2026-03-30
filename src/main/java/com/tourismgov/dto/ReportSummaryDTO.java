package com.tourismgov.dto;

import com.tourismgov.enums.ReportScope;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ReportSummaryDTO {
    private Long reportId;
    private ReportScope scope;
    private LocalDateTime generatedDate;
    private String generatedByName; // Just the name, not the whole User object
}