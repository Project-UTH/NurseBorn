package edu.uth.nurseborn.dtos;

import lombok.Data;

@Data
public class NurseProfileDTO {
    private Integer nurseProfileId;
    private Integer userId;
    private String location;
    private String skills;
    private Integer experienceYears;
    private String certifications;
    private Double hourlyRate;
    private String bio;
    private String profileImage;
    private boolean isApproved;
}