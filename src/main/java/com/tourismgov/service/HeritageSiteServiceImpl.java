package com.tourismgov.service;

import com.tourismgov.dto.HeritageSiteRequest;
import com.tourismgov.dto.PreservationActivityRequest;
import com.tourismgov.model.HeritageSite;
import com.tourismgov.model.PreservationActivity;
import com.tourismgov.model.User;
import com.tourismgov.repository.HeritageSiteRepository;
import com.tourismgov.repository.PreservationActivityRepository;
import com.tourismgov.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class HeritageSiteServiceImpl implements HeritageSiteService {

    private final HeritageSiteRepository siteRepository;
    private final PreservationActivityRepository activityRepository;
    private final UserRepository userRepository; // Added to verify the Officer

    public HeritageSiteServiceImpl(HeritageSiteRepository siteRepository, 
                                   PreservationActivityRepository activityRepository,
                                   UserRepository userRepository) {
        this.siteRepository = siteRepository;
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
    }

    // <--- Site Management --->

    @Override
    @Transactional
    public HeritageSite createSite(HeritageSiteRequest request) {
        HeritageSite site = new HeritageSite();
        site.setName(request.getName());
        site.setLocation(request.getLocation());
        site.setDescription(request.getDescription());
        site.setStatus("OPEN"); 
        return siteRepository.save(site);
    }

    @Override
    public List<HeritageSite> getAllSites() {
        return siteRepository.findAll();
    }

    @Override
    public HeritageSite getSiteById(Long siteId) {
        return siteRepository.findById(siteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Heritage Site not found with ID: " + siteId));
    }

    @Override
    @Transactional
    public HeritageSite updateSite(Long siteId, HeritageSiteRequest request) {
        HeritageSite site = getSiteById(siteId);
        site.setName(request.getName());
        site.setLocation(request.getLocation());
        site.setDescription(request.getDescription());
        
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            site.setStatus(request.getStatus().toUpperCase());
        }
        return siteRepository.save(site);
    }

    @Override
    @Transactional
    public void deleteSite(Long siteId) {
        HeritageSite site = getSiteById(siteId);
        siteRepository.delete(site);
    }

    // --- Preservation Activities ---

    @Override
    @Transactional
    public PreservationActivity logActivity(Long siteId, PreservationActivityRequest request) {
        HeritageSite site = getSiteById(siteId);
        
        // Ensure the officer actually exists in the database
        User officer = userRepository.findById(request.getOfficerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Officer not found"));
        
        PreservationActivity activity = new PreservationActivity();
        activity.setSite(site); 
        activity.setOfficer(officer); // Properly linking the User entity
        activity.setDescription(request.getDescription());
        activity.setDate(request.getDate());
        
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            activity.setStatus(request.getStatus().toUpperCase());
        } else {
            activity.setStatus("IN_PROGRESS");
        }
        
        return activityRepository.save(activity);
    }

    @Override
    public List<PreservationActivity> getActivitiesBySite(Long siteId) {
        getSiteById(siteId); // Verify site exists
        return activityRepository.findBySite_SiteId(siteId); // Using the corrected JPA method
    }

    @Override
    @Transactional
    public void deleteActivity(Long activityId) {
        PreservationActivity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));
        activityRepository.delete(activity);
    }
}