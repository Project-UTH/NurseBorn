package edu.uth.nurseborn.dtos;

import lombok.Data;
import java.sql.Time;

@Data
public class NurseAvailabilityDTO {
    private Integer availabilityId;
    private Integer nurseProfileId;
    private String dayOfWeek; // "Monday", "Tuesday", etc.
    private Time startTime;
    private Time endTime;
}