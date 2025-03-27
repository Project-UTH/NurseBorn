package edu.uth.nurseborn.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "AdminActions")
public class AdminAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "action_id")
    private Integer actionId;

    @ManyToOne
    @JoinColumn(name = "admin_user_id", nullable = false)
    private User admin;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;

    @ManyToOne
    @JoinColumn(name = "target_user_id", nullable = false)
    private User target;

    @Column(name = "description")
    private String description;

    @Column(name = "action_date")
    private LocalDateTime actionDate;

    @PrePersist
    public void prePersist() {
        this.actionDate = LocalDateTime.now();
    }

    // Getters, setters, constructors

    public Integer getActionId() {
        return actionId;
    }

    public User getAdmin() {
        return admin;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public User getTarget() {
        return target;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getActionDate() {
        return actionDate;
    }

    public void setAdmin(User admin) {
        this.admin = admin;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public void setTarget(User target) {
        this.target = target;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AdminAction() {}

    public AdminAction(User admin, ActionType actionType, User target, String description) {
        this.admin = admin;
        this.actionType = actionType;
        this.target = target;
        this.description = description;
    }
}
enum ActionType {
    APPROVE_USER, RESOLVE_DISPUTE, OTHER
}