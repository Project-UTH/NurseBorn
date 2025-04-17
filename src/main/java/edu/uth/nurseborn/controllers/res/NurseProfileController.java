package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.repositories.UserRepository;
import edu.uth.nurseborn.services.NurseProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping
public class NurseProfileController {

    private static final Logger logger = LoggerFactory.getLogger(NurseProfileController.class);

    @Autowired
    private NurseProfileService nurseProfileService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/review-nurse-profile")
    public String getNurseProfiles(Model model) {
        logger.info("Đã nhận yêu cầu GET cho /review-nurse-profile");
        model.addAttribute("nurseProfiles", nurseProfileService.getAllNurseProfiles());
        logger.info("Trả về view master/review-nurse-profile với {} hồ sơ", nurseProfileService.getAllNurseProfiles().size());
        return "admin/review-nurse-profile";
    }

    @PostMapping("/nurse/approve/{userId}")
    public String approveNurseProfile(@PathVariable Long userId) {
        logger.debug("Phê duyệt hồ sơ y tá cho userId: {}", userId);
        // Lấy thông tin admin từ SecurityContext
        String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy admin: " + adminUsername));

        nurseProfileService.updateApprovalStatus(userId, true, admin);
        return "redirect:/review-nurse-profile";
    }

    @PostMapping("/nurse/reject/{userId}")
    public String rejectNurseProfile(@PathVariable Long userId) {
        logger.debug("Từ chối hồ sơ y tá cho userId: {}", userId);
        // Lấy thông tin admin từ SecurityContext (vẫn cần để đồng bộ logic, nhưng không dùng để lưu hành động)
        String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy admin: " + adminUsername));

        nurseProfileService.updateApprovalStatus(userId, false, admin);
        return "redirect:/review-nurse-profile";
    }

    @GetMapping("/test")
    public String test() {
        logger.info("Test endpoint được gọi");
        return "Test endpoint works!";
    }
}