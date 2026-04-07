package com.tourismgov.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tourismgov.dto.HeritageSiteRequest;
import com.tourismgov.dto.HeritageSiteResponse;
import com.tourismgov.dto.PreservationActivityRequest;
import com.tourismgov.dto.PreservationActivityResponse;
import com.tourismgov.service.HeritageSiteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tourismgov/v1/sites")
@RequiredArgsConstructor
public class HeritageSiteController {

    private final HeritageSiteService siteService;

    // --- Site Management ---

    @PostMapping
    public ResponseEntity<HeritageSiteResponse> createSite(@Valid @RequestBody HeritageSiteRequest request) {
        // Now returns Response DTO to avoid loop
        return new ResponseEntity<>(siteService.createSite(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<HeritageSiteResponse>> getAllSites() {
        // Now returns a List of Response DTOs
        return ResponseEntity.ok(siteService.getAllSites());
    }

    @GetMapping("/{siteId}")
    public ResponseEntity<HeritageSiteResponse> getSiteById(@PathVariable Long siteId) {
        // This was the main looping point - now it returns the clean Response DTO
        return ResponseEntity.ok(siteService.getSiteById(siteId));
    }

    @PutMapping("/{siteId}")
    public ResponseEntity<HeritageSiteResponse> updateSite(
            @PathVariable Long siteId, 
            @Valid @RequestBody HeritageSiteRequest request) {
        return ResponseEntity.ok(siteService.updateSite(siteId, request));
    }

    @DeleteMapping("/{siteId}")
    public ResponseEntity<String> deleteSite(@PathVariable Long siteId) {
        siteService.deleteSite(siteId);
        return ResponseEntity.ok("Heritage Site deleted successfully");
    }

    // --- Preservation Activities ---
    
    @PostMapping("/{siteId}/activities")
    public ResponseEntity<PreservationActivityResponse> logActivity(
            @PathVariable Long siteId, 
            @Valid @RequestBody PreservationActivityRequest request) {
        
        // Explicitly define the type in the constructor to help the compiler
        PreservationActivityResponse response = siteService.logActivity(siteId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @PatchMapping("/activities/{activityId}/status")
    public ResponseEntity<PreservationActivityResponse> updateActivityStatus(
            @PathVariable Long activityId,
            @RequestParam String status) {
        
        PreservationActivityResponse response = siteService.updateActivityStatus(activityId, status);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{siteId}/activities")
    public ResponseEntity<List<PreservationActivityResponse>> getActivitiesBySite(@PathVariable Long siteId) {
        List<PreservationActivityResponse> responses = siteService.getActivitiesBySite(siteId);
        return ResponseEntity.ok(responses);
    }
    @DeleteMapping("/activities/{activityId}")
    public ResponseEntity<String> deleteActivity(@PathVariable Long activityId) {
        siteService.deleteActivity(activityId);
        return ResponseEntity.ok("Preservation Activity deleted successfully");
    }
}