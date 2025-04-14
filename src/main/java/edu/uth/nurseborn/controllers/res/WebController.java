package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.dtos.BookingDTO;
import edu.uth.nurseborn.dtos.LoginRequestDTO;
import edu.uth.nurseborn.dtos.NurseProfileDTO;
import edu.uth.nurseborn.dtos.FamilyProfileDTO;
import edu.uth.nurseborn.dtos.RegisterRequestDTO;
import edu.uth.nurseborn.dtos.UserDTO;
import edu.uth.nurseborn.models.*;
import edu.uth.nurseborn.repositories.UserRepository;
import edu.uth.nurseborn.services.*;
import edu.uth.nurseborn.components.JwtTokenUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class WebController {

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
    private BookingService bookingService; // Thay JobRequestService và HiredNurseService bằng BookingService

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private EarningService earningService;

    @Autowired
    private AdminActionService adminActionService;

    @Autowired
    private TokenCleanupService tokenCleanupService;

    @GetMapping("/")
    public String home() {
        return "master/home";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("loginRequest", new LoginRequestDTO());
        return "master/auth-login-basic";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("loginRequest") LoginRequestDTO loginRequest, Model model, HttpServletResponse response) {
        try {
            var loginResponse = userService.login(loginRequest);
            Cookie cookie = new Cookie("token", loginResponse.getToken());
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            response.addCookie(cookie);
            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "master/auth-login-basic";
        }
    }

    @GetMapping("/role-selection")
    public String roleSelection(Model model) {
        return "master/role-selection";
    }

    @GetMapping("/register/nurse")
    public String registerNurse(Model model) {
        model.addAttribute("registerRequest", new RegisterRequestDTO());
        model.addAttribute("userDTO", new UserDTO());
        model.addAttribute("nurseProfileDTO", new NurseProfileDTO());
        return "master/auth-register-basic-nurse";
    }

    @GetMapping("/register/family")
    public String registerFamily(Model model) {
        model.addAttribute("registerRequest", new RegisterRequestDTO());
        model.addAttribute("userDTO", new UserDTO());
        model.addAttribute("familyProfileDTO", new FamilyProfileDTO());
        return "master/auth-register-basic-family";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("registerRequest") RegisterRequestDTO registerRequest, Model model) {
        try {
            userService.registerUser(registerRequest);
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            String role = registerRequest.getUser().getRole();
            return "master/auth-register-basic-" + role.toLowerCase();
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpServletRequest request) {
        String token = getTokenFromCookies(request);
        if (token == null) {
            return "redirect:/login";
        }

        try {
            String username = jwtTokenUtil.extractUsername(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!jwtTokenUtil.validateToken(token, (UserDetails) user)) {
                return "redirect:/login";
            }

            model.addAttribute("user", user);
            if ("NURSE".equalsIgnoreCase(user.getRole().name())) {
                NurseProfileDTO nurseProfile = nurseProfileService.getNurseProfileByUserId(user.getUserId());
                model.addAttribute("nurseProfile", nurseProfile);
            } else if ("FAMILY".equalsIgnoreCase(user.getRole().name())) {
                FamilyProfileDTO familyProfile = familyProfileService.getFamilyProfileByUserId(user.getUserId());
                model.addAttribute("familyProfile", familyProfile);
            }
            return "master/masterpage";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    @GetMapping("/create-profile")
    public String createProfile(Model model, HttpServletRequest request) {
        String token = getTokenFromCookies(request);
        if (token == null) {
            return "redirect:/login";
        }

        try {
            String username = jwtTokenUtil.extractUsername(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!jwtTokenUtil.validateToken(token, (UserDetails) user)) {
                return "redirect:/login";
            }

            model.addAttribute("user", user);
            return "master/create-profile";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    @GetMapping("/manage-services")
    public String manageServices(Model model, HttpServletRequest request) {
        String token = getTokenFromCookies(request);
        if (token == null) {
            return "redirect:/login";
        }

        try {
            String username = jwtTokenUtil.extractUsername(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!jwtTokenUtil.validateToken(token, (UserDetails) user)) {
                return "redirect:/login";
            }

            model.addAttribute("user", user);
            return "master/manage-services";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    @GetMapping("/hired-nurses")
    public String hiredNurses(Model model, HttpServletRequest request) {
        String token = getTokenFromCookies(request);
        if (token == null) {
            return "redirect:/login";
        }

        try {
            String username = jwtTokenUtil.extractUsername(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!jwtTokenUtil.validateToken(token, (UserDetails) user) || !"FAMILY".equalsIgnoreCase(user.getRole().name())) {
                return "redirect:/login";
            }

            // Lấy danh sách booking của family user
            List<BookingDTO> bookings = bookingService.getBookingsByFamilyUserId(user.getUserId().intValue());
            // Lọc các booking có trạng thái "ACCEPTED" hoặc "COMPLETED" để xem như "hired nurses"
            List<BookingDTO> hiredNurses = bookings.stream()
                    .filter(booking -> "ACCEPTED".equalsIgnoreCase(booking.getStatus().toString()) || "COMPLETED".equalsIgnoreCase(booking.getStatus().toString()))
                    .collect(Collectors.toList());
            model.addAttribute("user", user);
            model.addAttribute("hiredNurses", hiredNurses);
            return "master/hired-nurses";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    @GetMapping("/job-requests")
    public String jobRequests(Model model, HttpServletRequest request) {
        String token = getTokenFromCookies(request);
        if (token == null) {
            return "redirect:/login";
        }

        try {
            String username = jwtTokenUtil.extractUsername(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!jwtTokenUtil.validateToken(token, (UserDetails) user) || !"NURSE".equalsIgnoreCase(user.getRole().name())) {
                return "redirect:/login";
            }

            // Lấy danh sách booking liên quan đến nurse user
            List<BookingDTO> bookings = bookingService.getBookingsByNurseUserId(user.getUserId().intValue());
            // Lọc các booking có trạng thái "PENDING" để xem như "job requests"
            List<BookingDTO> jobRequests = bookings.stream()
                    .filter(booking -> "PENDING".equalsIgnoreCase(booking.getStatus().toString()))
                    .collect(Collectors.toList());
            model.addAttribute("user", user);
            model.addAttribute("jobRequests", jobRequests);
            return "master/job-requests";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    @GetMapping("/nursepage")
    public String nursePage(Model model, HttpServletRequest request, Pageable pageable) {
        String token = getTokenFromCookies(request);
        if (token == null) {
            return "redirect:/login";
        }

        try {
            String username = jwtTokenUtil.extractUsername(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!jwtTokenUtil.validateToken(token, (UserDetails) user)) {
                return "redirect:/login";
            }

            List<NurseProfileDTO> nurseProfiles = nurseProfileService.getAllNurseProfiles();
            model.addAttribute("user", user);
            model.addAttribute("nurseProfiles", nurseProfiles);
            return "master/nursepage";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    @GetMapping("/nursing-service")
    public String nursingService(Model model, HttpServletRequest request) {
        String token = getTokenFromCookies(request);
        if (token == null) {
            return "redirect:/login";
        }

        try {
            String username = jwtTokenUtil.extractUsername(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!jwtTokenUtil.validateToken(token, (UserDetails) user)) {
                return "redirect:/login";
            }

            model.addAttribute("user", user);
            return "master/nursing-service";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    @GetMapping("/review-nurse-profile")
    public String reviewNurseProfile(Model model, HttpServletRequest request) {
        String token = getTokenFromCookies(request);
        if (token == null) {
            return "redirect:/login";
        }

        try {
            String username = jwtTokenUtil.extractUsername(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!jwtTokenUtil.validateToken(token, (UserDetails) user)) {
                return "redirect:/login";
            }

            model.addAttribute("user", user);
            return "master/review-nurse-profile";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    @GetMapping("/feedbacks")
    public String feedbacks(Model model, HttpServletRequest request) {
        String token = getTokenFromCookies(request);
        if (token == null) {
            return "redirect:/login";
        }

        try {
            String username = jwtTokenUtil.extractUsername(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!jwtTokenUtil.validateToken(token, (UserDetails) user)) {
                return "redirect:/login";
            }

            List<Feedback> feedbacks = feedbackService.getFeedbacksByUser(user);
            model.addAttribute("user", user);
            model.addAttribute("feedbacks", feedbacks);
            return "master/feedbacks";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    @GetMapping("/messages")
    public String messages(Model model, HttpServletRequest request) {
        String token = getTokenFromCookies(request);
        if (token == null) {
            return "redirect:/login";
        }

        try {
            String username = jwtTokenUtil.extractUsername(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!jwtTokenUtil.validateToken(token, (UserDetails) user)) {
                return "redirect:/login";
            }

            List<Message> messages = messageService.getMessagesByUser(user);
            model.addAttribute("user", user);
            model.addAttribute("messages", messages);
            return "master/messages";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }


    @GetMapping("/track-income")
    public String trackIncome(Model model, HttpServletRequest request) {
        String token = getTokenFromCookies(request);
        if (token == null) {
            return "redirect:/login";
        }

        try {
            String username = jwtTokenUtil.extractUsername(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!jwtTokenUtil.validateToken(token, (UserDetails) user) || !"NURSE".equalsIgnoreCase(user.getRole().name())) {
                return "redirect:/login";
            }

            List<Earning> earnings = earningService.getEarningsByNurse(user);
            model.addAttribute("user", user);
            model.addAttribute("earnings", earnings);
            return "master/track-income";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        String token = getTokenFromCookies(request);
        if (token != null) {
            tokenCleanupService.cleanupToken(token);
            Cookie cookie = new Cookie("token", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        return "redirect:/login?logout";
    }

    private String getTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}