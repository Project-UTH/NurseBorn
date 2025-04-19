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

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "notes")
    private String notes;

    @Column(name = "price", nullable = false)
    private Double price;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private ServiceType serviceType;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status;

    @ManyToOne
    @JoinColumn(name = "family_user_id", nullable = false)
    private User familyUser;

    @ManyToOne
    @JoinColumn(name = "nurse_user_id", nullable = false)
    private User nurseUser;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}