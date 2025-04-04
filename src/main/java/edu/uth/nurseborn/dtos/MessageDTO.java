package edu.uth.nurseborn.dtos;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageDTO {
    private Integer messageId;
    private Integer senderId;
    private Integer receiverId;
    private Integer bookingId;
    private String content;
    private String attachment;
    private LocalDateTime sentAt;
    private boolean isRead;
}