package edu.uth.nurseborn.dtos;

import edu.uth.nurseborn.models.enums.BookingStatus;
import edu.uth.nurseborn.models.enums.ServiceType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO để truyền dữ liệu thu nhập của y tá giữa các tầng.
 */
@Getter
@Setter
public class NurseIncomeDTO {

    private Long incomeId;
    private Long nurseUserId;
    private LocalDate bookingDate;
    private Double price;
    private ServiceType serviceType;
    private BookingStatus status;
}