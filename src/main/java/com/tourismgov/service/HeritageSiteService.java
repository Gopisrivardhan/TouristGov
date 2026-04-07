package com.tourismgov.service;

import com.tourismgov.dto.*;
import java.util.List;

public interface HeritageSiteService {
    HeritageSiteResponse createSite(HeritageSiteRequest request);
    List<HeritageSiteResponse> getAllSites();
    HeritageSiteResponse getSiteById(Long siteId);
    HeritageSiteResponse updateSite(Long siteId, HeritageSiteRequest request);
    void deleteSite(Long siteId);
    
    PreservationActivityResponse updateActivityStatus(Long activityId, String status);
    List<PreservationActivityResponse> getActivitiesBySite(Long siteId);
    void deleteActivity(Long activityId);
    PreservationActivityResponse logActivity(Long siteId, PreservationActivityRequest request);
}