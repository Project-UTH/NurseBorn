package edu.uth.nurseborn.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "web_income")
@Getter
@Setter
public class WebIncome {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "income_id")
    private Long incomeId;

    @Column(name = "booking_count", nullable = false)
    private Long bookingCount;

    @Column(name = "web_income", nullable = false)
    private Double webIncome;

    @Column(name = "nurse_income", nullable = false)
    private Double nurseIncome;

    @Column(name = "nurse_after_discount", nullable = false)
    private Double nurseAfterDiscount;

    @Column(name = "family_count", nullable = false)
    private Long familyCount;

    @Column(name = "nurse_count", nullable = false)
    private Long nurseCount;
}