package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.models.NurseProfile;
import edu.uth.nurseborn.services.NurseServiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class NurseController {

    private static final Logger logger = LoggerFactory.getLogger(NurseController.class);

    @Autowired
    private NurseServiceService nurseServiceService;

    @GetMapping("/nursepage")
    public String NursePage(Model model) {
        logger.debug("Hiển thị trang danh sách y tá");

        try {
            // Lấy danh sách y tá đã được phê duyệt
            List<NurseProfile> nurses = nurseServiceService.getApprovedNurses();
            model.addAttribute("nurses", nurses);
            logger.info("Đã lấy thành công {} y tá để hiển thị", nurses.size());
        } catch (Exception ex) {
            logger.error("Lỗi khi lấy danh sách y tá: {}", ex.getMessage(), ex);
            model.addAttribute("error", "Đã có lỗi xảy ra khi lấy danh sách y tá.");
        }

        return "master/nursepage";
    }
    @GetMapping("/nurse_review")
    public String nurse_review() {
        return "master/nurse_review";
    }

}
