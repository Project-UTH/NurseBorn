package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.dtos.NurseAvailabilityDTO;
import edu.uth.nurseborn.dtos.UserDTO;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.repositories.UserRepository;
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

import java.util.Arrays;
import java.util.List;

@Controller
public class NurseAvailabilityController {

    private static final Logger logger = LoggerFactory.getLogger(NurseAvailabilityController.class);

    @Autowired
    private NurseAvailabilityService nurseAvailabilityService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

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
            return "redirect:/nurse-availability";
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
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

            UserDTO userDTO = modelMapper.map(user, UserDTO.class);
            String role = user.getRole().name();
            userDTO.setRole(role.startsWith("ROLE_") ? role.substring(5) : role);
            logger.debug("UserDTO role: {}", userDTO.getRole());

            if (!"NURSE".equalsIgnoreCase(userDTO.getRole())) {
                logger.warn("User {} không có quyền truy cập lịch làm việc", username);
                return "redirect:/";
            }

            NurseAvailabilityDTO availabilityDTO = nurseAvailabilityService.getAvailabilityByUserId(user.getUserId());
            model.addAttribute("availabilityDTO", availabilityDTO);
            model.addAttribute("user", userDTO);
            model.addAttribute("daysOfWeek", Arrays.asList("Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"));
            logger.info("Hiển thị lịch làm việc cho user: {}", username);
            return "nurse/schedule";
        } catch (Exception e) {
            logger.error("Lỗi khi hiển thị lịch làm việc: {}", e.getMessage(), e);
            model.addAttribute("error", "Lỗi khi tải lịch làm việc: " + e.getMessage());
            return "error";
        }
    }
}