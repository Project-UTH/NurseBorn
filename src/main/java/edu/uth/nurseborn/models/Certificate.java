package edu.uth.nurseborn.models;

import jakarta.persistence.*;

@Entity
@Table(name = "certificates")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "certificate_id")
    private Long certificateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nurse_profile_id")
    private NurseProfile nurseProfile;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "certificate_name")
    private String certificateName;

    public String getCertificateName() {
        return certificateName;
    }

    public void setCertificateName(String certificateName) {
        this.certificateName = certificateName;
    }

    // Getters and setters
    public Long getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(Long certificateId) {
        this.certificateId = certificateId;
    }

    public NurseProfile getNurseProfile() {
        return nurseProfile;
    }

    public void setNurseProfile(NurseProfile nurseProfile) {
        this.nurseProfile = nurseProfile;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

}