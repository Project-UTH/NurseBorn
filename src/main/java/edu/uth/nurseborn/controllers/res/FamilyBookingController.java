package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.models.Booking;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.services.BookingService;
import edu.uth.nurseborn.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/family")
public class FamilyBookingController {

    private static final Logger logger = LoggerFactory.getLogger(FamilyBookingController.class);

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/bookings")
    public String showFamilyBookings(Model model, Authentication authentication) {
        logger.info("Hiển thị danh sách lịch đặt cho FAMILY");

        // Lấy username từ Authentication
        String username = authentication.getName();
        logger.debug("Username từ Authentication: {}", username);

        // Tìm kiếm đối tượng User từ cơ sở dữ liệu dựa trên username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với username: " + username));

        // Kiểm tra vai trò
        if (!"FAMILY".equalsIgnoreCase(user.getRole().name())) {
            logger.warn("Người dùng {} không có quyền truy cập danh sách lịch đặt của FAMILY", user.getUsername());
            model.addAttribute("error", "Bạn không có quyền truy cập trang này.");
            return "error";
        }

        // Lấy danh sách tất cả lịch đặt của FAMILY
        List<Booking> bookings = bookingService.getBookingsByFamilyUser(user);
        logger.debug("Số lượng lịch đặt tìm thấy cho user {}: {}", user.getUsername(), bookings.size());

        // Truyền dữ liệu vào model
        model.addAttribute("bookings", bookings);
        model.addAttribute("user", user);

        return "family/family-bookings";
    }

    @PostMapping("/cancel-booking")
    public String cancelBooking(@RequestParam("bookingId") Long bookingId, Authentication authentication, Model model) {
        logger.info("Hủy lịch đặt với ID: {} từ phía FAMILY", bookingId);

        // Lấy username từ Authentication
        String username = authentication.getName();
        logger.debug("Username từ Authentication: {}", username);

        // Tìm kiếm đối tượng User từ cơ sở dữ liệu dựa trên username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với username: " + username));

        // Kiểm tra vai trò
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

        // Lấy lại danh sách bookings để cập nhật giao diện
        List<Booking> bookings = bookingService.getBookingsByFamilyUser(user);
        model.addAttribute("bookings", bookings);
        model.addAttribute("user", user);

        return "family/family-bookings";
    }
}