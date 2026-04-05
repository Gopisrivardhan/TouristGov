package com.tourismgov.service;

import com.tourismgov.dto.TouristRequest;
import com.tourismgov.dto.TouristResponse;
import com.tourismgov.dto.TouristSummaryResponse;
import com.tourismgov.dto.TouristUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TouristService {
    TouristResponse createTourist(TouristRequest request);
    TouristResponse getTouristById(Long touristId);
    TouristResponse updateTourist(Long touristId, TouristUpdateRequest request);
    void deleteTourist(Long touristId);
    Page<TouristSummaryResponse> getTouristSummaries(Pageable pageable);
}