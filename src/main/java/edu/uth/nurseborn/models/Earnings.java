package edu.uth.nurseborn.models;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
public class Earnings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "earning_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "booking_id", nullable = false)
    private Bookings booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "nurse_user_id", nullable = false)
    private Users nurseUser;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @ColumnDefault("0.00")
    @Column(name = "platform_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal platformFee;

    @ColumnDefault("(`amount` - `platform_fee`)")
    @Column(name = "net_income", precision = 10, scale = 2)
    private BigDecimal netIncome;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "transaction_date")
    private Instant transactionDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Bookings getBooking() {
        return booking;
    }

    public void setBooking(Bookings booking) {
        this.booking = booking;
    }

    public Users getNurseUser() {
        return nurseUser;
    }

    public void setNurseUser(Users nurseUser) {
        this.nurseUser = nurseUser;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getPlatformFee() {
        return platformFee;
    }

    public void setPlatformFee(BigDecimal platformFee) {
        this.platformFee = platformFee;
    }

    public BigDecimal getNetIncome() {
        return netIncome;
    }

    public void setNetIncome(BigDecimal netIncome) {
        this.netIncome = netIncome;
    }

    public Instant getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Instant transactionDate) {
        this.transactionDate = transactionDate;
    }

}