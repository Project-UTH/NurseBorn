package edu.uth.nurseborn.models;

import edu.uth.nurseborn.models.enums.ReportType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Reports")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Integer reportId;

    @ManyToOne
    @JoinColumn(name = "admin_user_id", nullable = false)
    private User admin;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private ReportType reportType;

    @Column(name = "data")
    private String data;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @PrePersist
    public void prePersist() {
        this.generatedAt = LocalDateTime.now();
    }

    // Getters, setters, constructors


    public Integer getReportId() {
        return reportId;
    }

    public User getAdmin() {
        return admin;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public String getData() {
        return data;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setAdmin(User admin) {
        this.admin = admin;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Report() {}

    public Report(User admin, ReportType reportType, String data) {
        this.admin = admin;
        this.reportType = reportType;
        this.data = data;
    }
}
