package edu.uth.nurseborn.repositories;

import edu.uth.nurseborn.models.Booking;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.models.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByFamilyUser(User familyUser);

    List<Booking> findByNurseUser(User nurseUser);

    @Query("SELECT b FROM Booking b WHERE b.nurseUser = :nurseUser AND b.bookingDate = :bookingDate AND b.status NOT IN ('CANCELLED')")
    List<Booking> findByNurseUserAndBookingDate(@Param("nurseUser") User nurseUser, @Param("bookingDate") LocalDate bookingDate);
    // Phương thức tìm kiếm lịch theo nurseUserId, bookingDate và status không phải CANCELLED
    List<Booking> findByNurseUserUserIdAndBookingDateAndStatusNot(Long nurseUserId, LocalDate bookingDate, BookingStatus status);
}