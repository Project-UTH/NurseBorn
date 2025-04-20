package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.dtos.FeedbackDTO;
import edu.uth.nurseborn.models.Booking;
import edu.uth.nurseborn.models.Feedback;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.repositories.UserRepository;
import edu.uth.nurseborn.services.BookingService;
import edu.uth.nurseborn.services.FeedbackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

@Controller
public class FeedbackController {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackController.class);

    private final UserRepository userRepository;
    private final BookingService bookingService;
    private final FeedbackService feedbackService;

    public FeedbackController(UserRepository userRepository, BookingService bookingService, FeedbackService feedbackService) {
        this.userRepository = userRepository;
        this.bookingService = bookingService;
        this.feedbackService = feedbackService;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            logger.warn("Người dùng chưa đăng nhập, chuyển hướng đến trang đăng nhập");
            throw new IllegalStateException("Người dùng chưa đăng nhập");
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + username));
    }

    @GetMapping("/family/feedbacks")
    public String showFeedbackPage(
            @RequestParam(value = "bookingId", required = false) Long bookingId,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser();

            if (!"FAMILY".equalsIgnoreCase(user.getRole().name())) {
                logger.warn("Người dùng {} không phải FAMILY, không có quyền truy cập", user.getUsername());
                redirectAttributes.addFlashAttribute("error", "Bạn cần vai trò FAMILY để truy cập!");
                return "redirect:/";
            }

            // Lấy danh sách các booking đã hoàn thành
            List<Booking> completedBookings = bookingService.getCompletedBookingsForFamily(user);
            model.addAttribute("completedBookings", completedBookings);
            model.addAttribute("user", user);
            model.addAttribute("feedbackDTO", new FeedbackDTO());

            // Nếu có bookingId, hiển thị thông tin chi tiết của booking đó và danh sách đánh giá
            if (bookingId != null) {
                Optional<Booking> selectedBooking = completedBookings.stream()
                        .filter(b -> b.getBookingId().equals(bookingId))
                        .findFirst();

                if (selectedBooking.isPresent()) {
                    Booking booking = selectedBooking.get();
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
                } else {
                    redirectAttributes.addFlashAttribute("error", "Không tìm thấy lịch đặt với ID: " + bookingId);
                    return "redirect:/family/feedbacks";
                }
            }

            logger.info("Hiển thị trang đánh giá cho người dùng {} với {} lịch đặt đã hoàn thành", user.getUsername(), completedBookings.size());
            return "family/feedbacks";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập!");
            return "redirect:/login";
        } catch (Exception e) {
            logger.error("Lỗi khi hiển thị trang đánh giá: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/error";
        }
    }

    @PostMapping("/family/rate-booking")
    public String submitFeedback(
            @ModelAttribute("feedbackDTO") FeedbackDTO feedbackDTO,
            @RequestParam(value = "attachment", required = false) MultipartFile[] attachments,
            RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser();

            if (!"FAMILY".equalsIgnoreCase(user.getRole().name())) {
                logger.warn("Người dùng {} không phải FAMILY, không có quyền gửi đánh giá", user.getUsername());
                redirectAttributes.addFlashAttribute("error", "Bạn cần vai trò FAMILY để gửi đánh giá!");
                return "redirect:/";
            }

            feedbackService.createFeedback(feedbackDTO, user, attachments);
            redirectAttributes.addFlashAttribute("success", "Gửi đánh giá thành công!");
            logger.info("Người dùng {} đã gửi đánh giá cho lịch đặt ID: {}", user.getUsername(), feedbackDTO.getBookingId());
            return "redirect:/family/feedbacks?bookingId=" + feedbackDTO.getBookingId();
        } catch (IllegalStateException e) {
            logger.warn("Người dùng chưa đăng nhập, chuyển hướng đến trang đăng nhập");
            redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập!");
            return "redirect:/login";
        } catch (Exception e) {
            logger.error("Lỗi khi gửi đánh giá: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi gửi đánh giá: " + e.getMessage());
            return "redirect:/family/feedbacks?bookingId=" + feedbackDTO.getBookingId();
        }
    }
}