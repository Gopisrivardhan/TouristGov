package com.tourismgov.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HeritageSiteRequest {
    @NotBlank(message = "Site name is required")
    private String name;
    @NotBlank(message = "Location is required")
    private String location;
    private String description;
    private String status;
}