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
    private Boolean isVerified; // Thêm trường này
    private String location;
    private String skills;
    private String bio;
    private String profileImage;
    private LocalDateTime updatedAt;
    private Long userId;
    private String fullName;
    private String certificateNames; // Thêm trường này
    private List<CertificateDTO> certificates;

    private String username;
    private String email;
    private String phoneNumber;
    private String address;
    private String role;

    // Getter và Setter đã được tạo tự động bởi @Getter và @Setter của Lombok
}