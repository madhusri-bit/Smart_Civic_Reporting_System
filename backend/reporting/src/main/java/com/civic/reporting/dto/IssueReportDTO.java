package com.civic.reporting.dto;

import lombok.*;

@Data
public class IssueReportDTO {
    private String title;
    private String description;
    private Double latitude;
    private Double longitude;
    private String photoUrl;
}