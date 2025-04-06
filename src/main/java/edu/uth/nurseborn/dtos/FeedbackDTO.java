package edu.uth.nurseborn.dtos;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FeedbackDTO {
    private Integer feedbackId;
    private Integer bookingId;
    private Long familyUserId;
    private Long nurseUserId;
    private Integer rating;
    private String comment;
    private String response;
    private LocalDateTime createdAt;
}