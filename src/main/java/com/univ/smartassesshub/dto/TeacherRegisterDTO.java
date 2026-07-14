package com.univ.smartassesshub.dto;

import lombok.Data;

@Data
public class TeacherRegisterDTO {
    private String fullName;
    private String email;
    private String password;
    private String staffId; // Specific to teacher
    private String universitySecretKey; // High Security verification key field
}