package com.civic.reporting.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
public class Department {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String description;
}