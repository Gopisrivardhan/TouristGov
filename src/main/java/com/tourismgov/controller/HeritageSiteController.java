package com.tourismgov.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tourismgov.dto.HeritageSiteRequest;
import com.tourismgov.dto.HeritageSiteResponse;
import com.tourismgov.service.HeritageSiteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/tourismgov/v1/sites")
@RequiredArgsConstructor
public class HeritageSiteController {

    private final HeritageSiteService siteService;

    @PostMapping
    public ResponseEntity<HeritageSiteResponse> createSite(@Valid @RequestBody HeritageSiteRequest request) {
        log.info("REST request to create Heritage Site: {}", request.getName());
        return new ResponseEntity<>(siteService.createSite(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<HeritageSiteResponse>> getAllSites() {
        return ResponseEntity.ok(siteService.getAllSites());
    }

    @GetMapping("/{siteId}")
    public ResponseEntity<HeritageSiteResponse> getSiteById(@PathVariable Long siteId) {
        return ResponseEntity.ok(siteService.getSiteById(siteId));
    }

    @PutMapping("/{siteId}")
    public ResponseEntity<HeritageSiteResponse> updateSite(
            @PathVariable Long siteId, 
            @Valid @RequestBody HeritageSiteRequest request) {
        return ResponseEntity.ok(siteService.updateSite(siteId, request));
    }

    @DeleteMapping("/{siteId}")
    public ResponseEntity<Void> deleteSite(@PathVariable Long siteId) {
        log.info("REST request to delete Heritage Site ID: {}", siteId);
        siteService.deleteSite(siteId);
        return ResponseEntity.noContent().build();
    }
}