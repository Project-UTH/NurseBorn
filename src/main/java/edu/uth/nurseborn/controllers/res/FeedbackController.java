package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.dtos.FeedbackDTO;
import edu.uth.nurseborn.services.FeedbackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackController {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackController.class);

    @Autowired
    private FeedbackService feedbackService;

    // Tạo mới một đánh giá
    @PostMapping
    public ResponseEntity<FeedbackDTO> createFeedback(@RequestBody FeedbackDTO danhGiaDTO) {
        logger.info("Nhận yêu cầu tạo đánh giá: {}", danhGiaDTO);
        FeedbackDTO createdFeedback = feedbackService.createFeedback(danhGiaDTO);
        logger.info("Đã tạo đánh giá thành công với ID: {}", createdFeedback.getFeedbackId());
        return new ResponseEntity<>(createdFeedback, HttpStatus.CREATED);
    }

    // Lấy đánh giá theo feedbackId
    @GetMapping("/{feedbackId}")
    public ResponseEntity<FeedbackDTO> getFeedbackById(@PathVariable Integer feedbackId) {
        logger.info("Nhận yêu cầu lấy đánh giá với ID: {}", feedbackId);
        FeedbackDTO feedback = feedbackService.getFeedbackById(feedbackId);
        logger.info("Đã lấy đánh giá thành công với ID: {}", feedbackId);
        return new ResponseEntity<>(feedback, HttpStatus.OK);
    }

    // Lấy đánh giá theo bookingId
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<FeedbackDTO> getFeedbackByBooking(@PathVariable Integer bookingId) {
        logger.info("Nhận yêu cầu lấy đánh giá cho lịch đặt ID: {}", bookingId);
        FeedbackDTO feedback = feedbackService.getFeedbackByBooking(bookingId);
        logger.info("Đã lấy đánh giá thành công cho lịch đặt ID: {}", bookingId);
        return new ResponseEntity<>(feedback, HttpStatus.OK);
    }

    // Lấy đánh giá theo bookingId và nurseUserId
    @GetMapping("/booking/{bookingId}/nurse/{nurseUserId}")
    public ResponseEntity<FeedbackDTO> getFeedbackByBookingAndNurse(
            @PathVariable Integer bookingId,
            @PathVariable Long nurseUserId) { // Thay Integer thành Long
        logger.info("Nhận yêu cầu lấy đánh giá cho lịch đặt ID: {} và y tá ID: {}", bookingId, nurseUserId);
        FeedbackDTO feedback = feedbackService.getFeedbackByBookingAndNurse(bookingId, nurseUserId);
        logger.info("Đã lấy đánh giá thành công cho lịch đặt ID: {} và y tá ID: {}", bookingId, nurseUserId);
        return new ResponseEntity<>(feedback, HttpStatus.OK);
    }

    // Lấy tất cả đánh giá của một y tá
    @GetMapping("/nurse/{nurseUserId}")
    public ResponseEntity<List<FeedbackDTO>> getFeedbacksByNurse(@PathVariable Long nurseUserId) { // Thay Integer thành Long
        logger.info("Nhận yêu cầu lấy tất cả đánh giá cho y tá ID: {}", nurseUserId);
        List<FeedbackDTO> feedbacks = feedbackService.getFeedbacksByNurse(nurseUserId);
        logger.info("Đã lấy {} đánh giá cho y tá ID: {}", feedbacks.size(), nurseUserId);
        return new ResponseEntity<>(feedbacks, HttpStatus.OK);
    }

    // Lấy tất cả đánh giá của một gia đình
    @GetMapping("/family/{familyUserId}")
    public ResponseEntity<List<FeedbackDTO>> getFeedbacksByFamily(@PathVariable Long familyUserId) { // Thay Integer thành Long
        logger.info("Nhận yêu cầu lấy tất cả đánh giá cho gia đình ID: {}", familyUserId);
        List<FeedbackDTO> feedbacks = feedbackService.getFeedbacksByFamily(familyUserId);
        logger.info("Đã lấy {} đánh giá cho gia đình ID: {}", feedbacks.size(), familyUserId);
        return new ResponseEntity<>(feedbacks, HttpStatus.OK);
    }
}