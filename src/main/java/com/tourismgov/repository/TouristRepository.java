package com.tourismgov.repository;

import com.tourismgov.model.Tourist;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TouristRepository extends JpaRepository<Tourist, Long> {
	Optional<Tourist> findByContactInfo(String contactInfo);
	Page<Tourist> findAll(Pageable pageable);
}