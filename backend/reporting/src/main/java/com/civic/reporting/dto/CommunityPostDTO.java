package com.civic.reporting.dto;

import lombok.Data;

@Data
public class CommunityPostDTO {
    private Long issueId;
    private String title;
    private String content;
}
