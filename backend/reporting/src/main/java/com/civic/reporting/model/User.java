package com.civic.reporting.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

import com.civic.reporting.model.enumFolder.Role;

@Entity
@Table(name = "users")
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

    // New address/profile fields
    private String state;
    private String city;
    private String pincode;
    private String address;
    private String profile; // profile image URL

    private LocalDateTime createdAt;
}