package com.civic.reporting.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Location {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;

    private Double latitude;
    private Double longitude;

    private LocalDateTime capturedAt;
}
