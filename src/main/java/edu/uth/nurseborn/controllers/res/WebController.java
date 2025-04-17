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
import org.modelmapper.ModelMapper;
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
import java.util.ArrayList;
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

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/")
    public String home(Model model) {
        logger.debug("Hiển thị trang home");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            logger.debug("Người dùng chưa đăng nhập, hiển thị trang home mặc định");
            return "master/home";
        }

        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

            // Ánh xạ User sang UserDTO và sửa role
            UserDTO userDTO = modelMapper.map(user, UserDTO.class);
            String role = user.getRole().name();
            userDTO.setRole(role.startsWith("ROLE_") ? role.substring(5) : role);
            logger.debug("UserDTO role: {}", userDTO.getRole());
            model.addAttribute("user", userDTO);

            if ("ADMIN".equalsIgnoreCase(userDTO.getRole())) {
                logger.info("Admin đăng nhập, điều hướng đến trang home-admin");
                return "redirect:/admin/home";
            } else if ("FAMILY".equalsIgnoreCase(userDTO.getRole())) {
                FamilyProfileDTO familyProfile = familyProfileService.getFamilyProfileByUserId(user.getUserId());
                logger.debug("FamilyProfile cho userId {}: {}", user.getUserId(), familyProfile);
                model.addAttribute("familyProfile", familyProfile != null ? familyProfile : new FamilyProfileDTO());
            } else if ("NURSE".equalsIgnoreCase(userDTO.getRole())) {
                NurseProfileDTO nurseProfile = nurseProfileService.getNurseProfileByUserId(user.getUserId());
                logger.debug("NurseProfile cho userId {}: {}", user.getUserId(), nurseProfile);
                model.addAttribute("nurseProfile", nurseProfile != null ? nurseProfile : new NurseProfileDTO());
            }

            logger.info("Hiển thị trang home cho user: {}", username);
            return "master/home";
        } catch (Exception e) {
            logger.error("Lỗi khi hiển thị trang home: {}", e.getMessage(), e);
            return "master/home";
        }
    }

    @GetMapping("/admin/home")
    public String adminHome(Model model) {
        logger.debug("Hiển thị trang home-admin");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            logger.warn("Người dùng chưa đăng nhập, chuyển hướng đến trang đăng nhập");
            return "redirect:/login";
        }

        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

            // Ánh xạ User sang UserDTO và sửa role
            UserDTO userDTO = modelMapper.map(user, UserDTO.class);
            String role = user.getRole().name();
            userDTO.setRole(role.startsWith("ROLE_") ? role.substring(5) : role);
            logger.debug("UserDTO role: {}", userDTO.getRole());

            if (!"ADMIN".equalsIgnoreCase(userDTO.getRole())) {
                logger.warn("User {} không có quyền truy cập trang home-admin", username);
                return "redirect:/";
            }

            model.addAttribute("user", userDTO);
            logger.info("Hiển thị trang home-admin cho user: {}", username);
            return "admin/home-admin";
        } catch (Exception e) {
            logger.error("Lỗi khi hiển thị trang home-admin: {}", e.getMessage(), e);
            return "redirect:/login";
        }
    }

    @GetMapping("/login")
    public String login(Model model) {
        logger.debug("Hiển thị form đăng nhập");
        model.addAttribute("loginRequest", new LoginRequestDTO());
        return "auth/auth-login-basic";
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

            return "redirect:/";
        } catch (Exception e) {
            logger.error("Lỗi khi đăng nhập: {}", e.getMessage(), e);
            model.addAttribute("error", e.getMessage());
            return "auth/auth-login-basic";
        }
    }

    @GetMapping("/role-selection")
    public String roleSelection(Model model) {
        logger.debug("Hiển thị modal chọn vai trò");
        return "auth/role-selection";
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
        return "auth/auth-register-basic-nurse";
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
        return "auth/auth-register-basic-family";
    }

    @PostMapping("/register")
    public String register(
            @ModelAttribute("registerRequest") RegisterRequestDTO registerRequest,
            @RequestParam(value = "certificates", required = false) List<MultipartFile> certificates,
            @RequestParam(value = "certificateNames", required = false) List<String> certificateNames,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            Model model,
            HttpSession session) {
        try {
            logger.debug("Nhận yêu cầu POST /register với dữ liệu: {}", registerRequest);
            logger.debug("Certificates: {}, CertificateNames: {}, ProfileImage: {}",
                    certificates != null ? certificates.size() : "null", certificateNames != null ? certificateNames.size() : "null", profileImage != null ? profileImage.getOriginalFilename() : "null");

            String role = registerRequest.getUser().getRole();
            if (role == null || role.trim().isEmpty()) {
                logger.error("Vai trò không được cung cấp hoặc rỗng");
                model.addAttribute("error", "Vai trò không được để trống");
                return "auth/auth-register-basic-nurse";
            }

            if ("NURSE".equalsIgnoreCase(role)) {
                logger.debug("Tạo NurseProfile với skills: {}, experienceYears: {}", registerRequest.getNurseProfile().getSkills(), registerRequest.getNurseProfile().getExperienceYears());
                NurseProfileDTO nurseProfileDTO = registerRequest.getNurseProfile() != null ? registerRequest.getNurseProfile() : new NurseProfileDTO();
                nurseProfileDTO.setLocation(registerRequest.getUser().getAddress());
                nurseProfileDTO.setApproved(false);

                if (profileImage != null && !profileImage.isEmpty()) {
                    try {
                        String uploadDir = "src/main/resources/static/uploads/profile_images/";
                        File dir = new File(uploadDir);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        String fileName = System.currentTimeMillis() + "_" + profileImage.getOriginalFilename();
                        Path filePath = Paths.get(uploadDir, fileName);
                        Files.write(filePath, profileImage.getBytes());
                        nurseProfileDTO.setProfileImage("/uploads/profile_images/" + fileName);
                    } catch (Exception e) {
                        logger.error("Lỗi khi lưu ảnh đại diện: {}", e.getMessage(), e);
                        model.addAttribute("error", "Lỗi khi lưu ảnh đại diện: " + e.getMessage());
                        return "auth/auth-register-basic-nurse";
                    }
                }

                if (certificates != null && !certificates.isEmpty()) {
                    try {
                        String uploadDir = "src/main/resources/static/uploads/certificates/";
                        File dir = new File(uploadDir);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        List<CertificateDTO> certificateDTOs = new ArrayList<>();
                        for (int i = 0; i < certificates.size(); i++) {
                            MultipartFile file = certificates.get(i);
                            if (!file.isEmpty()) {
                                String contentType = file.getContentType();
                                if (!contentType.equals("application/pdf") && !contentType.startsWith("image/")) {
                                    throw new RuntimeException("Chỉ hỗ trợ file PDF hoặc hình ảnh");
                                }
                                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                                Path filePath = Paths.get(uploadDir, fileName);
                                Files.write(filePath, file.getBytes());

                                CertificateDTO certificateDTO = new CertificateDTO();
                                String certificateName = (certificateNames != null && i < certificateNames.size()) ? certificateNames.get(i) : file.getOriginalFilename();
                                certificateDTO.setCertificateName(certificateName);
                                certificateDTO.setFilePath("/uploads/certificates/" + fileName);
                                certificateDTOs.add(certificateDTO);
                            }
                        }
                        nurseProfileDTO.setCertificates(certificateDTOs);
                    } catch (Exception e) {
                        logger.error("Lỗi khi lưu chứng chỉ: {}", e.getMessage(), e);
                        model.addAttribute("error", "Lỗi khi lưu chứng chỉ: " + e.getMessage());
                        return "auth/auth-register-basic-nurse";
                    }
                }

                registerRequest.setNurseProfile(nurseProfileDTO);
            } else if ("FAMILY".equalsIgnoreCase(role)) {
                logger.debug("Tạo FamilyProfile với specificNeeds: {}", registerRequest.getFamilyProfile() != null ? registerRequest.getFamilyProfile().getSpecificNeeds() : "null");
                FamilyProfileDTO familyProfileDTO = registerRequest.getFamilyProfile() != null ? registerRequest.getFamilyProfile() : new FamilyProfileDTO();
                registerRequest.setFamilyProfile(familyProfileDTO);
            } else {
                logger.error("Vai trò không hợp lệ: {}", role);
                model.addAttribute("error", "Vai trò không hợp lệ: " + role);
                return "auth/auth-register-basic-nurse";
            }

            userService.registerUser(registerRequest);
            session.setAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
            logger.info("Đăng ký thành công cho username: {}", registerRequest.getUser().getUsername());
            return "redirect:/login";
        } catch (Exception e) {
            logger.error("Lỗi khi đăng ký: {}", e.getMessage(), e);
            model.addAttribute("error", e.getMessage());
            String role = registerRequest.getUser() != null && registerRequest.getUser().getRole() != null
                    ? registerRequest.getUser().getRole().toLowerCase()
                    : "nurse";
            return "auth/auth-register-basic-" + role;
        }
    }

    @GetMapping("/user-profile")
    public String userProfile(Model model) {
        logger.debug("Hiển thị trang hồ sơ người dùng");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            logger.debug("Người dùng chưa đăng nhập, chuyển hướng đến trang đăng nhập");
            return "redirect:/login";
        }

        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

            // Ánh xạ User sang UserDTO và sửa role
            UserDTO userDTO = modelMapper.map(user, UserDTO.class);
            String role = user.getRole().name();
            userDTO.setRole(role.startsWith("ROLE_") ? role.substring(5) : role);
            logger.debug("UserDTO role: {}", userDTO.getRole());
            model.addAttribute("user", userDTO);

            // Log chi tiết để kiểm tra trạng thái của user
            logger.debug("UserDTO details: username={}, role={}, createdAt={}",
                    userDTO.getUsername(), userDTO.getRole(), userDTO.getCreatedAt());

            if ("FAMILY".equalsIgnoreCase(userDTO.getRole())) {
                FamilyProfileDTO familyProfile = familyProfileService.getFamilyProfileByUserId(user.getUserId());
                logger.debug("FamilyProfile cho userId {}: {}", user.getUserId(), familyProfile);
                if (familyProfile != null) {
                    logger.debug("FamilyProfile details: childName={}, childAge={}, specificNeeds={}, preferredLocation={}",
                            familyProfile.getChildName(), familyProfile.getChildAge(),
                            familyProfile.getSpecificNeeds(), familyProfile.getPreferredLocation());
                }
                model.addAttribute("familyProfile", familyProfile != null ? familyProfile : new FamilyProfileDTO());
            } else if ("NURSE".equalsIgnoreCase(userDTO.getRole())) {
                NurseProfileDTO nurseProfile = nurseProfileService.getNurseProfileByUserId(user.getUserId());
                logger.debug("NurseProfile cho userId {}: {}", user.getUserId(), nurseProfile);
                if (nurseProfile != null) {
                    logger.debug("NurseProfile details: bio={}, skills={}, experienceYears={}",
                            nurseProfile.getBio(), nurseProfile.getSkills(), nurseProfile.getExperienceYears());
                }
                model.addAttribute("nurseProfile", nurseProfile != null ? nurseProfile : new NurseProfileDTO());
            }

            logger.info("Hiển thị trang hồ sơ cho user: {}", username);
            return "profile/user-profile";
        } catch (Exception e) {
            logger.error("Lỗi khi hiển thị trang hồ sơ: {}", e.getMessage(), e);
            return "redirect:/login";
        }
    }

    @GetMapping("/admin-dashboard")
    public String adminDashboard(Model model, HttpServletRequest request) {
        logger.debug("Hiển thị admin dashboard");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            logger.warn("Người dùng chưa đăng nhập, chuyển hướng đến trang đăng nhập");
            return "redirect:/login";
        }

        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

            // Ánh xạ User sang UserDTO và sửa role
            UserDTO userDTO = modelMapper.map(user, UserDTO.class);
            String role = user.getRole().name();
            userDTO.setRole(role.startsWith("ROLE_") ? role.substring(5) : role);
            logger.debug("UserDTO role: {}", userDTO.getRole());

            if (!"ADMIN".equalsIgnoreCase(userDTO.getRole())) {
                logger.warn("User {} không có quyền truy cập admin dashboard", username);
                return "redirect:/";
            }

            model.addAttribute("user", userDTO);
            logger.info("Hiển thị admin dashboard cho user: {}", username);
            return "auth/admin-dashboard";
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
        SecurityContextHolder.clearContext();
        logger.info("Đăng xuất thành công");
        return "redirect:/login?logout";
    }
    @GetMapping("/update-user")
    public String updateUserProfile(Model model) {
        logger.debug("Hiển thị trang cập nhật hồ sơ người dùng");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            logger.debug("Người dùng chưa đăng nhập, chuyển hướng đến trang đăng nhập");
            return "redirect:/login";
        }
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
            UserDTO userDTO = modelMapper.map(user, UserDTO.class);
            String role = user.getRole().name();
            userDTO.setRole(role.startsWith("ROLE_") ? role.substring(5) : role);
            model.addAttribute("user", userDTO);
            if ("FAMILY".equalsIgnoreCase(userDTO.getRole())) {
                FamilyProfileDTO familyProfile = familyProfileService.getFamilyProfileByUserId(user.getUserId());
                model.addAttribute("familyProfile", familyProfile != null ? familyProfile : new FamilyProfileDTO());
            } else if ("NURSE".equalsIgnoreCase(userDTO.getRole())) {
                NurseProfileDTO nurseProfile = nurseProfileService.getNurseProfileByUserId(user.getUserId());
                model.addAttribute("nurseProfile", nurseProfile != null ? nurseProfile : new NurseProfileDTO());
            }
            logger.info("Hiển thị trang cập nhật hồ sơ cho user: {}", username);
            return "profile/update-user";
        } catch (Exception e) {
            logger.error("Lỗi khi hiển thị trang cập nhật hồ sơ: {}", e.getMessage(), e);
            return "redirect:/login";
        }
    }
    @PostMapping("/update-user")
    public String updateUserProfile(
            @ModelAttribute("user") UserDTO userDTO,
            @ModelAttribute("familyProfile") FamilyProfileDTO familyProfileDTO,
            Model model, HttpSession session) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

            // Kiểm tra vai trò FAMILY
            String role = user.getRole().name().startsWith("ROLE_") ?
                    user.getRole().name().substring(5) : user.getRole().name();
            if (!"FAMILY".equalsIgnoreCase(role)) {
                logger.warn("User {} không có quyền cập nhật hồ sơ gia đình", username);
                model.addAttribute("error", "Chỉ người dùng có vai trò FAMILY mới có thể cập nhật hồ sơ gia đình");
                return "profile/update-user";
            }

            // Cập nhật thông tin người dùng
            user.setFullName(userDTO.getFullName());
            user.setEmail(userDTO.getEmail());
            user.setPhoneNumber(userDTO.getPhoneNumber());
            user.setAddress(userDTO.getAddress());
            userRepository.save(user);
            logger.debug("Đã cập nhật thông tin người dùng cho user: {}", username);

            // Cập nhật hồ sơ gia đình
            familyProfileDTO.setUserId(user.getUserId());
            FamilyProfileDTO updatedProfile = familyProfileService.updateFamilyProfile(user.getUserId(), familyProfileDTO);
            logger.debug("Đã cập nhật hồ sơ gia đình cho userId: {}", user.getUserId());

            session.setAttribute("success", "Cập nhật hồ sơ thành công!");
            logger.info("Cập nhật hồ sơ thành công cho user: {}", username);
            return "redirect:/user-profile";
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật hồ sơ: {}", e.getMessage(), e);
            model.addAttribute("error", "Lỗi khi cập nhật hồ sơ: " + e.getMessage());
            return "profile/update-user";
        }
    }
}