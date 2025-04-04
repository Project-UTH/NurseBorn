package edu.uth.nurseborn.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "earnings")
public class Earning {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "earning_id")
    private Integer earningId;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "nurse_user_id", nullable = false)
    private User nurse;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "platform_fee", nullable = false)
    private Double platformFee = 0.0;

    @Column(name = "net_income", insertable = false, updatable = false)
    private Double netIncome; // GENERATED ALWAYS AS (amount - platform_fee)

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @PrePersist
    public void prePersist() {
        this.transactionDate = LocalDateTime.now();
    }

    // Getters, setters, constructors

    public Integer getEarningId() {
        return earningId;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public Double getNetIncome() {
        return netIncome;
    }

    public Double getPlatformFee() {
        return platformFee;
    }

    public Double getAmount() {
        return amount;
    }

    public User getNurse() {
        return nurse;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public void setNurse(User nurse) {
        this.nurse = nurse;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setPlatformFee(Double platformFee) {
        this.platformFee = platformFee;
    }

    public Earning() {}

    public Earning(Booking booking, User nurse, Double amount, Double platformFee) {
        this.booking = booking;
        this.nurse = nurse;
        this.amount = amount;
        this.platformFee = platformFee;
    }
}