package com.civic.reporting.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class IssueResolution {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Issue issue;

    private String resolvedPhotoUrl;
    private Double ssimScore;
    private Double gpsDistanceMeters;

    @Column(columnDefinition = "TEXT")
    private String visionSummary;

    private Boolean aiResolved;

    private LocalDateTime createdAt;
}
