package edu.uth.nurseborn.dtos;

import java.time.LocalDateTime;

public class LoginResponseDTO {
    private Long userId;
    private String username;
    private String email;
    private String role;
    private String fullName;
    private String phoneNumber;
    private String address;
    private boolean isVerified;
    private LocalDateTime createdAt;
    private String token;
    private NurseProfileDTO nurseProfile;
    private FamilyProfileDTO familyProfile;

    // Getters and setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        this.isVerified = verified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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
}