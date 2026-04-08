package com.tourismgov.repository;

import com.tourismgov.model.PreservationActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreservationActivityRepository extends JpaRepository<PreservationActivity, Long> {
    
    // Core relation: Fetch all maintenance activities for a specific physical site
    List<PreservationActivity> findBySite_SiteId(Long siteId);

    // THE FIX: Required by the Service layer to fetch tasks assigned to a specific officer
    List<PreservationActivity> findByOfficer_UserId(Long officerId);

    // Useful for Service Layer: Filter tasks by their current state (e.g., IN_PROGRESS, COMPLETED)
    List<PreservationActivity> findByStatusIgnoreCase(String status);
}