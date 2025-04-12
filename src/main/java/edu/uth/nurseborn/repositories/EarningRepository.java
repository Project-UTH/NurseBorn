package edu.uth.nurseborn.repositories;

import edu.uth.nurseborn.models.Earning;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EarningRepository extends JpaRepository<Earning, Integer> {
    // Tìm tất cả thu nhập theo nurse_user_id
    List<Earning> findByNurseUserId(Long nurseUserId);

    // Tìm thu nhập theo booking_id
    Optional<Earning> findByBookingBookingId(Integer bookingId);
}