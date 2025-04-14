package edu.uth.nurseborn.repositories;

import edu.uth.nurseborn.models.Earning;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EarningRepository extends JpaRepository<Earning, Integer> {
    List<Earning> findByNurseUserId(Long nurseUserId);
    Optional<Earning> findByBookingBookingId(Integer bookingId);
}