package edu.uth.nurseborn.models;

import edu.uth.nurseborn.models.enums.BookingStatus;
import edu.uth.nurseborn.models.enums.ServiceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_user_id", nullable = false)
    private User familyUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nurse_user_id", nullable = false)
    private User nurseUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private ServiceType serviceType; // HOURLY, DAILY, WEEKLY

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "notes")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status; // PENDING, ACCEPTED, COMPLETED, CANCELLED

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.status = BookingStatus.PENDING;
    }
}