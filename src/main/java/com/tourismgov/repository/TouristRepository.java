package com.tourismgov.repository;

import com.tourismgov.model.Tourist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TouristRepository extends JpaRepository<Tourist, Long> {
    
    // Used during registration to prevent duplicate phone numbers
    Optional<Tourist> findByContactInfo(String contactInfo);

    // CRITICAL FOR SECURITY: Finds a tourist profile using the logged-in User's ID
    Optional<Tourist> findByUser_UserId(Long userId);
    
    // Used to quickly verify if a User already has a Tourist profile attached
    boolean existsByUser_UserId(Long userId);
}