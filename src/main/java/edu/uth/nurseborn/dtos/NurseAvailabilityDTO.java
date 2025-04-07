package edu.uth.nurseborn.dtos;

public class NurseAvailabilityDTO {
    private Integer availabilityId; // Khớp với NurseAvailability
    private Long nurseProfileId;    // Khớp với NurseProfile
    private String dayOfWeek;
    private String startTime;
    private String endTime;

    public NurseAvailabilityDTO() {}

    public NurseAvailabilityDTO(Integer availabilityId, Long nurseProfileId, String dayOfWeek, 
                               String startTime, String endTime) {
        this.availabilityId = availabilityId;
        this.nurseProfileId = nurseProfileId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Integer getAvailabilityId() {
        return availabilityId;
    }

    public void setAvailabilityId(Integer availabilityId) {
        this.availabilityId = availabilityId;
    }

    public Long getNurseProfileId() {
        return nurseProfileId;
    }

    public void setNurseProfileId(Long nurseProfileId) {
        this.nurseProfileId = nurseProfileId;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}