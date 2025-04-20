package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.dtos.MessageDTO;
import edu.uth.nurseborn.dtos.UserDTO;
import edu.uth.nurseborn.models.NurseProfile;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.repositories.NurseProfileRepository;
import edu.uth.nurseborn.services.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/messages")
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private NurseProfileRepository nurseProfileRepository;

    @GetMapping
    public String getMessagesPage(Model model, @RequestParam(value = "nurseUserId", required = false) Long nurseUserId) {
        logger.debug("Hiển thị trang tin nhắn, nurseUserId: {}", nurseUserId);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            User user = messageService.getUserByUsername(username);
            model.addAttribute("user", user);

            // Thêm nurseProfile nếu người dùng là NURSE
            if ("NURSE".equalsIgnoreCase(user.getRole().name())) {
                NurseProfile nurseProfile = nurseProfileRepository.findByUserUserId(user.getUserId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy NurseProfile cho userId: " + user.getUserId()));
                logger.debug("NurseProfile userId={}, profileImage={}", user.getUserId(), nurseProfile.getProfileImage());
                model.addAttribute("nurseProfile", nurseProfile);
            }

            if (nurseUserId != null) {
                User nurse = messageService.getUserById(nurseUserId);
                if (nurse != null) {
                    model.addAttribute("selectedNurse", nurse);
                }
            }
        } catch (Exception e) {
            logger.error("Lỗi khi lấy thông tin người dùng: {}", e.getMessage());
            model.addAttribute("error", "Không thể tải thông tin người dùng: " + e.getMessage());
        }
        return "master/messages";
    }

    @PostMapping("/send")
    @ResponseBody
    public ResponseEntity<?> sendMessage(@RequestBody MessageDTO messageDTO) {
        logger.debug("Nhận yêu cầu gửi tin nhắn từ {} đến {}", messageDTO.getSenderId(), messageDTO.getReceiverId());
        try {
            MessageDTO savedMessage = messageService.sendMessage(messageDTO);
            return ResponseEntity.ok(savedMessage);
        } catch (Exception e) {
            logger.error("Lỗi khi gửi tin nhắn: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Không thể gửi tin nhắn: " + e.getMessage()));
        }
    }

    @GetMapping("/conversation/{senderId}/{receiverId}")
    @ResponseBody
    public ResponseEntity<?> getConversation(@PathVariable Long senderId, @PathVariable Long receiverId) {
        logger.debug("Nhận yêu cầu lấy cuộc trò chuyện giữa {} và {}", senderId, receiverId);
        try {
            List<MessageDTO> messages = messageService.getConversation(senderId, receiverId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy cuộc trò chuyện: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Không thể lấy cuộc trò chuyện: " + e.getMessage()));
        }
    }

    @GetMapping("/partners/{userId}")
    @ResponseBody
    public ResponseEntity<?> getConversationPartners(@PathVariable Long userId) {
        logger.debug("Nhận yêu cầu lấy danh sách đối tác trò chuyện cho userId: {}", userId);
        try {
            List<User> partners = messageService.getConversationPartners(userId);
            List<UserDTO> partnerDTOs = partners.stream()
                    .map(user -> {
                        UserDTO dto = new UserDTO();
                        dto.setUserId(user.getUserId());
                        dto.setUsername(user.getUsername());
                        dto.setFullName(user.getFullName());
                        return dto;
                    })
                    .collect(Collectors.toList());
            return ResponseEntity.ok(partnerDTOs);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách đối tác trò chuyện: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Không thể lấy danh sách đối tác: " + e.getMessage()));
        }
    }

    @PostMapping("/read/{messageId}")
    @ResponseBody
    public ResponseEntity<?> markAsRead(@PathVariable Integer messageId) {
        logger.debug("Nhận yêu cầu đánh dấu tin nhắn {} là đã đọc", messageId);
        try {
            messageService.markAsRead(messageId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Lỗi khi đánh dấu tin nhắn đã đọc: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Không thể đánh dấu tin nhắn đã đọc: " + e.getMessage()));
        }
    }
}