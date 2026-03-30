package com.tourismgov.service;

import com.tourismgov.dto.DashboardDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Interface for Real-Time Dashboard Data.
 * Focuses on providing 7-8 specific metrics based on the User's Role.
 */
public interface DashboardService {

    /**
     * Aggregates 7-8 real-time quantities for the specific user role.
     * @param role The role of the user (TOURIST, OFFICER, MANAGER, etc.)
     * @param userId The unique ID of the authenticated user
     * @return DashboardDTO containing personalized metrics
     */
    DashboardDTO getDashboardMetrics(
        @NotBlank(message = "Role is required") String role, 
        @NotNull(message = "User ID is required") Long userId
    );
}