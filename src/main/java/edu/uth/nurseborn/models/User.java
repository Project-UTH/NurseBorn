package edu.uth.nurseborn.models;

import edu.uth.nurseborn.models.enums.Role;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User { // Đổi tên class thành "User" (số ít, chuẩn naming convention)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING) // Sửa: dùng enum thay vì String
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "created_at")
    private LocalDateTime createdAt; // Sửa: dùng LocalDateTime thay vì Timestamp

    @Column(name = "is_verified")
    private Boolean isVerified = false; // Thêm giá trị mặc định

    // Thêm quan hệ 1-1 với FamilyProfiles và NurseProfiles
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private FamilyProfile familyProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private NurseProfile nurseProfile;

    @PrePersist // Thêm: tự động gán createdAt
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters, setters


    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Boolean getVerified() {
        return isVerified;
    }

    public FamilyProfile getFamilyProfile() {
        return familyProfile;
    }

    public NurseProfile getNurseProfile() {
        return nurseProfile;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setVerified(Boolean verified) {
        isVerified = verified;
    }

    public void setFamilyProfile(FamilyProfile familyProfile) {
        this.familyProfile = familyProfile;
    }

    public void setNurseProfile(NurseProfile nurseProfile) {
        this.nurseProfile = nurseProfile;
    }

    public User() {}

    public User(NurseProfile nurseProfile, FamilyProfile familyProfile, Boolean isVerified, String address, String phoneNumber, String fullName, Role role, String email, String passwordHash, String username) {
        this.nurseProfile = nurseProfile;
        this.familyProfile = familyProfile;
        this.isVerified = isVerified;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.fullName = fullName;
        this.role = role;
        this.email = email;
        this.passwordHash = passwordHash;
        this.username = username;
    }
}

