package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.models.NurseProfile;
import edu.uth.nurseborn.repositories.NurseProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class NurseReviewController {

    private static final Logger logger = LoggerFactory.getLogger(NurseReviewController.class);

    @Autowired
    private NurseProfileRepository nurseProfileRepository;

    @GetMapping("/nurse_review")
    public String NurseReview(@RequestParam("nurseId") Long nurseId, Model model) {
        logger.debug("Hiển thị trang thông tin y tá với nurseId: {}", nurseId);
        try {
            Optional<NurseProfile> nurseOptional = nurseProfileRepository.findByUserUserId(nurseId);
            if (nurseOptional.isEmpty()) {
                logger.warn("Không tìm thấy y tá với nurseId: {}", nurseId);
                model.addAttribute("error", "Không tìm thấy y tá");
                return "nurse_review";
            }
            NurseProfile nurse = nurseOptional.get();
            model.addAttribute("nurse", nurse);
            logger.info("Đã lấy thành công thông tin y tá với nurseId: {}", nurseId);
        } catch (Exception ex) {
            logger.error("Lỗi khi lấy thông tin y tá: {}", ex.getMessage(), ex);
            model.addAttribute("error", "Đã có lỗi xảy ra khi lấy thông tin y tá: " + ex.getMessage());
        }
        return "master/nurse_review";
    }
}