package edu.uth.nurseborn.models;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(name = "AdminActions")
public class AdminActions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "action_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "admin_user_id", nullable = false)
    private edu.uth.nurseborn.models.Users adminUser;

    @Lob
    @Column(name = "action_type", nullable = false)
    private String actionType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "target_user_id", nullable = false)
    private edu.uth.nurseborn.models.Users targetUser;

    @Lob
    @Column(name = "description")
    private String description;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "action_date")
    private Instant actionDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public edu.uth.nurseborn.models.Users getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(edu.uth.nurseborn.models.Users adminUser) {
        this.adminUser = adminUser;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public edu.uth.nurseborn.models.Users getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(edu.uth.nurseborn.models.Users targetUser) {
        this.targetUser = targetUser;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getActionDate() {
        return actionDate;
    }

    public void setActionDate(Instant actionDate) {
        this.actionDate = actionDate;
    }

}