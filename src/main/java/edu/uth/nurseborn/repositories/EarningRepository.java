package edu.uth.nurseborn.repositories;

import edu.uth.nurseborn.models.Booking;
import edu.uth.nurseborn.models.Earning;
import edu.uth.nurseborn.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EarningRepository extends JpaRepository<Earning, Integer> {

    // Tìm tất cả thu nhập theo nurse
    List<Earning> findByNurse(User nurse);

    // Tìm tất cả thu nhập theo booking
    List<Earning> findByBooking(Booking booking);

    // Tìm thu nhập theo nurse và booking kết hợp
    List<Earning> findByNurseAndBooking(User nurse, Booking booking);

    // Tìm tất cả thu nhập trong khoảng thời gian transactionDate
    List<Earning> findByTransactionDateBetween(LocalDateTime start, LocalDateTime end);

    // Tính tổng amount theo nurse
    @Query("SELECT SUM(e.amount) FROM Earning e WHERE e.nurse = :nurse")
    Double sumAmountByNurse(User nurse);

    // Tính tổng netIncome theo nurse
    @Query("SELECT SUM(e.netIncome) FROM Earning e WHERE e.nurse = :nurse")
    Double sumNetIncomeByNurse(User nurse);

    // Tính tổng platformFee theo nurse
    @Query("SELECT SUM(e.platformFee) FROM Earning e WHERE e.nurse = :nurse")
    Double sumPlatformFeeByNurse(User nurse);

    // Đếm số bản ghi thu nhập theo nurse
    Long countByNurse(User nurse);
}