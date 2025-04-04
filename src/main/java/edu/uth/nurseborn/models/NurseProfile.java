package edu.uth.nurseborn.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "nurse_profiles")
public class NurseProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nurse_profile_id")
    private Integer nurseProfileId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "skills", nullable = false)
    private String skills;

    @Column(name = "experience_years", nullable = false)
    private Integer experienceYears;

    @Column(name = "certifications")
    private String certifications;

    @Column(name = "hourly_rate", nullable = false)
    private Double hourlyRate;

    @Column(name = "bio")
    private String bio;

    @Column(name = "profile_image")
    private String profileImage = "default_profile.jpg";

    @Column(name = "is_approved")
    private Boolean isApproved = false;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "nurseProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NurseAvailability> nurseAvailabilities;

    @PrePersist
    @PreUpdate
    public void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters, setters

    public Integer getNurseProfileId() {
        return nurseProfileId;
    }

    public User getUser() {
        return user;
    }

    public String getLocation() {
        return location;
    }

    public String getSkills() {
        return skills;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public String getCertifications() {
        return certifications;
    }

    public Double getHourlyRate() {
        return hourlyRate;
    }

    public String getBio() {
        return bio;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public Boolean getApproved() {
        return isApproved;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<NurseAvailability> getNurseAvailabilities() {
        return nurseAvailabilities;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public void setCertifications(String certifications) {
        this.certifications = certifications;
    }

    public void setHourlyRate(Double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void setApproved(Boolean approved) {
        isApproved = approved;
    }

    public void setNurseAvailabilities(List<NurseAvailability> nurseAvailabilities) {
        this.nurseAvailabilities = nurseAvailabilities;
    }

    public NurseProfile() {}

    public NurseProfile(User user, String location, String skills, Integer experienceYears, String certifications, Double hourlyRate, String bio, String profileImage, Boolean isApproved, List<NurseAvailability> nurseAvailabilities) {
        this.user = user;
        this.location = location;
        this.skills = skills;
        this.experienceYears = experienceYears;
        this.certifications = certifications;
        this.hourlyRate = hourlyRate;
        this.bio = bio;
        this.profileImage = profileImage;
        this.isApproved = isApproved;
        this.nurseAvailabilities = nurseAvailabilities;
    }
}
