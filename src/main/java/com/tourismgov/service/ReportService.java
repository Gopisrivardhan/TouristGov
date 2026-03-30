package com.tourismgov.service;

import com.tourismgov.dto.ReportRequestDTO;
import com.tourismgov.dto.ReportSummaryDTO;
import com.tourismgov.enums.ReportScope;
import com.tourismgov.model.Report;
import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    // Method 1: Generate the snapshot
    Report generateReport(ReportRequestDTO request);

    // Method 2: Convert existing report to bytes for PDF
    public byte[] downloadReport(Long reportId, Long userId);

    // Method 3: Return lightweight summaries for the dashboard
    public List<ReportSummaryDTO> getReportHistory(Long userId, ReportScope scope, LocalDate date);
}