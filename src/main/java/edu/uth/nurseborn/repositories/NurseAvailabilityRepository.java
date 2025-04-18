package edu.uth.nurseborn.repositories;

import edu.uth.nurseborn.models.NurseAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NurseAvailabilityRepository extends JpaRepository<NurseAvailability, Long> {
    List<NurseAvailability> findByNurseProfileNurseProfileId(Integer nurseProfileId);
    void deleteByNurseProfileNurseProfileId(Integer nurseProfileId);
}