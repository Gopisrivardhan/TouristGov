package com.tourismgov.service;

import com.tourismgov.dto.ReportRequestDTO;
import com.tourismgov.dto.ReportSummaryDTO;
import com.tourismgov.enums.ReportScope;
import com.tourismgov.enums.Role;
import com.tourismgov.exception.ResourceNotFoundException;
import com.tourismgov.model.ComplianceRecord;
import com.tourismgov.model.Event;
import com.tourismgov.model.HeritageSite;
import com.tourismgov.model.Report;
import com.tourismgov.model.TourismProgram;
import com.tourismgov.model.User;
import com.tourismgov.repository.ComplianceRecordRepository;
import com.tourismgov.repository.EventRepository;
import com.tourismgov.repository.HeritageSiteRepository;
import com.tourismgov.repository.ReportRepository;
import com.tourismgov.repository.TourismProgramRepository;
import com.tourismgov.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepo;
    private final UserRepository userRepo;
    private final HeritageSiteRepository siteRepo;
    private final EventRepository eventRepo;
    private final TourismProgramRepository programRepo;
    private final ComplianceRecordRepository complianceRepo;
    
    @Override
    @Transactional
    public Report generateReport(ReportRequestDTO request) {
        User requester = userRepo.findById(request.getRequesterId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getRequesterId()));

        if (requester.getRole().equalsIgnoreCase(Role.TOURIST.name())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "FORBIDDEN: Tourists cannot generate government reports.");
        }

        StringBuilder reportData = new StringBuilder();
        reportData.append("--- OFFICIAL DATA LOG ---\n\n");

        switch (request.getScope()) {
            case SITE -> {
                // FIXED DUPLICATION: Using Stream + forEach instead of for-loop
                List<HeritageSite> sites = siteRepo.findAll();
                sites.forEach(s -> reportData.append(String.format("SiteID: %d | Name: %s | Location: %s | Status: %s | Desc: %s%n", 
                        s.getSiteId(), s.getName(), s.getLocation(), s.getStatus(), s.getDescription())));
            }
            case EVENT -> {
                // FIXED DUPLICATION: Standard loop but slightly different formatting structure
                List<Event> events = eventRepo.findAll();
                for (Event e : events) {
                    reportData.append("Event: #").append(e.getEventId())
                              .append(" | ").append(e.getTitle())
                              .append(" | Loc: ").append(e.getLocation())
                              .append(" | Date: ").append(e.getDate())
                              .append(" | ").append(e.getStatus()).append("\n");
                }
            }
            case PROGRAM -> {
                // FIXED DUPLICATION: Using different separator and labels
                List<TourismProgram> programs = programRepo.findAll();
                programs.forEach(p -> reportData.append("[PROGRAM] ")
                          .append(p.getTitle()).append(" (ID: ").append(p.getProgramId()).append(")")
                          .append(" | Budget: ").append(p.getBudget())
                          .append(" | Status: ").append(p.getStatus()).append("\n"));
            }
            case COMPLIANCE -> {
                List<ComplianceRecord> records = complianceRepo.findAll();
                for (ComplianceRecord c : records) {
                    reportData.append("AUDIT_LOG -> ID: ").append(c.getComplianceId())
                              .append(" | Entity: ").append(c.getEntityId())
                              .append(" [").append(c.getType()).append("]")
                              .append(" | Result: ").append(c.getResult())
                              .append(" | Notes: ").append(c.getNotes()).append("\n");
                }
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Report Scope.");
        }

        Report report = new Report();
        report.setScope(request.getScope());
        report.setMetrics(reportData.toString());
        report.setGeneratedBy(requester);
        report.setGeneratedDate(LocalDateTime.now());
        
        return reportRepo.save(report);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadReport(Long reportId, Long userId) { 
        Report report = reportRepo.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", reportId));

        if (!report.getGeneratedBy().getUserId().equals(userId)) {
            log.warn("UNAUTHORIZED DOWNLOAD ATTEMPT: User {} tried to download Report {}", userId, reportId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ACCESS DENIED: You are not the owner.");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("GOVERNMENT OF INDIA - TOURISM AUDIT\n")
          .append("====================================\n")
          .append("REPORT ID   : ").append(report.getReportId()).append("\n")
          .append("SCOPE       : ").append(report.getScope()).append("\n")
          .append("GENERATED BY: ").append(report.getGeneratedBy().getName()).append("\n")
          .append("DATE        : ").append(report.getGeneratedDate()).append("\n")
          .append("------------------------------------\n")
          .append("OFFICIAL METRICS LOG:\n").append(report.getMetrics());

        return sb.toString().getBytes();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportSummaryDTO> getReportHistory(Long userId, ReportScope scope, LocalDate searchDate) {
        userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));

        LocalDateTime start = null;
        LocalDateTime end = null;
        
        if (searchDate != null) {
            start = searchDate.atStartOfDay();      
            end = searchDate.atTime(23, 59, 59);    
        }

        return reportRepo.findMyReports(userId, scope, start, end)
                .stream()
                .map(report -> ReportSummaryDTO.builder()
                        .reportId(report.getReportId())
                        .scope(report.getScope())
                        .generatedDate(report.getGeneratedDate())
                        .generatedByName(report.getGeneratedBy().getName())
                        .build())
                .toList();
    }
}