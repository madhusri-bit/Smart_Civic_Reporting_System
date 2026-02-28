package com.civic.reporting.dto;

import lombok.Data;

@Data
public class DepartmentUserDTO {
    private String name;
    private String email;
    private String password;
    private String role; // OFFICER or HEAD
    private Long departmentId;
}
