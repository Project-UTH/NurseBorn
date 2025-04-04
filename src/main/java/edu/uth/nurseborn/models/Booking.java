package edu.uth.nurseborn.models;

import edu.uth.nurseborn.models.enums.BookingStatus;
import edu.uth.nurseborn.models.enums.ServiceType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Integer bookingId;

    @ManyToOne
    @JoinColumn(name = "family_user_id", nullable = false)
    private User family;

    @ManyToOne
    @JoinColumn(name = "nurse_user_id", nullable = false)
    private User nurse;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private ServiceType serviceType;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "total_cost", nullable = false)
    private Double totalCost;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters, setters

    public Integer getBookingId() {
        return bookingId;
    }

    public User getFamily() {
        return family;
    }

    public User getNurse() {
        return nurse;
    }

    public Service getService() {
        return service;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setFamily(User family) {
        this.family = family;
    }

    public void setNurse(User nurse) {
        this.nurse = nurse;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }

    public Booking() {}

    public Booking(User family, User nurse, Service service, ServiceType serviceType, LocalDateTime startTime, LocalDateTime endTime, BookingStatus status, Double totalCost) {
        this.family = family;
        this.nurse = nurse;
        this.service = service;
        this.serviceType = serviceType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.totalCost = totalCost;
    }
}

