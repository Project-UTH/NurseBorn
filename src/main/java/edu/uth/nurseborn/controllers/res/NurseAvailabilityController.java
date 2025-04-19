package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.dtos.NurseAvailabilityDTO;
import edu.uth.nurseborn.dtos.UserDTO;
import edu.uth.nurseborn.models.Booking;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.models.enums.BookingStatus;
import edu.uth.nurseborn.repositories.UserRepository;
import edu.uth.nurseborn.services.BookingService;
import edu.uth.nurseborn.services.NurseAvailabilityService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class NurseAvailabilityController {

    private static final Logger logger = LoggerFactory.getLogger(NurseAvailabilityController.class);

    @Autowired
    private NurseAvailabilityService nurseAvailabilityService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ModelMapper modelMapper;

    // Phương thức tiện ích để kiểm tra và lấy thông tin người dùng
    private UserDTO authenticateAndGetNurse(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            logger.warn("Người dùng chưa đăng nhập, chuyển hướng đến trang đăng nhập");
            throw new IllegalStateException("redirect:/login");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        String role = user.getRole().name();
        userDTO.setRole(role.startsWith("ROLE_") ? role.substring(5) : role);

        if (!"NURSE".equalsIgnoreCase(userDTO.getRole())) {
            logger.warn("User {} không có quyền truy cập", username);
            throw new IllegalStateException("redirect:/");
        }

        return userDTO;
    }

    @GetMapping("/nurse-availability")
    public String showAvailabilityForm(Model model) {
        logger.debug("Hiển thị form chọn lịch làm việc");

        try {
            UserDTO userDTO = authenticateAndGetNurse(model);
            NurseAvailabilityDTO availabilityDTO = nurseAvailabilityService.getAvailabilityByUserId(userDTO.getUserId());
            model.addAttribute("availabilityDTO", availabilityDTO);
            model.addAttribute("user", userDTO);
            model.addAttribute("daysOfWeek", Arrays.asList("Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"));
            return "nurse/nurse-availability";
        } catch (IllegalStateException e) {
            return e.getMessage();
        } catch (Exception e) {
            logger.error("Lỗi khi hiển thị form lịch làm việc: {}", e.getMessage(), e);
            model.addAttribute("error", "Lỗi khi tải form: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/nurse-availability")
    public String submitAvailabilityForm(@ModelAttribute("availabilityDTO") NurseAvailabilityDTO availabilityDTO, Model model) {
        logger.debug("Xử lý submit form lịch làm việc");

        try {
            UserDTO userDTO = authenticateAndGetNurse(model);
            availabilityDTO.setUserId(userDTO.getUserId());
            nurseAvailabilityService.createOrUpdateAvailability(userDTO.getUserId(), availabilityDTO);
            model.addAttribute("success", "Cập nhật lịch làm việc thành công!");
            return "redirect:/nurse-schedule";
        } catch (IllegalStateException e) {
            return e.getMessage();
        } catch (Exception e) {
            logger.error("Lỗi khi lưu lịch làm việc: {}", e.getMessage(), e);
            model.addAttribute("error", "Lỗi khi lưu lịch làm việc: " + e.getMessage());
            model.addAttribute("daysOfWeek", Arrays.asList("Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"));
            return "nurse/nurse-availability";
        }
    }

    @GetMapping("/nurse-schedule")
    public String showSchedule(
            @RequestParam(value = "weekOffset", required = false, defaultValue = "0") int weekOffset,
            Model model) {
        logger.debug("Hiển thị lịch làm việc");

        try {
            UserDTO userDTO = authenticateAndGetNurse(model);
            NurseAvailabilityDTO availabilityDTO = nurseAvailabilityService.getAvailabilityByUserId(userDTO.getUserId());
            List<String> daysOfWeek = Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");

            List<Booking> acceptedBookings = bookingService.getBookingsByNurseUserIdAndStatus(userDTO.getUserId(), BookingStatus.ACCEPTED);

            LocalDate currentDate = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            LocalDate startOfWeek = currentDate.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).plusWeeks(weekOffset);
            LocalDate endOfWeek = startOfWeek.plusDays(6);

            List<LocalDate> weekDates = new ArrayList<>();
            for (LocalDate date = startOfWeek; !date.isAfter(endOfWeek); date = date.plusDays(1)) {
                weekDates.add(date);
            }

            Map<LocalDate, List<Booking>> bookingsByDate = acceptedBookings.stream()
                    .filter(booking -> !booking.getBookingDate().isBefore(startOfWeek) && !booking.getBookingDate().isAfter(endOfWeek))
                    .collect(Collectors.groupingBy(Booking::getBookingDate));

            model.addAttribute("availabilityDTO", availabilityDTO);
            model.addAttribute("daysOfWeek", daysOfWeek);
            model.addAttribute("weekDates", weekDates);
            model.addAttribute("bookingsByDate", bookingsByDate);
            model.addAttribute("currentDate", currentDate);
            model.addAttribute("weekOffset", weekOffset);
            model.addAttribute("user", userDTO);
            return "nurse/schedule";
        } catch (IllegalStateException e) {
            return e.getMessage();
        } catch (Exception e) {
            logger.error("Lỗi khi hiển thị lịch: {}", e.getMessage(), e);
            model.addAttribute("error", "Lỗi khi tải lịch: " + e.getMessage());
            return "error";
        }
    }
}