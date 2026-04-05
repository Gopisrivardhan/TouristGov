package com.tourismgov.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class ProgramResponse {
    private Long programId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double budget;
    private String status;
    
    // NEW: Returns the IDs of the linked sites
    private List<Long> heritageSiteIds; 
}