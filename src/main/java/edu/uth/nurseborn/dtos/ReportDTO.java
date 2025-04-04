package edu.uth.nurseborn.dtos;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReportDTO {
    private Integer reportId;
    private Integer adminUserId;
    private String reportType; // "service_demand", "user_activity", "platform_performance"
    private String data; // Có thể là JSON hoặc chuỗi dữ liệu
    private LocalDateTime generatedAt;
}