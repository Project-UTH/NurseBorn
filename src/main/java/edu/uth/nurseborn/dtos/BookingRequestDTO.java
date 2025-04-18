package edu.uth.nurseborn.dtos;

import edu.uth.nurseborn.models.enums.ServiceType;
import edu.uth.nurseborn.models.enums.BookingStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class BookingRequestDTO {
    private Long familyUserId;
    private Long nurseUserId;
    private ServiceType serviceType;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Double price;
    private String notes;
    private BookingStatus status;
}