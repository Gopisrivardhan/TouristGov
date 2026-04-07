package com.tourismgov.repository;

import com.tourismgov.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    
    List<Resource> findByProgram_ProgramId(Long programId);

    // Useful for Service Layer: e.g., finding all "FUNDS" or "STAFF" allocated to a program
    List<Resource> findByProgram_ProgramIdAndType(Long programId, String type);

    // Useful for Service Layer: Finding available vs exhausted resources
    List<Resource> findByStatus(String status);
}