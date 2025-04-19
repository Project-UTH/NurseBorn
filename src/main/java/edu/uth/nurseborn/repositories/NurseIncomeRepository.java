package edu.uth.nurseborn.repositories;

import edu.uth.nurseborn.models.NurseIncome;
import edu.uth.nurseborn.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface NurseIncomeRepository extends JpaRepository<NurseIncome, Long> {

    List<NurseIncome> findByNurseUserAndBookingDateBetween(User nurseUser, LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(ni.price) FROM NurseIncome ni WHERE ni.nurseUser = :nurseUser AND ni.bookingDate BETWEEN :startDate AND :endDate AND ni.status = 'COMPLETED'")
    Double sumIncomeByNurseUserAndDateRange(@Param("nurseUser") User nurseUser, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(ni) FROM NurseIncome ni WHERE ni.nurseUser = :nurseUser AND ni.bookingDate BETWEEN :startDate AND :endDate AND ni.status = 'COMPLETED'")
    Long countBookingsByNurseUserAndDateRange(@Param("nurseUser") User nurseUser, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}