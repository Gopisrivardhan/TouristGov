package com.tourismgov.repository;

import com.tourismgov.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    // Core relations
    List<Event> findBySite_SiteId(Long siteId);
    List<Event> findByProgram_ProgramId(Long programId);

    // Useful for Service Layer: Fetching events for a specific program at a specific site
    List<Event> findBySite_SiteIdAndProgram_ProgramId(Long siteId, Long programId);

    // Useful for Service Layer: Filtering active vs cancelled events (List)
    List<Event> findByStatus(String status);

    // THE MISSING PIECE: Used by getEventsPaged in the Service Layer
    Page<Event> findByStatusIgnoreCase(String status, Pageable pageable);

    // Useful for Service Layer: Finding upcoming events
    List<Event> findByDateAfter(LocalDateTime date);
}