package com.tourismgov.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingRequest {

    @NotNull(message = "Tourist ID is required")
    private Long touristId;

    private String status;
}