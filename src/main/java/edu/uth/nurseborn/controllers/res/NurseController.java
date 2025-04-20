package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.models.Booking;
import edu.uth.nurseborn.models.Feedback;
import edu.uth.nurseborn.models.NurseProfile;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.models.enums.BookingStatus;
import edu.uth.nurseborn.services.BookingService;
import edu.uth.nurseborn.services.FeedbackService;
import edu.uth.nurseborn.services.NurseServiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class NurseController {

    private static final Logger logger = LoggerFactory.getLogger(NurseController.class);

    @Autowired
    private NurseServiceService nurseServiceService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private FeedbackService feedbackService;

    // Phương thức tiện ích để kiểm tra và lấy thông tin người dùng
    private User authenticateAndGetNurse(String redirectUrl, RedirectAttributes redirectAttributes, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            logger.warn("Người dùng chưa đăng nhập, chuyển hướng đến trang đăng nhập");
            if (redirectAttributes != null) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập");
            } else {
                model.addAttribute("error", "Vui lòng đăng nhập");
            }
            throw new IllegalStateException("redirect:/login");
        }

        String username = authentication.getName();
        User user = bookingService.getUserByUsername(username);
        if (user == null || !"NURSE".equalsIgnoreCase(user.getRole().name())) {
            logger.warn("User {} không phải NURSE", username);
            if (redirectAttributes != null) {
                redirectAttributes.addFlashAttribute("error", "Người dùng phải có vai trò NURSE");
            } else {
                model.addAttribute("error", "Người dùng phải có vai trò NURSE");
            }
            throw new IllegalStateException(redirectUrl);
        }

        return user;
    }

    @GetMapping("/nursepage")
    public String nursePage(Model model) {
        logger.debug("Hiển thị trang danh sách y tá");

        try {
            List<NurseProfile> nurses = nurseServiceService.getApprovedNurses();

            // Tính số sao trung bình cho từng y tá
            Map<Long, Double> averageRatings = new HashMap<>();
            for (NurseProfile nurse : nurses) {
                List<Feedback> feedbacks = feedbackService.getFeedbacksByNurse(nurse.getUser());
                double averageRating = 0.0;
                if (feedbacks != null && !feedbacks.isEmpty()) {
                    double totalRating = feedbacks.stream()
                            .mapToDouble(Feedback::getRating)
                            .sum();
                    averageRating = totalRating / feedbacks.size();
                }
                averageRatings.put(nurse.getUser().getUserId(), averageRating);
            }

            model.addAttribute("nurses", nurses);
            model.addAttribute("averageRatings", averageRatings);
            logger.info("Đã lấy thành công {} y tá để hiển thị", nurses.size());
            return "family/nursepage";
        } catch (Exception ex) {
            logger.error("Lỗi khi lấy danh sách y tá: {}", ex.getMessage(), ex);
            model.addAttribute("error", "Đã có lỗi xảy ra khi lấy danh sách y tá.");
            return "error";
        }
    }

    @GetMapping("/nurse/pending-bookings")
    public String showPendingBookings(Model model) {
        logger.debug("Hiển thị danh sách lịch đặt chờ xác nhận cho y tá");

        try {
            User nurseUser = authenticateAndGetNurse("error", null, model);
            List<Booking> pendingBookings = bookingService.getBookingsByNurseUserIdAndStatus(nurseUser.getUserId(), BookingStatus.PENDING);
            model.addAttribute("pendingBookings", pendingBookings);
            logger.info("Hiển thị danh sách lịch đặt chờ xác nhận cho y tá: {}", nurseUser.getUsername());
            return "nurse/pending-bookings";
        } catch (IllegalStateException e) {
            return e.getMessage();
        } catch (Exception e) {
            logger.error("Lỗi khi hiển thị lịch đặt: {}", e.getMessage(), e);
            model.addAttribute("error", "Lỗi khi tải danh sách lịch đặt: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/nurse/accept-booking")
    public String acceptBooking(@RequestParam("bookingId") Long bookingId, RedirectAttributes redirectAttributes) {
        logger.debug("Xử lý chấp nhận lịch đặt với bookingId: {}", bookingId);

        try {
            User nurseUser = authenticateAndGetNurse("redirect:/nurse/pending-bookings", redirectAttributes, null);
            bookingService.acceptBooking(bookingId, nurseUser.getUserId());
            logger.info("Y tá {} đã chấp nhận lịch đặt với bookingId: {}", nurseUser.getUsername(), bookingId);
            redirectAttributes.addFlashAttribute("success", "Chấp nhận lịch đặt thành công!");
            return "redirect:/nurse-schedule";
        } catch (IllegalStateException e) {
            return e.getMessage();
        } catch (Exception e) {
            logger.error("Lỗi khi chấp nhận lịch đặt: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi chấp nhận lịch đặt: " + e.getMessage());
            return "redirect:/nurse/pending-bookings";
        }
    }

    @PostMapping("/nurse/cancel-booking")
    public String cancelBooking(@RequestParam("bookingId") Long bookingId, RedirectAttributes redirectAttributes) {
        logger.debug("Xử lý hủy lịch đặt với bookingId: {}", bookingId);

        try {
            User nurseUser = authenticateAndGetNurse("redirect:/nurse/pending-bookings", redirectAttributes, null);
            bookingService.cancelBooking(bookingId, nurseUser.getUserId());
            logger.info("Y tá {} đã hủy lịch đặt với bookingId: {}", nurseUser.getUsername(), bookingId);
            redirectAttributes.addFlashAttribute("success", "Hủy lịch đặt thành công!");
            return "redirect:/nurse/pending-bookings";
        } catch (IllegalStateException e) {
            return e.getMessage();
        } catch (Exception e) {
            logger.error("Lỗi khi hủy lịch đặt: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi hủy lịch đặt: " + e.getMessage());
            return "redirect:/nurse/pending-bookings";
        }
    }
}

