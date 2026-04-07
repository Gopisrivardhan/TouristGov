package com.tourismgov.service;

import com.tourismgov.dto.ProgramRequest;
import com.tourismgov.dto.ProgramResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface TourismProgramService {
    
    // Creates a new tourism program.
    ProgramResponse createProgram(ProgramRequest request);
    
    // Retrieves the details of a specific program by its ID.
    ProgramResponse getProgramById(Long programId);
    
    // Retrieves a list of all tourism programs.
    List<ProgramResponse> getAllPrograms();
    
    // Retrieves a paginated list of tourism programs.
    Page<ProgramResponse> getProgramsPaged(int page, int size);
    
    // Fully updates the details of an existing program.
    ProgramResponse updateProgram(Long programId, ProgramRequest request);
    
    // Updates the current status of a program (e.g., PLANNED, ACTIVE, COMPLETED).
    ProgramResponse updateProgramStatus(Long programId, String status);
    
    // Deletes a tourism program from the system.
    void deleteProgram(Long programId);

    // Generates a high-level budget report detailing total budget, spent funds, and remaining balance.
    Map<String, Object> getBudgetReport(Long programId);
}