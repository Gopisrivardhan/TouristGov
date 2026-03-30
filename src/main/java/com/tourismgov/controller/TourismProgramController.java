package com.tourismgov.controller;

import com.tourismgov.dto.*;
import com.tourismgov.service.TourismProgramService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/tourismgov/v1/programs") // Standardized base path
public class TourismProgramController {

    private final TourismProgramService programService;

    public TourismProgramController(TourismProgramService programService) {
        this.programService = programService;
    }

    // --- PROGRAM MANAGEMENT ---

    @PostMapping
    public ResponseEntity<ProgramResponse> createProgram(@Valid @RequestBody ProgramRequest request) {
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

    // NEW: Full Update Endpoint
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

    // NEW: Delete Program Endpoint
    @DeleteMapping("/{programId}")
    public ResponseEntity<String> deleteProgram(@PathVariable Long programId) {
        programService.deleteProgram(programId);
        return ResponseEntity.ok("Program deleted successfully");
    }

    // --- RESOURCE MANAGEMENT ---

    @PostMapping("/{programId}/resources")
    public ResponseEntity<ResourceResponse> allocateResource(
            @PathVariable Long programId,
            @Valid @RequestBody ResourceRequest request) {
        return new ResponseEntity<>(programService.allocateResourceToProgram(programId, request), HttpStatus.CREATED);
    }

    @GetMapping("/{programId}/resources")
    public ResponseEntity<List<ResourceResponse>> getResourcesForProgram(@PathVariable Long programId) {
        return ResponseEntity.ok(programService.getResourcesByProgram(programId));
    }

    @PatchMapping("/resources/{resourceId}/status")
    public ResponseEntity<ResourceResponse> updateResourceStatus(
            @PathVariable Long resourceId,
            @RequestParam String status) {
        return ResponseEntity.ok(programService.updateResourceStatus(resourceId, status));
    }

    // NEW: Delete Resource Endpoint
    @DeleteMapping("/resources/{resourceId}")
    public ResponseEntity<String> deleteResource(@PathVariable Long resourceId) {
        programService.deleteResource(resourceId);
        return ResponseEntity.ok("Resource deleted successfully");
    }

    // --- ANALYTICS / AUDIT ENDPOINT ---

    @GetMapping("/{programId}/budget-report")
    public ResponseEntity<Map<String, Object>> getProgramBudgetReport(@PathVariable Long programId) {
        return ResponseEntity.ok(programService.getBudgetReport(programId));
    }
}