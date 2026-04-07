package com.tourismgov.repository;

import com.tourismgov.model.TourismProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TourismProgramRepository extends JpaRepository<TourismProgram, Long> {
}