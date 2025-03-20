package edu.uth.nurseborn.models;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(name="Reports")
public class Reports {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "admin_user_id", nullable = false)
    private Users adminUser;

    @Lob
    @Column(name = "report_type", nullable = false)
    private String reportType;

    @Lob
    @Column(name = "data")
    private String data;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "generated_at")
    private Instant generatedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Users getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(Users adminUser) {
        this.adminUser = adminUser;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }

}