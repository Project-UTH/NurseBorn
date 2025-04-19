package edu.uth.nurseborn.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Người nhận thông báo

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}