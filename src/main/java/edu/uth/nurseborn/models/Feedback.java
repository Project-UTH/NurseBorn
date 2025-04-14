package edu.uth.nurseborn.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long feedbackId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_user_id")
    private User family; // Người gửi feedback (gia đình)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nurse_user_id")
    private User nurse; // Người nhận feedback (y tá)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking; // Lịch đặt liên quan đến feedback

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "comment")
    private String comment;

    @Column(name = "response")
    private String response;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(Long feedbackId) {
        this.feedbackId = feedbackId;
    }

    public User getFamily() {
        return family;
    }

    public void setFamily(User family) {
        this.family = family;
    }

    public User getNurse() {
        return nurse;
    }

    public void setNurse(User nurse) {
        this.nurse = nurse;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}