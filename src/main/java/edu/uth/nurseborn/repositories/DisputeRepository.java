package edu.uth.nurseborn.repositories;

import edu.uth.nurseborn.models.Dispute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Integer> {

    // Tìm tất cả tranh chấp liên quan đến một booking
    List<Dispute> findByBookingBookingId(Integer bookingId);

    // Tìm tất cả tranh chấp do một người dùng khởi tạo
    List<Dispute> findByRaisedByUserId(Long raisedByUserId);

    // Tìm tranh chấp theo trạng thái (status)
    List<Dispute> findByStatus(String status);

    // Tìm tranh chấp theo người khởi tạo và trạng thái
    List<Dispute> findByRaisedByUserIdAndStatus(Long raisedByUserId, String status);
}
