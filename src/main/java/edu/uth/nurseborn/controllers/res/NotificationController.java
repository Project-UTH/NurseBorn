package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.models.NurseProfile;
import edu.uth.nurseborn.models.Notification;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.repositories.NurseProfileRepository;
import edu.uth.nurseborn.repositories.UserRepository;
import edu.uth.nurseborn.services.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NurseProfileRepository nurseProfileRepository;

    @GetMapping("/notifications")
    public String Notifications(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            logger.warn("Người dùng chưa đăng nhập, chuyển hướng đến trang đăng nhập");
            return "redirect:/login";
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        // Thêm nurseProfile nếu người dùng là NURSE
        if ("NURSE".equalsIgnoreCase(user.getRole().name())) {
            NurseProfile nurseProfile = nurseProfileRepository.findByUserUserId(user.getUserId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy NurseProfile cho userId: " + user.getUserId()));
            logger.debug("NurseProfile userId={}, profileImage={}", user.getUserId(), nurseProfile.getProfileImage());
            model.addAttribute("nurseProfile", nurseProfile);
        }

        List<Notification> notifications = notificationService.getAllNotificationsForUser(user);
        model.addAttribute("notifications", notifications);
        model.addAttribute("user", user);
        return "master/notifications";
    }

    @GetMapping("/notifications/mark-as-read/{notificationId}")
    public String markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return "redirect:/notifications";
    }
}