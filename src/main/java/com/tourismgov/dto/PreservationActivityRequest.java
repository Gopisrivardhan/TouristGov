package com.tourismgov.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PreservationActivityRequest {

    @NotBlank(message = "Description of the activity is required")
    private String description;

    @NotNull(message = "Activity date is required")
    private LocalDate date;

    @NotBlank(message = "Status is required (e.g., PLANNED, COMPLETED)")
    private String status;
}