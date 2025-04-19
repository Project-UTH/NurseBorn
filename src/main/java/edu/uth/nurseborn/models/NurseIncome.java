package edu.uth.nurseborn.models;

import edu.uth.nurseborn.models.enums.BookingStatus;
import edu.uth.nurseborn.models.enums.ServiceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Lớp mô hình đại diện cho thu nhập của y tá.
 */
@Entity
@Table(name = "nurse_incomes")
@Getter
@Setter
public class NurseIncome {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "income_id")
    private Long incomeId;

    @ManyToOne
    @JoinColumn(name = "nurse_user_id", nullable = false)
    private User nurseUser;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "price", nullable = false)
    private Double price;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private ServiceType serviceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status;
}