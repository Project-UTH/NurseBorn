package edu.uth.nurseborn.dtos;

public class RegisterRequestDTO {
    private UserDTO user;
    private NurseProfileDTO nurseProfile;
    private FamilyProfileDTO familyProfile;

    // Getters and setters
    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public NurseProfileDTO getNurseProfile() {
        return nurseProfile;
    }

    public void setNurseProfile(NurseProfileDTO nurseProfile) {
        this.nurseProfile = nurseProfile;
    }

    public FamilyProfileDTO getFamilyProfile() {
        return familyProfile;
    }

    public void setFamilyProfile(FamilyProfileDTO familyProfile) {
        this.familyProfile = familyProfile;
    }
}