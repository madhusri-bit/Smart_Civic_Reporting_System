package com.civic.reporting.dto;

import lombok.Data;

@Data
public class ReviewDTO {
    private boolean approved;
    private String comment;
}
