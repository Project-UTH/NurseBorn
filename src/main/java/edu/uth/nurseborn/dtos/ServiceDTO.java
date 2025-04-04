package edu.uth.nurseborn.dtos;

import lombok.Data;

@Data
public class ServiceDTO {
    private Integer serviceId;
    private Integer nurseUserId;
    private String serviceName;
    private String description;
    private Double price;
    private String availabilitySchedule; // Có thể là JSON hoặc chuỗi định dạng
    private String status; // "active", "inactive"
}