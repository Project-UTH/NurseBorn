package edu.uth.nurseborn.dtos;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EarningDTO {
    private Integer earningId;
    private Integer bookingId;
    private Integer nurseUserId;
    private Double amount;
    private Double platformFee;
    private Double netIncome;
    private LocalDateTime transactionDate;
}