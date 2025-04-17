package edu.uth.nurseborn.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "nurse_profiles")
@Getter
@Setter
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

    @Column(name = "hourly_rate", nullable = false)
    private Double hourlyRate;

    @Column(name = "daily_rate")
    private Double dailyRate;

    @Column(name = "weekly_rate")
    private Double weeklyRate;

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

    @OneToMany(mappedBy = "nurseProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Certificate> certificates;


    @PrePersist
    @PreUpdate
    public void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

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

    public Double getHourlyRate() {
        return hourlyRate;
    }

    public Double getDailyRate() {
        return dailyRate;
    }

    public Double getWeeklyRate() {
        return weeklyRate;
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

    public List<Certificate> getCertificates() {
        return certificates;
    }

    public void setNurseProfileId(Integer nurseProfileId) {
        this.nurseProfileId = nurseProfileId;
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

    public void setHourlyRate(Double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public void setDailyRate(Double dailyRate) {
        this.dailyRate = dailyRate;
    }

    public void setWeeklyRate(Double weeklyRate) {
        this.weeklyRate = weeklyRate;
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

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setNurseAvailabilities(List<NurseAvailability> nurseAvailabilities) {
        this.nurseAvailabilities = nurseAvailabilities;
    }

}