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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

    // Ánh xạ từ tiếng Việt sang tiếng Anh
    private static final Map<String, String> DAY_OF_WEEK_MAPPING = new HashMap<>();
    static {
        DAY_OF_WEEK_MAPPING.put("Chủ Nhật", "SUNDAY");
        DAY_OF_WEEK_MAPPING.put("Thứ Hai", "MONDAY");
        DAY_OF_WEEK_MAPPING.put("Thứ Ba", "TUESDAY");
        DAY_OF_WEEK_MAPPING.put("Thứ Tư", "WEDNESDAY");
        DAY_OF_WEEK_MAPPING.put("Thứ Năm", "THURSDAY");
        DAY_OF_WEEK_MAPPING.put("Thứ Sáu", "FRIDAY");
        DAY_OF_WEEK_MAPPING.put("Thứ Bảy", "SATURDAY");
    }

    @GetMapping("/nurse-availability")
    public String showAvailabilityForm(Model model) {
        logger.debug("Hiển thị form chọn lịch làm việc");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            logger.warn("Người dùng chưa đăng nhập, chuyển hướng đến trang đăng nhập");
            return "redirect:/login";
        }

        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

            UserDTO userDTO = modelMapper.map(user, UserDTO.class);
            String role = user.getRole().name();
            userDTO.setRole(role.startsWith("ROLE_") ? role.substring(5) : role);
            logger.debug("UserDTO role: {}", userDTO.getRole());

            if (!"NURSE".equalsIgnoreCase(userDTO.getRole())) {
                logger.warn("User {} không có quyền truy cập form lịch làm việc", username);
                return "redirect:/";
            }

            NurseAvailabilityDTO availabilityDTO = nurseAvailabilityService.getAvailabilityByUserId(user.getUserId());
            model.addAttribute("availabilityDTO", availabilityDTO);
            model.addAttribute("user", userDTO);
            model.addAttribute("daysOfWeek", Arrays.asList("Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"));
            logger.info("Hiển thị form lịch làm việc cho user: {}", username);
            return "nurse/nurse-availability";
        } catch (Exception e) {
            logger.error("Lỗi khi hiển thị form lịch làm việc: {}", e.getMessage(), e);
            model.addAttribute("error", "Lỗi khi tải form: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/nurse-availability")
    public String submitAvailabilityForm(@ModelAttribute("availabilityDTO") NurseAvailabilityDTO availabilityDTO, Model model) {
        logger.debug("Xử lý submit form lịch làm việc");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            logger.warn("Người dùng chưa đăng nhập, chuyển hướng đến trang đăng nhập");
            return "redirect:/login";
        }

        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

            availabilityDTO.setUserId(user.getUserId());
            nurseAvailabilityService.createOrUpdateAvailability(user.getUserId(), availabilityDTO);
            logger.info("Cập nhật lịch làm việc thành công cho user: {}", username);
            model.addAttribute("success", "Cập nhật lịch làm việc thành công!");
            return "redirect:/nurse-schedule";
        } catch (Exception e) {
            logger.error("Lỗi khi lưu lịch làm việc: {}", e.getMessage(), e);
            model.addAttribute("error", "Lỗi khi lưu lịch làm việc: " + e.getMessage());
            model.addAttribute("daysOfWeek", Arrays.asList("Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"));
            return "nurse/nurse-availability";
        }
    }

    @GetMapping("/nurse-schedule")
    public String showSchedule(Model model) {
        logger.debug("Hiển thị lịch làm việc");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            logger.warn("Người dùng chưa đăng nhập, chuyển hướng đến trang đăng nhập");
            return "redirect:/login";
        }

        try {
            String username = authentication.getName();
            User nurseUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

            UserDTO userDTO = modelMapper.map(nurseUser, UserDTO.class);
            String role = nurseUser.getRole().name();
            userDTO.setRole(role.startsWith("ROLE_") ? role.substring(5) : role);
            logger.debug("UserDTO role: {}", userDTO.getRole());

            if (!"NURSE".equalsIgnoreCase(userDTO.getRole())) {
                logger.warn("User {} không có quyền truy cập lịch làm việc", username);
                return "redirect:/";
            }

            // Lấy lịch làm việc
            NurseAvailabilityDTO availabilityDTO = nurseAvailabilityService.getAvailabilityByUserId(nurseUser.getUserId());
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
            bookingsByDate.forEach((date, bookings) -> {
                logger.debug("Ngày {}: {} lịch đã chấp nhận", date, bookings.size());
                bookings.forEach(booking -> logger.debug("Booking: id={}, date={}, serviceType={}", booking.getBookingId(), booking.getBookingDate(), booking.getServiceType()));
            });

            // Log để kiểm tra weekDates và so sánh ngày
            logger.debug("Danh sách ngày trong tuần: {}", weekDates);
            logger.debug("Ngày hiện tại: {}", currentDate);
            for (LocalDate date : weekDates) {
                logger.debug("So sánh ngày {} với ngày hiện tại {}: {}", date, currentDate, date.equals(currentDate));
                logger.debug("Ngày {} có trong selectedDays {}: {}", date.getDayOfWeek().toString(), availabilityDTO.getSelectedDays(), availabilityDTO.getSelectedDays().contains(date.getDayOfWeek().toString()));
            }

            model.addAttribute("availabilityDTO", availabilityDTO);
            model.addAttribute("daysOfWeek", daysOfWeek);
            model.addAttribute("weekDates", weekDates);
            model.addAttribute("bookingsByDate", bookingsByDate);
            model.addAttribute("currentDate", currentDate);
            model.addAttribute("user", userDTO);
            logger.info("Hiển thị lịch làm việc cho user: {}", username);
            return "nurse/schedule";
        } catch (Exception e) {
            logger.error("Lỗi khi hiển thị lịch: {}", e.getMessage(), e);
            model.addAttribute("error", "Lỗi khi tải lịch: " + e.getMessage());
            return "error";
        }
    }
}