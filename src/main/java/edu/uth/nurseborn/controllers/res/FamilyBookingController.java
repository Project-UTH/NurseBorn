package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.dtos.FeedbackDTO;
import edu.uth.nurseborn.models.Booking;
import edu.uth.nurseborn.models.Feedback;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.services.BookingService;
import edu.uth.nurseborn.services.FeedbackService;
import edu.uth.nurseborn.repositories.BookingRepository;
import edu.uth.nurseborn.repositories.FeedbackRepository;
import edu.uth.nurseborn.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/family")
public class FamilyBookingController {

    private static final Logger logger = LoggerFactory.getLogger(FamilyBookingController.class);

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FeedbackService feedbackService;

    @GetMapping("/bookings")
    public String showFamilyBookings(Model model, Authentication authentication) {
        logger.info("Hiển thị danh sách lịch đặt cho FAMILY");

        String username = authentication.getName();
        logger.debug("Username từ Authentication: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với username: " + username));

        if (!"FAMILY".equalsIgnoreCase(user.getRole().name())) {
            logger.warn("Người dùng {} không có quyền truy cập danh sách lịch đặt của FAMILY", user.getUsername());
            model.addAttribute("error", "Bạn không có quyền truy cập trang này.");
            return "error";
        }

        List<Booking> bookings = bookingService.getBookingsByFamilyUser(user);
        logger.debug("Số lượng lịch đặt tìm thấy cho user {}: {}", user.getUsername(), bookings.size());
        for (Booking booking : bookings) {
            logger.debug("Booking ID: {}, Status: {}, Has Feedback: {}",
                    booking.getBookingId(), booking.getStatus().name(), booking.getHasFeedback());
        }

        model.addAttribute("bookings", bookings);
        model.addAttribute("user", user);

        return "family/family-bookings";
    }

    @PostMapping("/cancel-booking")
    public String cancelBooking(@RequestParam("bookingId") Long bookingId, Authentication authentication, Model model) {
        logger.info("Hủy lịch đặt với ID: {} từ phía FAMILY", bookingId);

        String username = authentication.getName();
        logger.debug("Username từ Authentication: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với username: " + username));

        if (!"FAMILY".equalsIgnoreCase(user.getRole().name())) {
            logger.warn("Người dùng {} không có quyền hủy lịch đặt", user.getUsername());
            model.addAttribute("error", "Bạn không có quyền hủy lịch đặt.");
            return "error";
        }

        try {
            bookingService.cancelBookingByFamily(bookingId, user.getUserId());
            model.addAttribute("success", "Hủy lịch đặt thành công!");
        } catch (IllegalArgumentException e) {
            logger.error("Lỗi khi hủy lịch đặt với ID: {}", bookingId, e);
            model.addAttribute("error", e.getMessage());
        }

        List<Booking> bookings = bookingService.getBookingsByFamilyUser(user);
        model.addAttribute("bookings", bookings);
        model.addAttribute("user", user);

        return "family/family-bookings";
    }

    @GetMapping("/feedback")
    public String showFeedbackForm(@RequestParam("bookingId") Long bookingId, Authentication authentication, Model model) {
        logger.info("Hiển thị form đánh giá cho booking ID: {}", bookingId);

        String username = authentication.getName();
        logger.debug("Username từ Authentication: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với username: " + username));

        if (!"FAMILY".equalsIgnoreCase(user.getRole().name())) {
            logger.warn("Người dùng {} không có quyền truy cập trang đánh giá", user.getUsername());
            model.addAttribute("error", "Bạn không có quyền truy cập trang này.");
            return "error";
        }

        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch đặt với ID: " + bookingId));

            if (!booking.getFamilyUser().getUserId().equals(user.getUserId())) {
                logger.warn("Người dùng {} không có quyền đánh giá booking ID: {}", user.getUsername(), bookingId);
                model.addAttribute("error", "Bạn không có quyền đánh giá lịch đặt này.");
                return "family/family-bookings";
            }

            if (!booking.getStatus().name().equals("COMPLETED")) {
                logger.warn("Lịch đặt với ID: {} không ở trạng thái COMPLETED", bookingId);
                model.addAttribute("error", "Chỉ có thể đánh giá các lịch đặt đã hoàn thành.");
                return "family/family-bookings";
            }

            // Kiểm tra xem người dùng đã đánh giá booking này chưa
            boolean hasFeedback = feedbackService.hasUserRatedBooking(bookingId, user.getUserId());
            if (hasFeedback) {
                model.addAttribute("hasFeedback", true);
                model.addAttribute("feedbackMessage", "Bạn đã đánh giá lịch đặt này rồi.");
            }

            // Lấy danh sách các đánh giá của y tá
            List<Feedback> feedbacks = feedbackService.getFeedbacksByNurse(booking.getNurseUser());
            model.addAttribute("booking", booking);
            model.addAttribute("feedbacks", feedbacks);
            model.addAttribute("feedbackDTO", new FeedbackDTO());
        } catch (IllegalArgumentException e) {
            logger.error("Lỗi khi tìm booking với ID: {}", bookingId, e);
            model.addAttribute("error", e.getMessage());
            return "family/family-bookings";
        }

        return "family/feedbacks";
    }

    @PostMapping("/submit-feedback")
    @Transactional
    public String submitFeedback(
            @ModelAttribute("feedbackDTO") FeedbackDTO feedbackDTO,
            @RequestParam(value = "attachment", required = false) MultipartFile[] attachments,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        logger.info("Gửi đánh giá cho booking ID: {}", feedbackDTO.getBookingId());

        String username = authentication.getName();
        logger.debug("Username từ Authentication: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với username: " + username));

        if (!"FAMILY".equalsIgnoreCase(user.getRole().name())) {
            logger.warn("Người dùng {} không có quyền gửi đánh giá", user.getUsername());
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền gửi đánh giá.");
            return "redirect:/family/bookings";
        }

        try {
            // Kiểm tra booking trước khi lưu feedback
            Booking booking = bookingRepository.findById(feedbackDTO.getBookingId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch đặt với ID: " + feedbackDTO.getBookingId()));
            logger.debug("Trước khi lưu feedback - Booking ID: {}, Has Feedback: {}", feedbackDTO.getBookingId(), booking.getHasFeedback());

            feedbackService.createFeedback(feedbackDTO, user, attachments);
            redirectAttributes.addFlashAttribute("success", "Gửi đánh giá thành công!");
            logger.info("Người dùng {} đã gửi đánh giá cho lịch đặt ID: {}", user.getUsername(), feedbackDTO.getBookingId());

            // Cập nhật hasFeedback của booking
            booking.setHasFeedback(true);
            bookingRepository.save(booking);
            logger.info("Đã cập nhật hasFeedback thành true cho booking ID: {}", feedbackDTO.getBookingId());

            // Làm mới dữ liệu để kiểm tra
            booking = bookingRepository.findById(feedbackDTO.getBookingId()).orElse(booking);
            logger.debug("Sau khi lưu feedback - Booking ID: {}, Has Feedback: {}", feedbackDTO.getBookingId(), booking.getHasFeedback());

        } catch (IllegalArgumentException e) {
            logger.error("Lỗi khi gửi đánh giá: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi gửi đánh giá: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Lỗi không mong muốn khi gửi đánh giá: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi không mong muốn khi gửi đánh giá: " + e.getMessage());
        }

        return "redirect:/family/feedbacks?bookingId=" + feedbackDTO.getBookingId();    }
}

