package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.models.Feedback;
import edu.uth.nurseborn.models.NurseProfile;
import edu.uth.nurseborn.repositories.NurseProfileRepository;
import edu.uth.nurseborn.services.FeedbackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class NurseReviewController {

    private static final Logger logger = LoggerFactory.getLogger(NurseReviewController.class);

    @Autowired
    private NurseProfileRepository nurseProfileRepository;

    @Autowired
    private FeedbackService feedbackService;

    @GetMapping("/nurse_review")
    public String NurseReview(@RequestParam("nurseId") Long nurseId, Model model) {
        logger.debug("Hiển thị trang thông tin y tá với nurseId: {}", nurseId);
        try {
            Optional<NurseProfile> nurseOptional = nurseProfileRepository.findByUserUserId(nurseId);
            if (nurseOptional.isEmpty()) {
                logger.warn("Không tìm thấy y tá với nurseId: {}", nurseId);
                model.addAttribute("error", "Không tìm thấy y tá");
                return "family/nurse_review";
            }

            NurseProfile nurse = nurseOptional.get();

            // Lấy danh sách các đánh giá của y tá
            List<Feedback> feedbacks = feedbackService.getFeedbacksByNurse(nurse.getUser());

            // Tính số sao trung bình
            double averageRating = 0.0;
            if (feedbacks != null && !feedbacks.isEmpty()) {
                double totalRating = feedbacks.stream()
                        .mapToDouble(Feedback::getRating)
                        .sum();
                averageRating = totalRating / feedbacks.size();
            }

            model.addAttribute("nurse", nurse);
            model.addAttribute("feedbacks", feedbacks);
            model.addAttribute("averageRating", averageRating);
            logger.info("Đã lấy thành công thông tin y tá với nurseId: {}", nurseId);
        } catch (Exception ex) {
            logger.error("Lỗi khi lấy thông tin y tá: {}", ex.getMessage(), ex);
            model.addAttribute("error", "Đã có lỗi xảy ra khi lấy thông tin y tá: " + ex.getMessage());
        }
        return "family/nurse_review";
    }
}

