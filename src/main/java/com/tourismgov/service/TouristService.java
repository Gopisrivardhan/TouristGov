package com.tourismgov.service;

import java.util.List;
import com.tourismgov.dto.TouristRequest;
import com.tourismgov.dto.TouristResponse;

public interface TouristService {

    /**
     * Registers a new tourist and saves their profile.
     */
    TouristResponse createTourist(TouristRequest request);

    /**
     * Retrieves a single tourist profile by ID.
     */
    TouristResponse getTouristById(Long touristId);

    /**
     * Updates an existing tourist profile.
     */
    TouristResponse updateTourist(Long touristId, TouristRequest request);

    /**
     * Deletes a tourist and all associated records.
     */
    void deleteTourist(Long touristId);

    /**
     * Retrieves a list of all registered tourists.
     */
    List<TouristResponse> getAllTourists();
}