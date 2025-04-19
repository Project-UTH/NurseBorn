package edu.uth.nurseborn.dtos;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageDTO {
    private Integer messageId;
    private Long senderId;
    private Long receiverId;
    private Integer bookingId;
    private String senderUsername;
    private String receiverUsername;
    private String content;
    private String attachment;
    private LocalDateTime sentAt;
    private Boolean isRead;
}