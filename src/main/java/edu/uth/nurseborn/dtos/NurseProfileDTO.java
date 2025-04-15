package edu.uth.nurseborn.dtos;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class NurseProfileDTO {

    private Integer nurseProfileId;
    private Integer experienceYears;
    private Double hourlyRate;
    private Double dailyRate;
    private Double weeklyRate;
    private Boolean approved;
    private String location;
    private String skills;
    private String bio;
    private String profileImage;
    private LocalDateTime updatedAt;
    private Long userId;
    private List<CertificateDTO> certificates;
}