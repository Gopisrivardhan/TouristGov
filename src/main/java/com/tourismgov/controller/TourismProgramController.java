package com.tourismgov.controller;

import com.tourismgov.dto.ProgramRequest;
import com.tourismgov.dto.ProgramResponse;
import com.tourismgov.service.TourismProgramService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/tourismgov/v1/programs")
@RequiredArgsConstructor
public class TourismProgramController {

    private final TourismProgramService programService;

    @PostMapping
    public ResponseEntity<ProgramResponse> createProgram(@Valid @RequestBody ProgramRequest request) {
        log.info("REST request to create Tourism Program: '{}'", request.getTitle());
        return new ResponseEntity<>(programService.createProgram(request), HttpStatus.CREATED);
    }

    @GetMapping("/{programId}")
    public ResponseEntity<ProgramResponse> getProgramById(@PathVariable Long programId) {
        return ResponseEntity.ok(programService.getProgramById(programId));
    }

    @GetMapping
    public ResponseEntity<List<ProgramResponse>> getAllPrograms() {
        return ResponseEntity.ok(programService.getAllPrograms());
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<ProgramResponse>> getProgramsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(programService.getProgramsPaged(page, size));
    }

    @PutMapping("/{programId}")
    public ResponseEntity<ProgramResponse> updateProgram(
            @PathVariable Long programId, 
            @Valid @RequestBody ProgramRequest request) {
        return ResponseEntity.ok(programService.updateProgram(programId, request));
    }

    @PatchMapping("/{programId}/status")
    public ResponseEntity<ProgramResponse> updateProgramStatus(
            @PathVariable Long programId,
            @RequestParam String status) {
        return ResponseEntity.ok(programService.updateProgramStatus(programId, status));
    }

    @DeleteMapping("/{programId}")
    public ResponseEntity<Void> deleteProgram(@PathVariable Long programId) {
        programService.deleteProgram(programId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reporting API: Generates financial health status for a program
     */
    @GetMapping("/{programId}/budget-report")
    public ResponseEntity<Map<String, Object>> getProgramBudgetReport(@PathVariable Long programId) {
        log.info("REST request to generate budget report for Program ID: {}", programId);
        return ResponseEntity.ok(programService.getBudgetReport(programId));
    }
}