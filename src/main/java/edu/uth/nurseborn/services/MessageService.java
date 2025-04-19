package edu.uth.nurseborn.services;

import edu.uth.nurseborn.dtos.MessageDTO;
import edu.uth.nurseborn.exception.UserNotFoundException;
import edu.uth.nurseborn.exception.IllegalAccessException;
import edu.uth.nurseborn.models.Message;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.models.enums.Role;
import edu.uth.nurseborn.repositories.MessageRepository;
import edu.uth.nurseborn.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public MessageDTO sendMessage(MessageDTO messageDTO) {
        logger.debug("Gửi tin nhắn từ userId: {} đến userId: {}", messageDTO.getSenderId(), messageDTO.getReceiverId());

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng: " + username));

        if (currentUser.getRole() == Role.ADMIN) {
            logger.warn("Admin {} cố gắng gửi tin nhắn", username);
            throw new IllegalAccessException("Admin không có quyền gửi tin nhắn");
        }

        if (!currentUser.getUserId().equals(messageDTO.getSenderId())) {
            logger.warn("SenderId {} không khớp với userId hiện tại {}", messageDTO.getSenderId(), currentUser.getUserId());
            throw new IllegalArgumentException("SenderId không hợp lệ");
        }

        User sender = userRepository.findById(messageDTO.getSenderId())
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người gửi với ID: " + messageDTO.getSenderId()));
        User receiver = userRepository.findById(messageDTO.getReceiverId())
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người nhận với ID: " + messageDTO.getReceiverId()));

        if (receiver.getRole() == Role.ADMIN) {
            logger.warn("Cố gắng gửi tin nhắn đến Admin với userId: {}", receiver.getUserId());
            throw new IllegalArgumentException("Không thể gửi tin nhắn đến Admin");
        }

        Message message = modelMapper.map(messageDTO, Message.class);
        message.setSender(sender);
        message.setReceiver(receiver);

        Message savedMessage = messageRepository.save(message);
        logger.info("Đã lưu tin nhắn từ {} đến {} thành công", sender.getUsername(), receiver.getUsername());

        MessageDTO responseDTO = modelMapper.map(savedMessage, MessageDTO.class);
        responseDTO.setSenderUsername(sender.getUsername());
        responseDTO.setReceiverUsername(receiver.getUsername());
        return responseDTO;
    }

    public List<MessageDTO> getConversation(Long senderId, Long receiverId) {
        logger.debug("Lấy cuộc trò chuyện giữa userId: {} và userId: {}", senderId, receiverId);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng: " + username));

        if (currentUser.getRole() == Role.ADMIN) {
            logger.warn("Admin {} cố gắng truy cập cuộc trò chuyện", username);
            throw new IllegalAccessException("Admin không có quyền xem tin nhắn");
        }

        if (!currentUser.getUserId().equals(senderId) && !currentUser.getUserId().equals(receiverId)) {
            logger.warn("User {} cố gắng truy cập cuộc trò chuyện không thuộc về họ", username);
            throw new IllegalAccessException("Không có quyền truy cập cuộc trò chuyện này");
        }

        List<Message> messages = messageRepository.findConversationBetweenUsers(senderId, receiverId);
        return messages.stream()
                .map(message -> {
                    MessageDTO dto = modelMapper.map(message, MessageDTO.class);
                    dto.setSenderUsername(message.getSender().getUsername());
                    dto.setReceiverUsername(message.getReceiver().getUsername());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<User> getConversationPartners(Long userId) {
        logger.debug("Lấy danh sách đối tác trò chuyện cho userId: {}", userId);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng: " + username));

        if (currentUser.getRole() == Role.ADMIN) {
            logger.warn("Admin {} cố gắng truy cập danh sách đối tác trò chuyện", username);
            throw new IllegalAccessException("Admin không có quyền xem danh sách đối tác trò chuyện");
        }

        if (!currentUser.getUserId().equals(userId)) {
            logger.warn("User {} cố gắng truy cập danh sách đối tác trò chuyện của userId: {}", username, userId);
            throw new IllegalAccessException("Không có quyền truy cập danh sách này");
        }

        return messageRepository.findConversationPartners(userId);
    }

    @Transactional
    public void markAsRead(Integer messageId) {
        logger.debug("Đánh dấu tin nhắn {} là đã đọc", messageId);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng: " + username));

        Message message = messageRepository.findByMessageId(messageId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin nhắn với ID: " + messageId));

        if (!message.getReceiver().getUserId().equals(currentUser.getUserId())) {
            logger.warn("User {} cố gắng đánh dấu tin nhắn không phải của họ", username);
            throw new IllegalAccessException("Không có quyền đánh dấu tin nhắn này");
        }

        message.setIsRead(true);
        messageRepository.save(message);
        logger.info("Đã đánh dấu tin nhắn {} là đã đọc", messageId);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng: " + username));
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng với ID: " + userId));
    }
}