package com.civic.reporting.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

import com.civic.reporting.model.enumFolder.Category;
import com.civic.reporting.model.enumFolder.Status;

@Entity
@Data
public class Issue {

    @Id
    @GeneratedValue
    private Long id;

    private String title;
    private String description;
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Double latitude;
    private Double longitude;

    // Prediction / provenance metadata
    @Enumerated(EnumType.STRING)
    private Category predictedCategory;

    private Double predictionConfidence;

    private Double authenticityScore;

    private Double imageLatitude;
    private Double imageLongitude;

    private Double gpsDistanceMeters;

    @Lob
    private String visionResponseJson;

    private Double priorityScore;

    private LocalDateTime createdAt;
}