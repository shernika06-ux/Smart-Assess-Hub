package com.univ.smartassesshub.dto;

import lombok.Data;

@Data
public class StudentRegisterDTO {
    private String fullName;
    private String email;
    private String password;
    private String rollNumber; // Specific to student
}