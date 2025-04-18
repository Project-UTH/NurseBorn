package edu.uth.nurseborn.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "nurse_availabilities")
@Getter
@Setter
public class NurseAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "availability_id")
    private Long availabilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nurse_profile_id", nullable = false)
    private NurseProfile nurseProfile;

    @Column(name = "day_of_week", nullable = false)
    private String dayOfWeek;

    public NurseAvailability() {}

    public NurseAvailability(NurseProfile nurseProfile, String dayOfWeek) {
        this.nurseProfile = nurseProfile;
        this.dayOfWeek = dayOfWeek;
    }
}