package edu.uth.nurseborn.dtos;

import lombok.Data;

@Data
public class FamilyProfileDTO {
    private Integer familyProfileId;
    private Long userId;
    private String specificNeeds;
    private String preferredLocation;
    private String childName;
    private String childAge;
}