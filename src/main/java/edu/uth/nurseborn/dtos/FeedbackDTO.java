package edu.uth.nurseborn.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackDTO {
    private Long bookingId; // ID của lịch đặt
    private int rating; // Điểm đánh giá (1-5)
    private String comment; // Phản hồi văn bản
}