package edu.uth.nurseborn.dtos;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DisputeDTO {
    private Integer disputeId;
    private Integer bookingId;
    private Integer raisedByUserId;
    private String description;
    private String status; // "open", "resolved", "closed"
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}