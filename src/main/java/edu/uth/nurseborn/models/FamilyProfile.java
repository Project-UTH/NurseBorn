package edu.uth.nurseborn.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "FamilyProfiles")
public class FamilyProfile { // Đổi tên thành "FamilyProfile"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "family_profile_id")
    private Integer familyProfileId;

    @OneToOne // Sửa: ánh xạ quan hệ 1-1 với User
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "family_size")
    private Integer familySize;

    @Column(name = "specific_needs")
    private String specificNeeds;

    @Column(name = "preferred_location")
    private String preferredLocation;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Sửa: dùng LocalDateTime

    @PrePersist
    @PreUpdate // Thêm: tự động cập nhật updatedAt
    public void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters, setters

    public Integer getFamilyProfileId() {
        return familyProfileId;
    }

    public User getUser() {
        return user;
    }

    public Integer getFamilySize() {
        return familySize;
    }

    public String getSpecificNeeds() {
        return specificNeeds;
    }

    public String getPreferredLocation() {
        return preferredLocation;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setFamilySize(Integer familySize) {
        this.familySize = familySize;
    }

    public void setSpecificNeeds(String specificNeeds) {
        this.specificNeeds = specificNeeds;
    }

    public void setPreferredLocation(String preferredLocation) {
        this.preferredLocation = preferredLocation;
    }

    public FamilyProfile() {}

    public FamilyProfile(User user, Integer familySize, String specificNeeds, String preferredLocation) {
        this.user = user;
        this.familySize = familySize;
        this.specificNeeds = specificNeeds;
        this.preferredLocation = preferredLocation;
    }
}