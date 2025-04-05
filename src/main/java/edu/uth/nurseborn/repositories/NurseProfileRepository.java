package edu.uth.nurseborn.repositories;

import edu.uth.nurseborn.models.NurseProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NurseProfileRepository extends JpaRepository<NurseProfile, Long> {
    Optional<NurseProfile> findByUserUserId(Long userId);
}