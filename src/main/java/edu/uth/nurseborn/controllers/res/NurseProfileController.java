package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.dtos.NurseProfileDTO;
import edu.uth.nurseborn.services.NurseProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping
public class NurseProfileController {

    private static final Logger logger = LoggerFactory.getLogger(NurseProfileController.class);

    @Autowired
    private NurseProfileService nurseProfileService;

    @GetMapping("/review-nurse-profile")
    public String getNurseProfiles(Model model) {
        logger.info("Đã nhận yêu cầu GET cho /review-nurse-profile");
        model.addAttribute("nurseProfiles", nurseProfileService.getAllNurseProfiles());
        logger.info("Trả về view master/review-nurse-profile với {} hồ sơ", nurseProfileService.getAllNurseProfiles().size());
        return "master/review-nurse-profile"; // Cập nhật đường dẫn view
    }

    @PostMapping("/nurse/approve/{userId}")
    public String approveNurseProfile(@PathVariable Long userId) {
        logger.debug("Phê duyệt hồ sơ y tá cho userId: {}", userId);
        nurseProfileService.updateApprovalStatus(userId, true);
        return "redirect:/review-nurse-profile";
    }

    @PostMapping("/nurse/reject/{userId}")
    public String rejectNurseProfile(@PathVariable Long userId) {
        logger.debug("Từ chối hồ sơ y tá cho userId: {}", userId);
        nurseProfileService.updateApprovalStatus(userId, false);
        return "redirect:/review-nurse-profile";
    }

    @GetMapping("/test")
    public String test() {
        logger.info("Test endpoint được gọi");
        return "Test endpoint works!";
    }
}