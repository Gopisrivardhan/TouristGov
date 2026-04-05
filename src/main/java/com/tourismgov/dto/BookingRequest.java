package com.tourismgov.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingRequest {
	@NotNull(message="Enter the tourist id")
	private Long touristId;
    private String status;
}