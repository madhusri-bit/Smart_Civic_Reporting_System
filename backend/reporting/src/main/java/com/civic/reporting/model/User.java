package com.civic.reporting.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

import com.civic.reporting.model.enumFolder.Role;

@Entity
@Data
public class User {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Integer reputationScore;
    private String preferredLanguage;

    private LocalDateTime createdAt;
}