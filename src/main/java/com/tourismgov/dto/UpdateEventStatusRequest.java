package com.tourismgov.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateEventStatusRequest {
    @NotBlank(message = "Status is required")
    private String status;
}