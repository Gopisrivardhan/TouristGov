package com.tourismgov.dto;

import lombok.Data;

@Data
public class ResourceResponse {
    private Long resourceId;
    private Long programId;
    private String type;
    private Double quantity;
    private String status;
}