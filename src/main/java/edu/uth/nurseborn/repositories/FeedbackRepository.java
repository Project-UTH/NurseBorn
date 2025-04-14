package edu.uth.nurseborn.repositories;

import edu.uth.nurseborn.models.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {

    // Tìm tất cả Feedback theo booking
    List<Feedback> findByBooking(Booking booking);

    // Tìm tất cả Feedback theo family (gia đình)
    List<Feedback> findByFamily(User family);

    // Tìm tất cả Feedback theo nurse (y tá)
    List<Feedback> findByNurse(User nurse);

    // Tìm tất cả Feedback theo rating
    List<Feedback> findByRating(Integer rating);

    // Tìm tất cả Feedback theo nurse và rating
    List<Feedback> findByNurseAndRating(User nurse, Integer rating);

    // Tìm tất cả Feedback theo booking và nurse
    List<Feedback> findByBookingAndNurse(Booking booking, User nurse);

    // Tìm một Feedback duy nhất theo booking
    Optional<Feedback> findOneByBooking(Booking booking);

    // Tìm một Feedback duy nhất theo booking và nurse
    Optional<Feedback> findOneByBookingAndNurse(Booking booking, User nurse);

    // Tìm một Feedback duy nhất theo feedbackId
    Optional<Feedback> findByFeedbackId(Integer feedbackId);

    // Tìm tất cả Feedback theo family_user_id
    List<Feedback> findByFamilyUserId(Long familyUserId);

    // Tìm tất cả Feedback theo nurse_user_id
    List<Feedback> findByNurseUserId(Long nurseUserId);
}