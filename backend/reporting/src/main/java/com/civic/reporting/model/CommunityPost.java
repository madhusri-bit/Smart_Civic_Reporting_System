package com.civic.reporting.model;

import jakarta.persistence.Id;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import lombok.Data;

@Entity
@Data
public class CommunityPost {
    @Id
    @GeneratedValue
    private Long id;
    private String title;
    private String content;
}