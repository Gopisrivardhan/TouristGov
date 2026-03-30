package com.tourismgov.controller;

import com.tourismgov.dto.HeritageSiteRequest;
import com.tourismgov.dto.PreservationActivityRequest;
import com.tourismgov.model.HeritageSite;
import com.tourismgov.model.PreservationActivity;
import com.tourismgov.service.HeritageSiteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tourismgov/v1/sites")
public class HeritageSiteController {

    private final HeritageSiteService siteService;

    public HeritageSiteController(HeritageSiteService siteService) {
        this.siteService = siteService;
    }

    // --- Site Management ---

    @PostMapping
    public ResponseEntity<HeritageSite> createSite(@Valid @RequestBody HeritageSiteRequest request) {
        return new ResponseEntity<>(siteService.createSite(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<HeritageSite>> getAllSites() {
        return ResponseEntity.ok(siteService.getAllSites());
    }

    @GetMapping("/{siteId}")
    public ResponseEntity<HeritageSite> getSiteById(@PathVariable Long siteId) {
        return ResponseEntity.ok(siteService.getSiteById(siteId));
    }

    @PutMapping("/{siteId}")
    public ResponseEntity<HeritageSite> updateSite(
            @PathVariable Long siteId, 
            @Valid @RequestBody HeritageSiteRequest request) {
        return ResponseEntity.ok(siteService.updateSite(siteId, request));
    }

    // NEW: Delete Site Endpoint
    @DeleteMapping("/{siteId}")
    public ResponseEntity<String> deleteSite(@PathVariable Long siteId) {
        siteService.deleteSite(siteId);
        return ResponseEntity.ok("Heritage Site deleted successfully");
    }

    // --- Preservation Activities ---

    @PostMapping("/{siteId}/activities")
    public ResponseEntity<PreservationActivity> logActivity(
            @PathVariable Long siteId, 
            @Valid @RequestBody PreservationActivityRequest request) {
        return new ResponseEntity<>(siteService.logActivity(siteId, request), HttpStatus.CREATED);
    }
    
    @GetMapping("/{siteId}/activities")
    public ResponseEntity<List<PreservationActivity>> getActivitiesBySite(@PathVariable Long siteId) {
        return ResponseEntity.ok(siteService.getActivitiesBySite(siteId));
    }

    // NEW: Delete Activity Endpoint
    @DeleteMapping("/activities/{activityId}")
    public ResponseEntity<String> deleteActivity(@PathVariable Long activityId) {
        siteService.deleteActivity(activityId);
        return ResponseEntity.ok("Preservation Activity deleted successfully");
    }
}