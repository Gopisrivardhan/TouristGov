package com.tourismgov.service;

import com.tourismgov.dto.HeritageSiteRequest;
import com.tourismgov.dto.PreservationActivityRequest;
import com.tourismgov.model.HeritageSite;
import com.tourismgov.model.PreservationActivity;

import java.util.List;

public interface HeritageSiteService {
    // --- Site Management ---
    HeritageSite createSite(HeritageSiteRequest request);
    List<HeritageSite> getAllSites();
    HeritageSite getSiteById(Long siteId);
    HeritageSite updateSite(Long siteId, HeritageSiteRequest request);
    void deleteSite(Long siteId); // NEW

    // --- Preservation Activities ---
    PreservationActivity logActivity(Long siteId, PreservationActivityRequest request);
    List<PreservationActivity> getActivitiesBySite(Long siteId);
    void deleteActivity(Long activityId); // NEW
}