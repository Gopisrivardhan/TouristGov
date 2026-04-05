package com.tourismgov.service;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tourismgov.autosender.GlobalActivityEvent;
import com.tourismgov.dto.HeritageSiteRequest;
import com.tourismgov.dto.HeritageSiteResponse;
import com.tourismgov.dto.PreservationActivityRequest;
import com.tourismgov.dto.PreservationActivityResponse;
import com.tourismgov.dto.ProgramSummary;
import com.tourismgov.enums.NotificationCategory;
import com.tourismgov.model.HeritageSite;
import com.tourismgov.model.PreservationActivity;
import com.tourismgov.model.User;
import com.tourismgov.repository.HeritageSiteRepository;
import com.tourismgov.repository.PreservationActivityRepository;
import com.tourismgov.repository.UserRepository;
import com.tourismgov.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeritageSiteServiceImpl implements HeritageSiteService {

    private static final String RESOURCE_SITE = "HeritageSiteService";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String ACTION_SITE_CREATE = "SITE_CREATE";
    private static final String ACTION_SITE_UPDATE = "SITE_UPDATE";
    private static final String ACTION_SITE_DELETE = "SITE_DELETE";
    private static final String ACTION_ACTIVITY_LOG = "PRESERVATION_LOG";
    private static final String ACTION_ACTIVITY_DELETE = "PRESERVATION_DELETE";

    private final HeritageSiteRepository siteRepository;
    private final PreservationActivityRepository activityRepository;
    private final UserRepository userRepository; 
    private final AuditLogService auditLogService; 
    private final ApplicationEventPublisher eventPublisher;
    @Override
    @Transactional
    public HeritageSiteResponse createSite(HeritageSiteRequest request) {
        HeritageSite site = new HeritageSite();
        site.setName(request.getName());
        site.setLocation(request.getLocation());
        site.setDescription(request.getDescription());
        site.setStatus("OPEN");
        
        HeritageSite saved = siteRepository.save(site);
        
        Long currentUserId = SecurityUtils.getCurrentUserId();
        auditLogService.logAction(currentUserId, ACTION_SITE_CREATE, RESOURCE_SITE, STATUS_SUCCESS);

        // TRIGGER GLOBAL NOTIFICATION
        String message = String.format("We are thrilled to announce a new heritage site is now open: %s, located at %s. Plan your visit today!", 
                saved.getName(), saved.getLocation());
                
        eventPublisher.publishEvent(new GlobalActivityEvent(
                currentUserId,
                saved.getSiteId(),
                "New Heritage Site Added!",
                message,
                NotificationCategory.SYSTEM // Or you can create NotificationCategory.SITE
        ));

        return mapToSiteResponse(saved);
    }

    @Override
    public List<HeritageSiteResponse> getAllSites() {
        return siteRepository.findAll().stream()
                .map(this::mapToSiteResponse)
                .toList(); 
    }

    @Override
    public HeritageSiteResponse getSiteById(Long siteId) {
        HeritageSite site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Heritage Site not found with ID: " + siteId));
        return mapToSiteResponse(site);
    }

    @Override
    @Transactional
    public HeritageSiteResponse updateSite(Long siteId, HeritageSiteRequest request) {
        HeritageSite site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Site not found"));
        
        String oldStatus = site.getStatus();
        
        site.setName(request.getName());
        site.setLocation(request.getLocation());
        site.setDescription(request.getDescription());
        
        boolean statusChanged = false;
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            String newStatus = request.getStatus().toUpperCase();
            if (!oldStatus.equals(newStatus)) {
                site.setStatus(newStatus);
                statusChanged = true;
            }
        }
        
        HeritageSite updatedSite = siteRepository.save(site);
        
        Long currentUserId = SecurityUtils.getCurrentUserId();
        auditLogService.logAction(currentUserId, ACTION_SITE_UPDATE, RESOURCE_SITE, STATUS_SUCCESS);

        // TRIGGER GLOBAL NOTIFICATION (Only if site status changed, e.g., CLOSED for maintenance)
        if (statusChanged) {
            String message = String.format("Notice: The heritage site '%s' is now marked as %s.", 
                    site.getName(), site.getStatus());
                    
            eventPublisher.publishEvent(new GlobalActivityEvent(
                    currentUserId,
                    site.getSiteId(),
                    "Heritage Site Status Update",
                    message,
                    NotificationCategory.SYSTEM
            ));
        }

        return mapToSiteResponse(updatedSite);
    }

    @Override
    @Transactional
    public void deleteSite(Long siteId) {
        if (!siteRepository.existsById(siteId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Site not found");
        }
        siteRepository.deleteById(siteId);
        auditLogService.logAction(SecurityUtils.getCurrentUserId(), ACTION_SITE_DELETE, RESOURCE_SITE, STATUS_SUCCESS);
    }

    // --- Preservation Activities ---
    @Override
    @Transactional
    public PreservationActivityResponse logActivity(Long siteId, PreservationActivityRequest request) {
        HeritageSite site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Site not found"));

        Long currentUserId = SecurityUtils.getCurrentUserId();
        User officer = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Officer not found"));

        PreservationActivity activity = new PreservationActivity();
        activity.setSite(site); 
        activity.setOfficer(officer); 
        activity.setDescription(request.getDescription());
        activity.setDate(request.getDate());
        activity.setStatus(request.getStatus() != null ? request.getStatus().toUpperCase() : "IN_PROGRESS");
        
        PreservationActivity saved = activityRepository.save(activity);
        
        // Log audit
        auditLogService.logAction(currentUserId, ACTION_ACTIVITY_LOG, RESOURCE_SITE, STATUS_SUCCESS);
        
        // MAP TO CLEAN DTO (This stops the loop)
        PreservationActivityResponse response = new PreservationActivityResponse();
        response.setActivityId(saved.getActivityId());
        response.setDescription(saved.getDescription());
        response.setDate(saved.getDate());
        response.setStatus(saved.getStatus());
        response.setCreatedAt(saved.getCreatedAt()); // From BaseEntity
        response.setSiteId(site.getSiteId());
        response.setOfficerId(officer.getUserId());

        return response;
    }
    
    @Override
    @Transactional
    public PreservationActivityResponse updateActivityStatus(Long activityId, String status) {
        // 1. Fetch the Entity
        PreservationActivity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));

        // 2. Update the Status
        activity.setStatus(status.toUpperCase());
        
        // 3. Save the changes
        PreservationActivity updatedActivity = activityRepository.save(activity);
        
        // 4. Log the audit action
        auditLogService.logAction(SecurityUtils.getCurrentUserId(), 
                "ACTIVITY_STATUS_UPDATE", RESOURCE_SITE, STATUS_SUCCESS);

        // 5. Return the clean Response DTO (Breaks the loop)
        return mapToActivityResponse(updatedActivity);
    }

    @Override
    public List<PreservationActivityResponse> getActivitiesBySite(Long siteId) {
        // 1. Check if site exists
        if (!siteRepository.existsById(siteId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Site not found");
        }

        // 2. Fetch activities and map them to the clean Response DTO
        List<PreservationActivity> activities = activityRepository.findBySite_SiteId(siteId);

        return activities.stream()
                .map(this::mapToActivityResponse) // Uses the helper method we created
                .toList();
    }

    @Override
    @Transactional
    public void deleteActivity(Long activityId) {
        if (!activityRepository.existsById(activityId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found");
        }
        activityRepository.deleteById(activityId);
        auditLogService.logAction(SecurityUtils.getCurrentUserId(), ACTION_ACTIVITY_DELETE, RESOURCE_SITE, STATUS_SUCCESS);
    }

    // --- Mapper Logic (The Loop-Breaker) ---
    private HeritageSiteResponse mapToSiteResponse(HeritageSite site) {
        HeritageSiteResponse res = new HeritageSiteResponse();
        res.setSiteId(site.getSiteId());
        res.setName(site.getName());
        res.setLocation(site.getLocation());
        res.setDescription(site.getDescription());
        res.setStatus(site.getStatus());
        
        // Map programs to ProgramSummary to prevent infinite looping
        if (site.getPrograms() != null) {
            res.setPrograms(site.getPrograms().stream()
                .map(p -> new ProgramSummary(p.getProgramId(), p.getTitle(), p.getStatus()))
                .toList());
        }
        
     // 2. Map Preservation Activities (NEW)
        if (site.getPreservationActivities() != null) {
            res.setPreservationActivities(site.getPreservationActivities().stream()
                    .map(this::mapToActivityResponse) // Uses your existing helper
                    .toList());
        }
        
        return res;
    }
    
    private PreservationActivityResponse mapToActivityResponse(PreservationActivity activity) {
        PreservationActivityResponse response = new PreservationActivityResponse();
        response.setActivityId(activity.getActivityId());
        response.setDescription(activity.getDescription());
        response.setDate(activity.getDate());
        response.setStatus(activity.getStatus());
        response.setCreatedAt(activity.getCreatedAt());
        
        // Set IDs instead of full objects to prevent recursion
        if (activity.getSite() != null) {
            response.setSiteId(activity.getSite().getSiteId());
        }
        if (activity.getOfficer() != null) {
            response.setOfficerId(activity.getOfficer().getUserId());
        }
        
        return response;
    }
}