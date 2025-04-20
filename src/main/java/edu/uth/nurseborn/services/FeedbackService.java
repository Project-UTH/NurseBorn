package edu.uth.nurseborn.services;

import edu.uth.nurseborn.dtos.FeedbackDTO;
import edu.uth.nurseborn.models.Booking;
import edu.uth.nurseborn.models.Feedback;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.models.enums.BookingStatus;
import edu.uth.nurseborn.repositories.BookingRepository;
import edu.uth.nurseborn.repositories.FeedbackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackService {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackService.class);

    private final FeedbackRepository feedbackRepository;
    private final BookingRepository bookingRepository;

    public FeedbackService(FeedbackRepository feedbackRepository, BookingRepository bookingRepository) {
        this.feedbackRepository = feedbackRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public void createFeedback(FeedbackDTO feedbackDTO, User familyUser) {
        // Kiểm tra xem lịch đặt có tồn tại và đã hoàn thành không
        Booking booking = bookingRepository.findById(feedbackDTO.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch đặt với ID: " + feedbackDTO.getBookingId()));

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new IllegalArgumentException("Lịch đặt chưa hoàn thành, không thể gửi đánh giá.");
        }

        // Kiểm tra xem người dùng có phải là chủ lịch đặt không
        if (!booking.getFamilyUser().getUserId().equals(familyUser.getUserId())) {
            throw new IllegalArgumentException("Bạn không có quyền gửi đánh giá cho lịch đặt này.");
        }

        // Kiểm tra xem đã có đánh giá cho lịch đặt này chưa
        if (feedbackRepository.existsByBookingAndFamilyUser(feedbackDTO.getBookingId(), familyUser.getUserId())) {
            throw new IllegalArgumentException("Bạn đã gửi đánh giá cho lịch đặt này rồi.");
        }

        // Tạo và lưu đánh giá
        Feedback feedback = new Feedback();
        feedback.setBooking(booking);
        feedback.setFamilyUser(familyUser);
        feedback.setRating(feedbackDTO.getRating());
        feedback.setComment(feedbackDTO.getComment());
        feedbackRepository.save(feedback);

        logger.info("Đã lưu đánh giá cho lịch đặt ID: {}", feedbackDTO.getBookingId());
    }
}