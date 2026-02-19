package com.civic.reporting.model;

import com.civic.reporting.model.enumFolder.PhotoType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
public class IssuePhoto {

    @Id
    @GeneratedValue
    private Long id;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private PhotoType type;

    private Double ssimScore;

    @ManyToOne
    private Issue issue;
}