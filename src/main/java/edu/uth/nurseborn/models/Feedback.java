package edu.uth.nurseborn.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Integer feedbackId;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "family_user_id", nullable = false)
    private User family;

    @ManyToOne
    @JoinColumn(name = "nurse_user_id", nullable = false)
    private User nurse;

    @Column(name = "rating")
    private Integer rating; // CHECK (rating >= 1 AND rating <= 5) sẽ xử lý trong service

    @Column(name = "comment")
    private String comment;

    @Column(name = "response")
    private String response;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters, setters, constructors

    public Integer getFeedbackId() {
        return feedbackId;
    }

    public Booking getBooking() {
        return booking;
    }

    public User getFamily() {
        return family;
    }

    public User getNurse() {
        return nurse;
    }

    public Integer getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getResponse() {
        return response;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public void setFamily(User family) {
        this.family = family;
    }

    public void setNurse(User nurse) {
        this.nurse = nurse;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Feedback() {}

    public Feedback(Booking booking, User family, User nurse, Integer rating, String comment, String response) {
        this.booking = booking;
        this.family = family;
        this.nurse = nurse;
        this.rating = rating;
        this.comment = comment;
        this.response = response;
    }
}