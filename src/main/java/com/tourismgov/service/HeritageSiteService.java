package com.tourismgov.service;

import com.tourismgov.dto.HeritageSiteRequest;
import com.tourismgov.dto.HeritageSiteResponse;
import java.util.List;

public interface HeritageSiteService {
    HeritageSiteResponse createSite(HeritageSiteRequest request);
    List<HeritageSiteResponse> getAllSites();
    HeritageSiteResponse getSiteById(Long siteId);
    HeritageSiteResponse updateSite(Long siteId, HeritageSiteRequest request);
    void deleteSite(Long siteId);
}