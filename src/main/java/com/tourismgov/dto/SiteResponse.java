package com.tourismgov.dto;

import lombok.Data;

@Data
public class SiteResponse {
    private Long siteId;
    private String name;
    private String location;
    private String description;
    private String status;
}