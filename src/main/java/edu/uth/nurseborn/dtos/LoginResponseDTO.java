package edu.uth.nurseborn.dtos;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String token;
    private String username;
    private String role;
    private String email;
    private String fullName;
    private NurseProfileDTO nurseProfile;
    private FamilyProfileDTO familyProfile;
}