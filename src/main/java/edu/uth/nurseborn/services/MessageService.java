package edu.uth.nurseborn.services;

import edu.uth.nurseborn.dtos.MessageDTO;
import edu.uth.nurseborn.exception.UserNotFoundException;
import edu.uth.nurseborn.models.Booking;
import edu.uth.nurseborn.models.Message;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.repositories.BookingRepository;
import edu.uth.nurseborn.repositories.MessageRepository;
import edu.uth.nurseborn.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public MessageDTO sendMessage(String senderUsername, String receiverUsername, Integer bookingId, String content, String attachment) {
        logger.debug("Gửi tin nhắn từ {} đến {}", senderUsername, receiverUsername);

        // Kiểm tra sender
        Optional<User> senderOptional = userRepository.findByUsername(senderUsername);
        if (!senderOptional.isPresent()) {
            logger.warn("Không tìm thấy sender với username: {}", senderUsername);
            throw new UserNotFoundException("Không tìm thấy sender với username: " + senderUsername);
        }
        User sender = senderOptional.get();

        // Kiểm tra receiver
        Optional<User> receiverOptional = userRepository.findByUsername(receiverUsername);
        if (!receiverOptional.isPresent()) {
            logger.warn("Không tìm thấy receiver với username: {}", receiverUsername);
            throw new UserNotFoundException("Không tìm thấy receiver với username: " + receiverUsername);
        }
        User receiver = receiverOptional.get();

        // Kiểm tra booking nếu có
        Booking booking = null;
        if (bookingId != null) {
            Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
            if (!bookingOptional.isPresent()) {
                logger.warn("Không tìm thấy booking với ID: {}", bookingId);
                throw new RuntimeException("Không tìm thấy booking với ID: " + bookingId);
            }
            booking = bookingOptional.get();
            // Kiểm tra sender và receiver có liên quan đến booking không (theo trigger trong database)
            if (!(sender.getUserId().equals(booking.getFamily().getUserId()) && receiver.getUserId().equals(booking.getNurse().getUserId())) &&
                    !(sender.getUserId().equals(booking.getNurse().getUserId()) && receiver.getUserId().equals(booking.getFamily().getUserId()))) {
                logger.warn("Sender và receiver không liên quan đến booking ID: {}", bookingId);
                throw new IllegalArgumentException("Sender và receiver phải liên quan đến booking");
            }
        }

        // Kiểm tra content không rỗng (theo database: NOT NULL)
        if (content == null || content.trim().isEmpty()) {
            logger.warn("Nội dung tin nhắn không được rỗng");
            throw new IllegalArgumentException("Nội dung tin nhắn không được rỗng");
        }

        try {
            Message message = new Message();
            message.setSender(sender);
            message.setReceiver(receiver);
            message.setBooking(booking); // Có thể null nếu không có booking
            message.setContent(content);
            message.setAttachment(attachment);
            message.setRead(false); // Mặc định là chưa đọc, khớp với database

            logger.debug("Message entity trước khi lưu: {}", message);
            Message savedMessage = messageRepository.save(message);
            messageRepository.flush();
            logger.info("Đã gửi tin nhắn thành công từ {} đến {}", senderUsername, receiverUsername);

            return modelMapper.map(savedMessage, MessageDTO.class);
        } catch (Exception ex) {
            logger.error("Lỗi khi gửi tin nhắn: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi gửi tin nhắn: " + ex.getMessage());
        }
    }

    public List<MessageDTO> getConversation(String senderUsername, String receiverUsername) {
        logger.debug("Lấy cuộc hội thoại giữa {} và {}", senderUsername, receiverUsername);

        Optional<User> senderOptional = userRepository.findByUsername(senderUsername);
        if (!senderOptional.isPresent()) {
            logger.warn("Không tìm thấy sender với username: {}", senderUsername);
            throw new UserNotFoundException("Không tìm thấy sender với username: " + senderUsername);
        }

        Optional<User> receiverOptional = userRepository.findByUsername(receiverUsername);
        if (!receiverOptional.isPresent()) {
            logger.warn("Không tìm thấy receiver với username: {}", receiverUsername);
            throw new UserNotFoundException("Không tìm thấy receiver với username: " + receiverUsername);
        }

        List<Message> messages = messageRepository.findConversationBetweenUsers(
                senderOptional.get().getUserId(), receiverOptional.get().getUserId());
        return messages.stream()
                .map(message -> modelMapper.map(message, MessageDTO.class))
                .collect(Collectors.toList());
    }

    public List<MessageDTO> getMessagesByBooking(Integer bookingId) {
        logger.debug("Lấy tin nhắn cho bookingId: {}", bookingId);

        // Kiểm tra booking tồn tại
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (!bookingOptional.isPresent()) {
            logger.warn("Không tìm thấy booking với ID: {}", bookingId);
            throw new RuntimeException("Không tìm thấy booking với ID: " + bookingId);
        }

        List<Message> messages = messageRepository.findByBookingBookingId(bookingId);
        return messages.stream()
                .map(message -> modelMapper.map(message, MessageDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Integer messageId) {
        logger.debug("Đánh dấu tin nhắn {} là đã đọc", messageId);

        Optional<Message> messageOptional = messageRepository.findByMessageId(messageId);
        if (!messageOptional.isPresent()) {
            logger.warn("Không tìm thấy tin nhắn với ID: {}", messageId);
            throw new RuntimeException("Không tìm thấy tin nhắn với ID: " + messageId);
        }

        Message message = messageOptional.get();
        message.setRead(true);
        messageRepository.save(message);
        messageRepository.flush();
        logger.info("Đã đánh dấu tin nhắn {} là đã đọc", messageId);
    }

    public List<MessageDTO> getUnreadMessages(String receiverUsername) {
        logger.debug("Lấy tin nhắn chưa đọc cho receiver: {}", receiverUsername);

        Optional<User> receiverOptional = userRepository.findByUsername(receiverUsername);
        if (!receiverOptional.isPresent()) {
            logger.warn("Không tìm thấy receiver với username: {}", receiverUsername);
            throw new UserNotFoundException("Không tìm thấy receiver với username: " + receiverUsername);
        }

        List<Message> messages = messageRepository.findByReceiverUserIdAndIsReadFalse(receiverOptional.get().getUserId());
        return messages.stream()
                .map(message -> modelMapper.map(message, MessageDTO.class))
                .collect(Collectors.toList());
    }

    public List<MessageDTO> getSentMessages(String senderUsername) {
        logger.debug("Lấy tin nhắn đã gửi bởi sender: {}", senderUsername);

        Optional<User> senderOptional = userRepository.findByUsername(senderUsername);
        if (!senderOptional.isPresent()) {
            logger.warn("Không tìm thấy sender với username: {}", senderUsername);
            throw new UserNotFoundException("Không tìm thấy sender với username: " + senderUsername);
        }

        List<Message> messages = messageRepository.findBySenderUserId(senderOptional.get().getUserId());
        return messages.stream()
                .map(message -> modelMapper.map(message, MessageDTO.class))
                .collect(Collectors.toList());
    }

    public List<MessageDTO> getReceivedMessages(String receiverUsername) {
        logger.debug("Lấy tin nhắn đã nhận bởi receiver: {}", receiverUsername);

        Optional<User> receiverOptional = userRepository.findByUsername(receiverUsername);
        if (!receiverOptional.isPresent()) {
            logger.warn("Không tìm thấy receiver với username: {}", receiverUsername);
            throw new UserNotFoundException("Không tìm thấy receiver với username: " + receiverUsername);
        }

        List<Message> messages = messageRepository.findByReceiverUserId(receiverOptional.get().getUserId());
        return messages.stream()
                .map(message -> modelMapper.map(message, MessageDTO.class))
                .collect(Collectors.toList());
    }
    public List<Message> getMessagesByUser(User user) {
        return messageRepository.findBySenderOrReceiver(user, user);
    }
}
