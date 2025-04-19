package edu.uth.nurseborn.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class BookingDTO {
    private Long nurseUserId;
    private String serviceType; // HOURLY, DAILY, WEEKLY
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Double price;
    private String notes;
}