package edu.uth.nurseborn.dtos;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminActionDTO {
    private Integer actionId;
    private Integer adminUserId;
    private String actionType; // "approve_user", "resolve_dispute", "other"
    private Integer targetUserId;
    private String description;
    private LocalDateTime actionDate;
}