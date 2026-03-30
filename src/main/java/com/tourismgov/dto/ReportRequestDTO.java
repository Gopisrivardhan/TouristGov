package com.tourismgov.dto;

import com.tourismgov.enums.ReportScope;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportRequestDTO {
    @NotNull(message = "Scope is required")
    private ReportScope scope;

    @NotNull(message = "Requester ID is required")
    private Long requesterId;
}