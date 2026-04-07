package com.tourismgov.service;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourismgov.autosender.GlobalActivityEvent;
import com.tourismgov.dto.HeritageSiteRequest;
import com.tourismgov.dto.HeritageSiteResponse;
import com.tourismgov.dto.PreservationActivityResponse;
import com.tourismgov.enums.NotificationCategory;
import com.tourismgov.enums.SiteStatus;
import com.tourismgov.exception.ResourceNotFoundException;
import com.tourismgov.model.HeritageSite;
import com.tourismgov.model.PreservationActivity;
import com.tourismgov.repository.HeritageSiteRepository;
import com.tourismgov.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeritageSiteServiceImpl implements HeritageSiteService {

    private static final String RESOURCE_SITE = "HeritageSiteService";
    private static final String ENTITY_SITE = "Heritage Site";
    private static final String STATUS_SUCCESS = "SUCCESS";
    
    private static final String ACTION_SITE_CREATE = "SITE_CREATE";
    private static final String ACTION_SITE_UPDATE = "SITE_UPDATE";
    private static final String ACTION_SITE_DELETE = "SITE_DELETE";

    private final HeritageSiteRepository siteRepository;
    private final AuditLogService auditLogService; 
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public HeritageSiteResponse createSite(HeritageSiteRequest request) {
        log.info("Attempting to create heritage site: {}", request.getName());
        
        HeritageSite site = new HeritageSite();
        site.setName(request.getName());
        site.setLocation(request.getLocation());
        site.setDescription(request.getDescription());
        
        // 1. Professional Enum Validation
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                site.setStatus(SiteStatus.valueOf(request.getStatus().toUpperCase()).name());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid Site Status. Allowed: OPEN, CLOSED_FOR_MAINTENANCE, RESTORATION_IN_PROGRESS, PERMANENTLY_CLOSED");
            }
        } else {
            site.setStatus(SiteStatus.OPEN.name());
        }
        
        HeritageSite saved = siteRepository.save(site);
        
        Long currentUserId = SecurityUtils.getCurrentUserId();
        auditLogService.logAction(currentUserId, ACTION_SITE_CREATE, RESOURCE_SITE, STATUS_SUCCESS);

        String message = String.format("New heritage site added: %s at %s.", saved.getName(), saved.getLocation());
        eventPublisher.publishEvent(new GlobalActivityEvent(
                currentUserId, saved.getSiteId(), "New Heritage Site Added!",
                message, NotificationCategory.SYSTEM 
        ));

        return mapToSiteResponse(saved);
    }

    @Override
    @Transactional
    public HeritageSiteResponse updateSite(Long siteId, HeritageSiteRequest request) {
        HeritageSite site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_SITE, siteId));
        
        String oldStatus = site.getStatus();
        site.setName(request.getName());
        site.setLocation(request.getLocation());
        site.setDescription(request.getDescription());
        
        boolean statusChanged = false;
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                String newStatus = SiteStatus.valueOf(request.getStatus().toUpperCase()).name();
                if (!oldStatus.equals(newStatus)) {
                    site.setStatus(newStatus);
                    statusChanged = true;
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status provided for the site.");
            }
        }
        
        HeritageSite updatedSite = siteRepository.save(site);
        auditLogService.logAction(SecurityUtils.getCurrentUserId(), ACTION_SITE_UPDATE, RESOURCE_SITE, STATUS_SUCCESS);

        if (statusChanged) {
            String message = String.format("Notice: %s is now marked as %s.", site.getName(), site.getStatus());
            eventPublisher.publishEvent(new GlobalActivityEvent(
                    SecurityUtils.getCurrentUserId(), site.getSiteId(), "Site Status Update",
                    message, NotificationCategory.SYSTEM
            ));
        }

        return mapToSiteResponse(updatedSite);
    }

    @Override
    public List<HeritageSiteResponse> getAllSites() {
        return siteRepository.findAll().stream().map(this::mapToSiteResponse).toList(); 
    }

    @Override
    public HeritageSiteResponse getSiteById(Long siteId) {
        return siteRepository.findById(siteId)
                .map(this::mapToSiteResponse)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_SITE, siteId));
    }

    @Override
    @Transactional
    public void deleteSite(Long siteId) {
        if (!siteRepository.existsById(siteId)) {
            throw new ResourceNotFoundException(ENTITY_SITE, siteId);
        }
        siteRepository.deleteById(siteId);
        auditLogService.logAction(SecurityUtils.getCurrentUserId(), ACTION_SITE_DELETE, RESOURCE_SITE, STATUS_SUCCESS);
    }

    // --- Private Mappers ---
    
    private HeritageSiteResponse mapToSiteResponse(HeritageSite site) {
        HeritageSiteResponse res = new HeritageSiteResponse();
        res.setSiteId(site.getSiteId());
        res.setName(site.getName());
        res.setLocation(site.getLocation());
        res.setDescription(site.getDescription());
        res.setStatus(site.getStatus());
        
        if (site.getPreservationActivities() != null) {
            res.setPreservationActivities(site.getPreservationActivities().stream()
                    .map(this::mapToActivityResponse) 
                    .toList());
        }
        return res;
    }
    
    private PreservationActivityResponse mapToActivityResponse(PreservationActivity activity) {
        PreservationActivityResponse response = new PreservationActivityResponse();
        response.setActivityId(activity.getActivityId());
        response.setDescription(activity.getDescription());
        if (activity.getDate() != null) {
            response.setDate(activity.getDate().atStartOfDay());
        }
        response.setStatus(activity.getStatus());
        response.setCreatedAt(activity.getCreatedAt());
        if (activity.getSite() != null) response.setSiteId(activity.getSite().getSiteId());
        if (activity.getOfficer() != null) response.setOfficerId(activity.getOfficer().getUserId());
        return response;
    }
}