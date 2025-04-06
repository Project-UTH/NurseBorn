package edu.uth.nurseborn.dtos;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookingDTO {
    private Integer bookingId;
    private Long familyUserId;
    private Long nurseUserId;
    private Integer serviceId;
    private String serviceType; // "hourly", "daily", "weekly"
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // "pending", "accepted", "completed", "cancelled"
    private Double totalCost;
}