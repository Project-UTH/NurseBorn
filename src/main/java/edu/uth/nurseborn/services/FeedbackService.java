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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FeedbackService {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackService.class);

    private final FeedbackRepository feedbackRepository;
    private final BookingRepository bookingRepository;

    @Value("${file.upload-dir:uploads/feedback/}")
    private String uploadDir;

    public FeedbackService(FeedbackRepository feedbackRepository, BookingRepository bookingRepository) {
        this.feedbackRepository = feedbackRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public void createFeedback(FeedbackDTO feedbackDTO, User familyUser, MultipartFile[] attachments) {
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

        // Xử lý upload file
        List<String> filePaths = new ArrayList<>();
        if (attachments != null && attachments.length > 0) {
            File uploadDirFile = new File(uploadDir);
            if (!uploadDirFile.exists()) {
                uploadDirFile.mkdirs();
            }

            for (MultipartFile file : attachments) {
                if (!file.isEmpty()) {
                    try {
                        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                        File dest = new File(uploadDir + fileName);
                        file.transferTo(dest);
                        filePaths.add(fileName);
                    } catch (Exception e) {
                        logger.error("Lỗi khi upload tệp: {}", file.getOriginalFilename(), e);
                        throw new IllegalArgumentException("Lỗi khi upload tệp: " + file.getOriginalFilename());
                    }
                }
            }
        }

        // Tạo và lưu đánh giá
        Feedback feedback = new Feedback();
        feedback.setBooking(booking);
        feedback.setFamilyUser(familyUser);
        feedback.setNurseUser(booking.getNurseUser()); // Lấy nurseUser từ booking
        feedback.setRating(feedbackDTO.getRating());
        feedback.setComment(feedbackDTO.getComment());
        feedback.setAttachment(String.join(",", filePaths)); // Lưu danh sách đường dẫn file
        feedback.setCreatedAt(LocalDateTime.now());

        feedbackRepository.save(feedback);
        logger.info("Đã lưu đánh giá cho lịch đặt ID: {}", feedbackDTO.getBookingId());
    }

    public boolean hasUserRatedBooking(Long bookingId, Long familyUserId) {
        return feedbackRepository.existsByBookingAndFamilyUser(bookingId, familyUserId);
    }

    public List<Feedback> getFeedbacksByNurse(User nurse) {
        return feedbackRepository.findAll().stream()
                .filter(f -> f.getNurseUser().getUserId().equals(nurse.getUserId()))
                .toList();
    }

    public List<Feedback> getFeedbacksByBooking(Long bookingId) {
        return feedbackRepository.findByBookingBookingId(bookingId);
    }
}