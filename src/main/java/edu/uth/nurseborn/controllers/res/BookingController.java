package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.dtos.BookingDTO;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.models.enums.Role;
import edu.uth.nurseborn.repositories.UserRepository;
import edu.uth.nurseborn.services.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controller xử lý các yêu cầu liên quan đến đặt lịch.
 */
@Controller
@RequestMapping("/booking")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Tạo một booking mới (dành cho FAMILY).
     */
    @PostMapping("/create")
    public String createBooking(@ModelAttribute BookingDTO bookingDTO, Authentication authentication) {
        logger.debug("Nhận yêu cầu tạo booking từ người dùng: {}", authentication.getName());

        String username = authentication.getName();
        User familyUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + username));

        if (!Role.FAMILY.equals(familyUser.getRole())) {
            logger.warn("Người dùng {} không có vai trò FAMILY", username);
            return "redirect:/";
        }

        try {
            bookingService.createBooking(bookingDTO, familyUser.getUserId());
            logger.info("Tạo booking thành công cho familyUserId: {}", familyUser.getUserId());
            return "redirect:/family/bookings";
        } catch (IllegalArgumentException e) {
            logger.error("Lỗi khi tạo booking: {}", e.getMessage());
            return "redirect:/family/book-nurse?error=" + e.getMessage();
        }
    }

    /**
     * Chấp nhận một booking (dành cho NURSE).
     */
    @PostMapping("/accept")
    public String acceptBooking(@RequestParam Long bookingId, Authentication authentication) {
        logger.debug("Nhận yêu cầu chấp nhận booking ID: {} từ người dùng: {}", bookingId, authentication.getName());

        String username = authentication.getName();
        User nurseUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy y tá: " + username));

        if (!Role.NURSE.equals(nurseUser.getRole())) {
            logger.warn("Người dùng {} không phải y tá", username);
            return "redirect:/";
        }

        try {
            bookingService.acceptBooking(bookingId, nurseUser.getUserId());
            logger.info("Chấp nhận booking ID: {} thành công bởi y tá: {}", bookingId, username);
            return "redirect:/nurse/pending-bookings";
        } catch (IllegalArgumentException e) {
            logger.error("Lỗi khi chấp nhận booking ID: {}: {}", bookingId, e.getMessage());
            return "redirect:/nurse/pending-bookings?error=" + e.getMessage();
        }
    }

    /**
     * Hủy một booking (dành cho NURSE).
     */
    @PostMapping("/cancel")
    public String cancelBooking(@RequestParam Long bookingId, Authentication authentication) {
        logger.debug("Nhận yêu cầu hủy booking ID: {} từ người dùng: {}", bookingId, authentication.getName());

        String username = authentication.getName();
        User nurseUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy y tá: " + username));

        if (!Role.NURSE.equals(nurseUser.getRole())) {
            logger.warn("Người dùng {} không phải y tá", username);
            return "redirect:/";
        }

        try {
            bookingService.cancelBooking(bookingId, nurseUser.getUserId());
            logger.info("Hủy booking ID: {} thành công bởi y tá: {}", bookingId, username);
            return "redirect:/nurse/pending-bookings";
        } catch (IllegalArgumentException e) {
            logger.error("Lỗi khi hủy booking ID: {}: {}", bookingId, e.getMessage());
            return "redirect:/nurse/pending-bookings?error=" + e.getMessage();
        }
    }

    /**
     * Hoàn thành một booking (dành cho NURSE).
     */
    @PostMapping("/complete")
    public String completeBooking(@RequestParam Long bookingId, Authentication authentication) {
        logger.debug("Nhận yêu cầu hoàn thành booking ID: {} từ người dùng: {}", bookingId, authentication.getName());

        String username = authentication.getName();
        User nurseUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy y tá: " + username));

        if (!Role.NURSE.equals(nurseUser.getRole())) {
            logger.warn("Người dùng {} không phải y tá", username);
            return "redirect:/";
        }

        try {
            bookingService.completeBooking(bookingId, nurseUser.getUserId());
            logger.info("Hoàn thành booking ID: {} thành công bởi y tá: {}", bookingId, username);
            return "redirect:/nurse/accepted-bookings";
        } catch (IllegalArgumentException e) {
            logger.error("Lỗi khi hoàn thành booking ID: {}: {}", bookingId, e.getMessage());
            return "redirect:/nurse/accepted-bookings?error=" + e.getMessage();
        }
    }
}