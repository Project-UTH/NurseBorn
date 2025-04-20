package edu.uth.nurseborn.repositories;

import edu.uth.nurseborn.models.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    @Query("SELECT COUNT(f) > 0 FROM Feedback f WHERE f.booking.bookingId = :bookingId AND f.familyUser.userId = :familyUserId")
    boolean existsByBookingAndFamilyUser(@Param("bookingId") Long bookingId, @Param("familyUserId") Long familyUserId);
}