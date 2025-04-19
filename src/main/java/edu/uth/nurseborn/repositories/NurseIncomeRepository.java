package edu.uth.nurseborn.repositories;

import edu.uth.nurseborn.models.NurseIncome;
import edu.uth.nurseborn.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository để truy xuất dữ liệu thu nhập của y tá từ cơ sở dữ liệu.
 */
public interface NurseIncomeRepository extends JpaRepository<NurseIncome, Long> {

    /**
     * Tìm danh sách thu nhập theo y tá và khoảng thời gian.
     */
    List<NurseIncome> findByNurseUserAndBookingDateBetween(User nurseUser, LocalDate startDate, LocalDate endDate);

    /**
     * Tính tổng thu nhập của y tá trong khoảng thời gian với trạng thái ACCEPTED.
     */
    @Query("SELECT SUM(ni.price) FROM NurseIncome ni WHERE ni.nurseUser = :nurseUser AND ni.bookingDate BETWEEN :startDate AND :endDate AND ni.status = 'ACCEPTED'")
    Double sumIncomeByNurseUserAndDateRange(@Param("nurseUser") User nurseUser, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Đếm số lượng đặt lịch của y tá trong khoảng thời gian với trạng thái ACCEPTED.
     */
    @Query("SELECT COUNT(ni) FROM NurseIncome ni WHERE ni.nurseUser = :nurseUser AND ni.bookingDate BETWEEN :startDate AND :endDate AND ni.status = 'ACCEPTED'")
    Long countBookingsByNurseUserAndDateRange(@Param("nurseUser") User nurseUser, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}