package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.dtos.BookingDTO;
import edu.uth.nurseborn.dtos.NurseAvailabilityDTO;
import edu.uth.nurseborn.dtos.UserDTO;
import edu.uth.nurseborn.models.NurseProfile;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.models.enums.Role;
import edu.uth.nurseborn.repositories.NurseProfileRepository;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

    @Autowired
    private NurseAvailabilityService nurseAvailabilityService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NurseProfileRepository nurseProfileRepository;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/nurse-list")
    public String showNurseList(Model model) {
        logger.debug("Hiển thị danh sách y tá");

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

            if (!"FAMILY".equalsIgnoreCase(userDTO.getRole())) {
                logger.warn("User {} không có quyền xem danh sách y tá", username);
                return "redirect:/";
            }

            List<NurseProfile> nurseProfiles = nurseProfileRepository.findByUserUserIdInAndIsApproved(
                    userRepository.findByRoleAndIsVerified(Role.NURSE, true).stream()
                            .map(User::getUserId)
                            .toList(),
                    true
            );

            model.addAttribute("nurses", nurseProfiles);
            model.addAttribute("user", userDTO);
            logger.info("Hiển thị danh sách y tá cho user: {}", username);
            return "family/nursepage";
        } catch (Exception e) {
            logger.error("Lỗi khi hiển thị danh sách y tá: {}", e.getMessage(), e);
            model.addAttribute("error", "Lỗi khi tải danh sách y tá: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/set_service")
    public String showBookingForm(@RequestParam(value = "nurseUserId", required = false) Long nurseUserId, Model model) {
        logger.debug("Hiển thị form đặt lịch cho y tá: {}", nurseUserId);

        if (nurseUserId == null) {
            logger.warn("Thiếu tham số nurseUserId trong yêu cầu");
            model.addAttribute("error", "Thiếu thông tin y tá. Vui lòng chọn y tá từ danh sách.");
            return "error";
        }

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

            if (!"FAMILY".equalsIgnoreCase(userDTO.getRole())) {
                logger.warn("User {} không có quyền đặt lịch", username);
                return "redirect:/";
            }

            User nurseUser = userRepository.findById(nurseUserId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy y tá với ID: " + nurseUserId));
            NurseProfile nurseProfile = nurseProfileRepository.findByUserUserId(nurseUserId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy NurseProfile cho y tá với ID: " + nurseUserId));

            if (!nurseProfile.getApproved()) {
                logger.warn("Y tá {} chưa được phê duyệt", nurseUser.getUsername());
                model.addAttribute("error", "Y tá này chưa được phê duyệt");
                return "error";
            }

            // Đặt giá trị mặc định nếu các trường giá là null
            if (nurseProfile.getHourlyRate() == null) nurseProfile.setHourlyRate(0.0);
            if (nurseProfile.getDailyRate() == null) nurseProfile.setDailyRate(0.0);
            if (nurseProfile.getWeeklyRate() == null) nurseProfile.setWeeklyRate(0.0);

            NurseAvailabilityDTO availabilityDTO = nurseAvailabilityService.getAvailabilityByUserId(nurseUserId);

            BookingDTO bookingDTO = new BookingDTO();
            bookingDTO.setNurseUserId(nurseUserId);

            model.addAttribute("bookingDTO", bookingDTO);
            model.addAttribute("nurse", modelMapper.map(nurseUser, UserDTO.class));
            model.addAttribute("nurseProfile", nurseProfile);
            model.addAttribute("availability", availabilityDTO);
            model.addAttribute("user", userDTO);
            model.addAttribute("serviceTypes", List.of("HOURLY", "DAILY", "WEEKLY"));
            logger.info("Hiển thị form đặt lịch cho user: {} và y tá: {}", username, nurseUser.getUsername());
            return "family/book-nurse";
        } catch (Exception e) {
            logger.error("Lỗi khi hiển thị form đặt lịch: {}", e.getMessage(), e);
            model.addAttribute("error", "Lỗi khi tải form đặt lịch: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/set_service")
    public String submitBookingForm(@ModelAttribute("bookingDTO") BookingDTO bookingDTO, Model model, RedirectAttributes redirectAttributes) {
        logger.debug("Xử lý submit form đặt lịch");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            logger.warn("Người dùng chưa đăng nhập, chuyển hướng đến trang đăng nhập");
            return "redirect:/login";
        }

        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

            bookingService.createBooking(bookingDTO, user.getUserId());
            logger.info("Đặt lịch thành công cho user: {}", username);

            // Thiết lập thông báo thành công
            redirectAttributes.addFlashAttribute("success", "Đặt lịch thành công!");

            // Chuyển hướng về trang đặt lịch với cùng nurseUserId
            return "redirect:/set_service?nurseUserId=" + bookingDTO.getNurseUserId();

        } catch (Exception e) {
            logger.error("Lỗi khi đặt lịch: {}", e.getMessage(), e);

            // Lấy lại thông tin y tá và các dữ liệu cần thiết để hiển thị lại form
            Long nurseUserId = bookingDTO.getNurseUserId();
            User nurseUser = userRepository.findById(nurseUserId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy y tá với ID: " + nurseUserId));
            NurseProfile nurseProfile = nurseProfileRepository.findByUserUserId(nurseUserId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy NurseProfile cho y tá với ID: " + nurseUserId));
            NurseAvailabilityDTO availabilityDTO = nurseAvailabilityService.getAvailabilityByUserId(nurseUserId);

            // Đặt giá trị mặc định nếu các trường giá là null
            if (nurseProfile.getHourlyRate() == null) nurseProfile.setHourlyRate(0.0);
            if (nurseProfile.getDailyRate() == null) nurseProfile.setDailyRate(0.0);
            if (nurseProfile.getWeeklyRate() == null) nurseProfile.setWeeklyRate(0.0);

            // Thêm các đối tượng cần thiết vào model
            model.addAttribute("error", "Lỗi khi đặt lịch: " + e.getMessage());
            model.addAttribute("bookingDTO", bookingDTO);
            model.addAttribute("nurse", modelMapper.map(nurseUser, UserDTO.class));
            model.addAttribute("nurseProfile", nurseProfile);
            model.addAttribute("availability", availabilityDTO);
            model.addAttribute("serviceTypes", List.of("HOURLY", "DAILY", "WEEKLY"));

            return "family/book-nurse";
        }
    }
}