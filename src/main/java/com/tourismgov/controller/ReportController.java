package com.tourismgov.controller;

import com.tourismgov.dto.ReportRequestDTO;
import com.tourismgov.dto.ReportSummaryDTO;
import com.tourismgov.enums.ReportScope;
import com.tourismgov.model.Report;
import com.tourismgov.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/tourismgov/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // API 1: Generate (Still returns the new Report)
    @PostMapping("/generate")
    public ResponseEntity<Report> generate(@RequestBody ReportRequestDTO request) {
        return new ResponseEntity<>(reportService.generateReport(request), HttpStatus.CREATED);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ReportSummaryDTO>> getHistory(
            @RequestParam Long userId,
            @RequestParam(required = false) ReportScope scope,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate date) {
        
        // Keeping your original logic structure
        List<ReportSummaryDTO> history = reportService.getReportHistory(userId, scope, date);
        
        return ResponseEntity.ok(history);
    }
    // API 3: Download (Remains binary for the PDF)
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> download(@PathVariable Long id, @RequestParam Long userId) {
        // We pass userId to the service to verify ownership
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(reportService.downloadReport(id, userId));
    }
}