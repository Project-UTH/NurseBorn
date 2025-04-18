package edu.uth.nurseborn.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "family_profiles")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FamilyProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "family_profile_id")
    private Integer familyProfileId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonBackReference
    private User user;

    @Column(name="child_name")
    private String childName;

    @Column(name="child_age")
    private String childAge;

    @Column(name = "specific_needs")
    private String specificNeeds;

    @Column(name = "preferred_location")
    private String preferredLocation;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and setters
    public Integer getFamilyProfileId() {
        return familyProfileId;
    }

    public User getUser() {
        return user;
    }

    public String getSpecificNeeds() {
        return specificNeeds;
    }

    public String getPreferredLocation() {
        return preferredLocation;
    }

    public String getChildAge() {
        return childAge;
    }

    public String getChildName() {
        return childName;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setFamilyProfileId(Integer familyProfileId) {
        this.familyProfileId = familyProfileId;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public void setChildAge(String childAge) {
        this.childAge = childAge;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setSpecificNeeds(String specificNeeds) {
        this.specificNeeds = specificNeeds;
    }

    public void setPreferredLocation(String preferredLocation) {
        this.preferredLocation = preferredLocation;
    }

    public FamilyProfile() {}

    public FamilyProfile(User user, String specificNeeds, String preferredLocation, String childAge, String childName) {
        this.user = user;
        this.preferredLocation = preferredLocation;
        this.specificNeeds = specificNeeds;
        this.childName = childName;
        this.childAge = childAge;
    }
}