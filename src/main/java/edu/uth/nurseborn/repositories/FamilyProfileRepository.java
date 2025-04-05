package edu.uth.nurseborn.repositories;

import edu.uth.nurseborn.models.FamilyProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyProfileRepository extends JpaRepository<FamilyProfile, Long> {
    Optional<FamilyProfile> findByUserUserId(Long userId);
}