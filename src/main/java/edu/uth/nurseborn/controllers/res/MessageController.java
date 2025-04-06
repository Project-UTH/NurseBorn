package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.dtos.MessageDTO;
import edu.uth.nurseborn.services.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private MessageService messageService;

    // Gửi tin nhắn mới
    @PostMapping("/send")
    public ResponseEntity<MessageDTO> sendMessage(
            @RequestParam String senderUsername,
            @RequestParam String receiverUsername,
            @RequestParam(required = false) Integer bookingId,
            @RequestParam String content,
            @RequestParam(required = false) String attachment) {
        logger.debug("Yêu cầu gửi tin nhắn từ {} đến {}", senderUsername, receiverUsername);
        MessageDTO messageDTO = messageService.sendMessage(senderUsername, receiverUsername, bookingId, content, attachment);
        logger.info("Đã gửi tin nhắn thành công từ {} đến {}", senderUsername, receiverUsername);
        return ResponseEntity.ok(messageDTO);
    }

    // Lấy cuộc hội thoại giữa hai người dùng
    @GetMapping("/conversation")
    public ResponseEntity<List<MessageDTO>> getConversation(
            @RequestParam String senderUsername,
            @RequestParam String receiverUsername) {
        logger.debug("Yêu cầu lấy cuộc hội thoại giữa {} và {}", senderUsername, receiverUsername);
        List<MessageDTO> messages = messageService.getConversation(senderUsername, receiverUsername);
        logger.info("Đã lấy thành công cuộc hội thoại giữa {} và {}", senderUsername, receiverUsername);
        return ResponseEntity.ok(messages);
    }

    // Lấy tin nhắn theo booking
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<MessageDTO>> getMessagesByBooking(@PathVariable Integer bookingId) {
        logger.debug("Yêu cầu lấy tin nhắn cho bookingId: {}", bookingId);
        List<MessageDTO> messages = messageService.getMessagesByBooking(bookingId);
        logger.info("Đã lấy thành công tin nhắn cho bookingId: {}", bookingId);
        return ResponseEntity.ok(messages);
    }

    // Đánh dấu tin nhắn là đã đọc
    @PutMapping("/mark-as-read/{messageId}")
    public ResponseEntity<Void> markAsRead(@PathVariable Integer messageId) {
        logger.debug("Yêu cầu đánh dấu tin nhắn {} là đã đọc", messageId);
        messageService.markAsRead(messageId);
        logger.info("Đã đánh dấu tin nhắn {} là đã đọc", messageId);
        return ResponseEntity.ok().build();
    }

    // Lấy tin nhắn chưa đọc của người dùng
    @GetMapping("/unread/{receiverUsername}")
    public ResponseEntity<List<MessageDTO>> getUnreadMessages(@PathVariable String receiverUsername) {
        logger.debug("Yêu cầu lấy tin nhắn chưa đọc cho receiver: {}", receiverUsername);
        List<MessageDTO> messages = messageService.getUnreadMessages(receiverUsername);
        logger.info("Đã lấy thành công tin nhắn chưa đọc cho receiver: {}", receiverUsername);
        return ResponseEntity.ok(messages);
    }

    // Lấy tất cả tin nhắn đã gửi của người dùng
    @GetMapping("/sent/{senderUsername}")
    public ResponseEntity<List<MessageDTO>> getSentMessages(@PathVariable String senderUsername) {
        logger.debug("Yêu cầu lấy tin nhắn đã gửi bởi sender: {}", senderUsername);
        List<MessageDTO> messages = messageService.getSentMessages(senderUsername);
        logger.info("Đã lấy thành công tin nhắn đã gửi bởi sender: {}", senderUsername);
        return ResponseEntity.ok(messages);
    }

    // Lấy tất cả tin nhắn đã nhận của người dùng
    @GetMapping("/received/{receiverUsername}")
    public ResponseEntity<List<MessageDTO>> getReceivedMessages(@PathVariable String receiverUsername) {
        logger.debug("Yêu cầu lấy tin nhắn đã nhận bởi receiver: {}", receiverUsername);
        List<MessageDTO> messages = messageService.getReceivedMessages(receiverUsername);
        logger.info("Đã lấy thành công tin nhắn đã nhận bởi receiver: {}", receiverUsername);
        return ResponseEntity.ok(messages);
    }
}
