package edu.uth.nurseborn.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NurseAvailabilityDTO {
    private Long userId;
    private List<String> selectedDays;
}