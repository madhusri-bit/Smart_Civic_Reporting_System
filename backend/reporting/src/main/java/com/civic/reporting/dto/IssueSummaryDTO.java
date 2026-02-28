package com.civic.reporting.dto;

import lombok.Data;

@Data
public class IssueSummaryDTO {
    private Long id;
    private String title;
    private String description;
    private String photoUrl;
    private String category;
    private String status;
    private Double severity;
    private long upvotes;
    private long comments;
}
