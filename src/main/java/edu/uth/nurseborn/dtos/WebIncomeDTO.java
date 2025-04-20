package edu.uth.nurseborn.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WebIncomeDTO {
    private Long bookingCount;
    private Double webIncome;
    private Double nurseIncome;
    private Double nurseAfterDiscount;
    private Long familyCount;
    private Long nurseCount;
    private List<String> chartLabels;
    private List<Double> chartData;
    private Long todayBookingCount;
    private Double todayWebIncome;
    private Double todayNurseIncome;
    private Double todayNurseAfterDiscount;
    private String filterType;  // Thêm trường filterType
    private String filterValue; // Thêm trường filterValue
}