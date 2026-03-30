package com.tourismgov.repository;

import com.tourismgov.model.Booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    // Looks inside 'event' for 'eventId'
    List<Booking> findByEvent_EventId(Long eventId); 
    
    // Looks inside 'tourist' for 'touristId'
    List<Booking> findByTourist_TouristId(Long touristId);
    
    Page<Booking> findByEvent_EventId(Long eventId,Pageable pageable);
}