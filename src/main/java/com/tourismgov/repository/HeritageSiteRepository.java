package com.tourismgov.repository;

import com.tourismgov.model.HeritageSite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HeritageSiteRepository extends JpaRepository<HeritageSite, Long> {

    // Validation: Prevent duplicate site registrations
    boolean existsByNameIgnoreCase(String name);
    
    Optional<HeritageSite> findByName(String name);

    // Useful for Service Layer: Searching for sites in a specific city/region
    List<HeritageSite> findByLocationContainingIgnoreCase(String location);

    // Useful for Service Layer: Filtering by status (e.g., "OPEN", "UNDER_MAINTENANCE")
    List<HeritageSite> findByStatus(String status);
}