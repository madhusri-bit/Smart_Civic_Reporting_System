package com.civic.reporting.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String name;
    private String email;
    private String password;
    private String role;
    private String preferredLanguage;
    private String state;
    private String city;
    private String pincode;
    private String address;
    private String profile;
}
