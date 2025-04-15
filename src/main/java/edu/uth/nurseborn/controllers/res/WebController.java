package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.dtos.*;
import edu.uth.nurseborn.models.*;
import edu.uth.nurseborn.repositories.UserRepository;
import edu.uth.nurseborn.services.*;
import edu.uth.nurseborn.components.JwtTokenUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
public class WebController {

    private static final Logger logger = LoggerFactory.getLogger(WebController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenUtils jwtTokenUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NurseProfileService nurseProfileService;

    @Autowired
    private FamilyProfileService familyProfileService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private EarningService earningService;

    @Autowired
    private TokenCleanupService tokenCleanupService;

    @GetMapping("/")
    public String home(Model model) {
        logger.debug("Hiển thị trang home");

        // Kiểm tra trạng thái đăng nhập bằng SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            logger.debug("Người dùng chưa đăng nhập, hiển thị trang home mặc định");
            return "master/home";
        }

        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

            model.addAttribute("user", user);

            if ("NURSE".equalsIgnoreCase(user.getRole().name())) {
                NurseProfileDTO nurseProfile = nurseProfileService.getNurseProfileByUserId(user.getUserId());
                model.addAttribute("nurseProfile", nurseProfile);
            } else if ("FAMILY".equalsIgnoreCase(user.getRole().name())) {
                FamilyProfileDTO familyProfile = familyProfileService.getFamilyProfileByUserId(user.getUserId());
                model.addAttribute("familyProfile", familyProfile);
            }

            logger.info("Hiển thị trang home cho user: {}", username);
            return "master/home";
        } catch (Exception e) {
            logger.error("Lỗi khi hiển thị trang home: {}", e.getMessage(), e);
            return "master/home";
        }
    }

    @GetMapping("/login")
    public String login(Model model) {
        logger.debug("Hiển thị form đăng nhập");
        model.addAttribute("loginRequest", new LoginRequestDTO());
        return "master/auth-login-basic";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("loginRequest") LoginRequestDTO loginRequest, Model model, HttpServletResponse response) {
        try {
            logger.debug("Xử lý yêu cầu đăng nhập với username: {}", loginRequest.getUsername());
            LoginResponseDTO loginResponse = userService.login(loginRequest);
            Cookie cookie = new Cookie("token", loginResponse.getToken());
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            response.addCookie(cookie);
            logger.info("Đăng nhập thành công cho username: {}", loginRequest.getUsername());

            // Chuyển hướng đến trang home cho tất cả người dùng
            return "redirect:/";
        } catch (Exception e) {
            logger.error("Lỗi khi đăng nhập: {}", e.getMessage(), e);
            model.addAttribute("error", e.getMessage());
            return "master/auth-login-basic";
        }
    }

    @GetMapping("/role-selection")
    public String roleSelection(Model model) {
        logger.debug("Hiển thị modal chọn vai trò");
        return "master/role-selection";
    }

    @GetMapping("/register/nurse")
    public String registerNurse(Model model) {
        logger.debug("Hiển thị form đăng ký tài khoản y tá");
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        UserDTO userDTO = new UserDTO();
        userDTO.setRole("NURSE");
        registerRequest.setUser(userDTO);
        model.addAttribute("registerRequest", registerRequest);
        model.addAttribute("userDTO", userDTO);
        model.addAttribute("nurseProfileDTO", new NurseProfileDTO());
        return "master/auth-register-basic-nurse";
    }

    @GetMapping("/register/family")
    public String registerFamily(Model model) {
        logger.debug("Hiển thị form đăng ký tài khoản gia đình");
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        UserDTO userDTO = new UserDTO();
        userDTO.setRole("FAMILY");
        registerRequest.setUser(userDTO);
        model.addAttribute("registerRequest", registerRequest);
        model.addAttribute("userDTO", userDTO);
        model.addAttribute("familyProfileDTO", new FamilyProfileDTO());
        return "master/auth-register-basic-family";
    }

    @PostMapping("/register")
    public String register(
            @ModelAttribute("registerRequest") RegisterRequestDTO registerRequest,
            @RequestParam(value = "certificates", required = false) List<MultipartFile> certificates,
            @RequestParam(value = "experienceYears", required = false) Integer experienceYears,
            @RequestParam(value = "hourlyRate", required = false) Double hourlyRate,
            @RequestParam(value = "dailyRate", required = false) Double dailyRate,
            @RequestParam(value = "weeklyRate", required = false) Double weeklyRate,
            @RequestParam(value = "skills", required = false) String skills,
            @RequestParam(value = "bio", required = false) String bio,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            Model model,
            HttpSession session) {
        try {
            logger.debug("Nhận yêu cầu POST /register với dữ liệu: {}", registerRequest);
            logger.debug("Certificates: {}, Skills: {}, ExperienceYears: {}, Bio: {}, ProfileImage: {}",
                    certificates, skills, experienceYears, bio, profileImage != null ? profileImage.getOriginalFilename() : "null");

            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(registerRequest.getUser().getUsername());
            userDTO.setPassword(registerRequest.getUser().getPassword());
            userDTO.setEmail(registerRequest.getUser().getEmail());
            userDTO.setRole(registerRequest.getUser().getRole());
            userDTO.setFullName(registerRequest.getUser().getFullName());
            userDTO.setPhoneNumber(registerRequest.getUser().getPhoneNumber());
            userDTO.setAddress(registerRequest.getUser().getAddress());
            registerRequest.setUser(userDTO);

            String role = userDTO.getRole();
            if (role == null || role.trim().isEmpty()) {
                logger.error("Vai trò không được cung cấp hoặc rỗng");
                model.addAttribute("error", "Vai trò không được để trống");
                return "master/auth-register-basic-nurse";
            }

            if ("NURSE".equalsIgnoreCase(role)) {
                logger.debug("Tạo NurseProfile với skills: {}, experienceYears: {}", skills, experienceYears);
                NurseProfileDTO nurseProfileDTO = new NurseProfileDTO();
                nurseProfileDTO.setLocation(userDTO.getAddress());
                nurseProfileDTO.setSkills(skills);
                nurseProfileDTO.setExperienceYears(experienceYears);
                nurseProfileDTO.setHourlyRate(hourlyRate);
                nurseProfileDTO.setDailyRate(dailyRate);
                nurseProfileDTO.setWeeklyRate(weeklyRate);
                nurseProfileDTO.setBio(bio);
                nurseProfileDTO.setApproved(false);

                // Xử lý ảnh đại diện nếu có
                if (profileImage != null && !profileImage.isEmpty()) {
                    try {
                        String uploadDir = "uploads/profile_images/";
                        File dir = new File(uploadDir);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        String fileName = System.currentTimeMillis() + "_" + profileImage.getOriginalFilename();
                        Path filePath = Paths.get(uploadDir, fileName);
                        Files.write(filePath, profileImage.getBytes());
                        nurseProfileDTO.setProfileImage(filePath.toString());
                    } catch (Exception e) {
                        logger.error("Lỗi khi lưu ảnh đại diện: {}", e.getMessage(), e);
                        model.addAttribute("error", "Lỗi khi lưu ảnh đại diện: " + e.getMessage());
                        return "master/auth-register-basic-nurse";
                    }
                }

                registerRequest.setNurseProfile(nurseProfileDTO);
                registerRequest.setCertificates(certificates);
            } else if ("FAMILY".equalsIgnoreCase(role)) {
                logger.debug("Tạo FamilyProfile với familySize: {}, specificNeeds: {}",
                        registerRequest.getFamilyProfile().getFamilySize(),
                        registerRequest.getFamilyProfile().getSpecificNeeds());
                FamilyProfileDTO familyProfileDTO = new FamilyProfileDTO();
                familyProfileDTO.setFamilySize(registerRequest.getFamilyProfile().getFamilySize());
                familyProfileDTO.setSpecificNeeds(registerRequest.getFamilyProfile().getSpecificNeeds());
                familyProfileDTO.setPreferredLocation(registerRequest.getFamilyProfile().getPreferredLocation());
                registerRequest.setFamilyProfile(familyProfileDTO);
            } else {
                logger.error("Vai trò không hợp lệ: {}", role);
                model.addAttribute("error", "Vai trò không hợp lệ: " + role);
                return "master/auth-register-basic-nurse";
            }

            userService.registerUser(registerRequest);
            session.setAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
            logger.info("Đăng ký thành công cho username: {}", userDTO.getUsername());
            return "redirect:/login";
        } catch (Exception e) {
            logger.error("Lỗi khi đăng ký: {}", e.getMessage(), e);
            model.addAttribute("error", e.getMessage());
            String role = registerRequest.getUser() != null && registerRequest.getUser().getRole() != null
                    ? registerRequest.getUser().getRole().toLowerCase()
                    : "nurse";
            return "master/auth-register-basic-" + role;
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpServletRequest request) {
        logger.debug("Hiển thị dashboard");

        // Kiểm tra trạng thái đăng nhập bằng SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            logger.warn("Người dùng chưa đăng nhập, chuyển hướng đến trang đăng nhập");
            return "redirect:/login";
        }

        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

            model.addAttribute("user", user);

            if ("NURSE".equalsIgnoreCase(user.getRole().name())) {
                NurseProfileDTO nurseProfile = nurseProfileService.getNurseProfileByUserId(user.getUserId());
                model.addAttribute("nurseProfile", nurseProfile);
            } else if ("FAMILY".equalsIgnoreCase(user.getRole().name())) {
                FamilyProfileDTO familyProfile = familyProfileService.getFamilyProfileByUserId(user.getUserId());
                model.addAttribute("familyProfile", familyProfile);
            } else if ("ADMIN".equalsIgnoreCase(user.getRole().name())) {
                return "redirect:/admin-dashboard";
            }

            logger.info("Hiển thị dashboard cho user: {}", username);
            return "master/dashboard";
        } catch (Exception e) {
            logger.error("Lỗi khi hiển thị dashboard: {}", e.getMessage(), e);
            return "redirect:/login";
        }
    }

    @GetMapping("/admin-dashboard")
    public String adminDashboard(Model model, HttpServletRequest request) {
        logger.debug("Hiển thị admin dashboard");

        // Kiểm tra trạng thái đăng nhập bằng SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            logger.warn("Người dùng chưa đăng nhập, chuyển hướng đến trang đăng nhập");
            return "redirect:/login";
        }

        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

            if (!"ADMIN".equalsIgnoreCase(user.getRole().name())) {
                logger.warn("User {} không có quyền truy cập admin dashboard", username);
                return "redirect:/dashboard";
            }

            model.addAttribute("user", user);
            logger.info("Hiển thị admin dashboard cho user: {}", username);
            return "master/admin-dashboard";
        } catch (Exception e) {
            logger.error("Lỗi khi hiển thị admin dashboard: {}", e.getMessage(), e);
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        logger.debug("Xử lý yêu cầu đăng xuất");
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        SecurityContextHolder.clearContext(); // Xóa thông tin xác thực
        logger.info("Đăng xuất thành công");
        return "redirect:/login?logout";
    }
}