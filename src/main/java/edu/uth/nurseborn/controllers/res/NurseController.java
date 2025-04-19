package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.models.Booking;
import edu.uth.nurseborn.models.NurseProfile;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.models.enums.BookingStatus;
import edu.uth.nurseborn.services.BookingService;
import edu.uth.nurseborn.services.NurseAvailabilityService;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class NurseController {

    private static final Logger logger = LoggerFactory.getLogger(NurseController.class);

    @Autowired
    private NurseServiceService nurseServiceService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private NurseAvailabilityService nurseAvailabilityService;

    @GetMapping("/nursepage")
    public String NursePage(Model model) {
        logger.debug("Hiển thị trang danh sách y tá");

        try {
            // Lấy danh sách y tá đã được phê duyệt
            List<NurseProfile> nurses = nurseServiceService.getApprovedNurses();
            model.addAttribute("nurses", nurses);
            logger.info("Đã lấy thành công {} y tá để hiển thị", nurses.size());
        } catch (Exception ex) {
            logger.error("Lỗi khi lấy danh sách y tá: {}", ex.getMessage(), ex);
            model.addAttribute("error", "Đã có lỗi xảy ra khi lấy danh sách y tá.");
        }

        return "family/nursepage";
    }

    @GetMapping("/nurse/schedule")
    public String showSchedule(Model model) {
        logger.debug("Hiển thị lịch làm việc và lịch đã chấp nhận cho y tá");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            logger.warn("Người dùng chưa đăng nhập, chuyển hướng đến trang đăng nhập");
            return "redirect:/login";
        }

        try {
            String username = authentication.getName();
            User nurseUser = bookingService.getUserByUsername(username);
            if (nurseUser == null || !"NURSE".equalsIgnoreCase(nurseUser.getRole().name())) {
                logger.warn("User {} không phải NURSE", username);
                model.addAttribute("error", "Người dùng phải có vai trò NURSE");
                return "error";
            }

            // Lấy lịch làm việc
            var availabilityDTO = nurseAvailabilityService.getAvailabilityByUserId(nurseUser.getUserId());
            List<String> daysOfWeek = Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");

            // Log để kiểm tra availabilityDTO.selectedDays
            logger.debug("Ngày làm việc đã chọn: {}", availabilityDTO.getSelectedDays());

            // Lấy danh sách lịch đã chấp nhận của y tá
            List<Booking> acceptedBookings = bookingService.getBookingsByNurseUserIdAndStatus(nurseUser.getUserId(), BookingStatus.ACCEPTED);
            logger.debug("Số lượng lịch đã chấp nhận: {}", acceptedBookings.size());
            for (Booking booking : acceptedBookings) {
                logger.debug("Lịch đã chấp nhận: bookingId={}, bookingDate={}, dayOfWeek={}",
                        booking.getBookingId(), booking.getBookingDate(), booking.getBookingDate().getDayOfWeek());
            }

            // Xác định tuần hiện tại (dựa trên ngày hiện tại: 19/04/2025)
            LocalDate currentDate = LocalDate.of(2025, 4, 19); // Ngày hiện tại
            LocalDate startOfWeek = currentDate.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
            LocalDate endOfWeek = startOfWeek.plusDays(6);

            // Log để kiểm tra tuần hiện tại
            logger.debug("Tuần hiện tại: từ {} đến {}", startOfWeek, endOfWeek);

            // Tạo danh sách các ngày trong tuần
            List<LocalDate> weekDates = new ArrayList<>();
            for (LocalDate date = startOfWeek; !date.isAfter(endOfWeek); date = date.plusDays(1)) {
                weekDates.add(date);
            }

            // Nhóm các lịch đã chấp nhận theo ngày trong tuần
            Map<LocalDate, List<Booking>> bookingsByDate = acceptedBookings.stream()
                    .filter(booking -> {
                        boolean isInWeek = !booking.getBookingDate().isBefore(startOfWeek) && !booking.getBookingDate().isAfter(endOfWeek);
                        logger.debug("Lịch bookingId={} có bookingDate={} nằm trong tuần: {}", booking.getBookingId(), booking.getBookingDate(), isInWeek);
                        return isInWeek;
                    })
                    .collect(Collectors.groupingBy(Booking::getBookingDate));

            // Log để kiểm tra dữ liệu nhóm
            bookingsByDate.forEach((date, bookings) ->
                    logger.debug("Ngày {}: {} lịch đã chấp nhận", date, bookings.size()));

            // Log để kiểm tra weekDates
            logger.debug("Danh sách ngày trong tuần: {}", weekDates);

            model.addAttribute("availabilityDTO", availabilityDTO);
            model.addAttribute("daysOfWeek", daysOfWeek);
            model.addAttribute("weekDates", weekDates);
            model.addAttribute("bookingsByDate", bookingsByDate);
            model.addAttribute("currentDate", currentDate);
            logger.info("Hiển thị lịch làm việc và lịch đã chấp nhận cho y tá: {}", username);
            return "nurse/schedule";
        } catch (Exception e) {
            logger.error("Lỗi khi hiển thị lịch: {}", e.getMessage(), e);
            model.addAttribute("error", "Lỗi khi tải lịch: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/nurse/pending-bookings")
    public String showPendingBookings(Model model) {
        logger.debug("Hiển thị danh sách lịch đặt chờ xác nhận cho y tá");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            logger.warn("Người dùng chưa đăng nhập, chuyển hướng đến trang đăng nhập");
            return "redirect:/login";
        }

        try {
            String username = authentication.getName();
            User nurseUser = bookingService.getUserByUsername(username);
            if (nurseUser == null || !"NURSE".equalsIgnoreCase(nurseUser.getRole().name())) {
                logger.warn("User {} không phải NURSE", username);
                model.addAttribute("error", "Người dùng phải có vai trò NURSE");
                return "error";
            }

            // Lấy danh sách lịch đặt PENDING của y tá
            List<Booking> pendingBookings = bookingService.getBookingsByNurseUserIdAndStatus(nurseUser.getUserId(), BookingStatus.PENDING);

            model.addAttribute("pendingBookings", pendingBookings);
            logger.info("Hiển thị danh sách lịch đặt chờ xác nhận cho y tá: {}", username);
            return "nurse/pending-bookings";
        } catch (Exception e) {
            logger.error("Lỗi khi hiển thị lịch đặt: {}", e.getMessage(), e);
            model.addAttribute("error", "Lỗi khi tải danh sách lịch đặt: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/nurse/accept-booking")
    public String acceptBooking(@RequestParam("bookingId") Long bookingId, RedirectAttributes redirectAttributes) {
        logger.debug("Xử lý chấp nhận lịch đặt với bookingId: {}", bookingId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            logger.warn("Người dùng chưa đăng nhập, chuyển hướng đến trang đăng nhập");
            return "redirect:/login";
        }

        try {
            String username = authentication.getName();
            User nurseUser = bookingService.getUserByUsername(username);
            if (nurseUser == null || !"NURSE".equalsIgnoreCase(nurseUser.getRole().name())) {
                logger.warn("User {} không phải NURSE", username);
                redirectAttributes.addFlashAttribute("error", "Người dùng phải có vai trò NURSE");
                return "redirect:/nurse/pending-bookings";
            }

            // Chấp nhận lịch
            bookingService.acceptBooking(bookingId, nurseUser.getUserId());
            logger.info("Y tá {} đã chấp nhận lịch đặt với bookingId: {}", username, bookingId);

            redirectAttributes.addFlashAttribute("success", "Chấp nhận lịch đặt thành công!");
            return "redirect:/nurse/schedule"; // Chuyển hướng về trang lịch làm việc
        } catch (Exception e) {
            logger.error("Lỗi khi chấp nhận lịch đặt: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khi chấp nhận lịch đặt: " + e.getMessage());
            return "redirect:/nurse/pending-bookings";
        }
    }
}