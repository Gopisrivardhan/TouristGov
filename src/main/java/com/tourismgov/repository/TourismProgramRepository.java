package com.tourismgov.repository;

import com.tourismgov.model.TourismProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TourismProgramRepository extends JpaRepository<TourismProgram, Long> {

    // Validation: Ensures no two programs share the same name (ignoring case is safer)
    boolean existsByTitleIgnoreCase(String title);

    Optional<TourismProgram> findByTitle(String title);

    // Useful for Service Layer: Fetching only "ACTIVE" or "COMPLETED" programs
    List<TourismProgram> findByStatus(String status);

    // Useful for Service Layer: Finding programs currently running
    List<TourismProgram> findByStartDateBeforeAndEndDateAfter(LocalDate date1, LocalDate date2);
}