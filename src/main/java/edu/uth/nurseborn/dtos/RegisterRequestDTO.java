package edu.uth.nurseborn.dtos;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class RegisterRequestDTO {
    private UserDTO user;
    private NurseProfileDTO nurseProfile;
    private FamilyProfileDTO familyProfile;
    private List<MultipartFile> certificates; // Danh sách file chứng chỉ

    // Getters and setters
    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public NurseProfileDTO getNurseProfile() {
        return nurseProfile;
    }

    public void setNurseProfile(NurseProfileDTO nurseProfile) {
        this.nurseProfile = nurseProfile;
    }

    public FamilyProfileDTO getFamilyProfile() {
        return familyProfile;
    }

    public void setFamilyProfile(FamilyProfileDTO familyProfile) {
        this.familyProfile = familyProfile;
    }

    public List<MultipartFile> getCertificates() {
        return certificates;
    }

    public void setCertificates(List<MultipartFile> certificates) {
        this.certificates = certificates;
    }
}