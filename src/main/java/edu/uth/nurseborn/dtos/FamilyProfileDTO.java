package edu.uth.nurseborn.dtos;

import lombok.Data;

@Data
public class FamilyProfileDTO {
    private Integer familyProfileId;
    private Long userId;
    private Integer familySize;
    private String specificNeeds;
    private String preferredLocation;
}