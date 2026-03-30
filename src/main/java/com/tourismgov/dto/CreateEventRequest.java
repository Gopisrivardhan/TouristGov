package com.tourismgov.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateEventRequest {
    @NotNull(message = "Site ID is required")
    private Long siteId;
    @NotBlank(message = "Title is required")
    private String title;
    private String location;
    @NotNull(message = "Date is required")
    private LocalDateTime date;
}