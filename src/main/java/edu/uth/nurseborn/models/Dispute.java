package edu.uth.nurseborn.models;

import edu.uth.nurseborn.models.enums.DisputeStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Disputes")
public class Dispute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dispute_id")
    private Integer disputeId;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "raised_by_user_id", nullable = false)
    private User raisedBy;

    @Column(name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private DisputeStatus status = DisputeStatus.OPEN;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters, setters, constructors

    public Integer getDisputeId() {
        return disputeId;
    }

    public Booking getBooking() {
        return booking;
    }

    public User getRaisedBy() {
        return raisedBy;
    }

    public String getDescription() {
        return description;
    }

    public DisputeStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public void setRaisedBy(User raisedBy) {
        this.raisedBy = raisedBy;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(DisputeStatus status) {
        this.status = status;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Dispute() {}

    public Dispute(Booking booking, User raisedBy, String description, DisputeStatus status, LocalDateTime resolvedAt) {
        this.booking = booking;
        this.raisedBy = raisedBy;
        this.description = description;
        this.status = status;
        this.resolvedAt = resolvedAt;
    }
}
