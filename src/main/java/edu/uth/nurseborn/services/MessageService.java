package edu.uth.nurseborn.services;
import edu.uth.nurseborn.models.Message;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.repositories.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    // Tìm tin nhắn theo ID
    public Optional<Message> findByMessageId(Integer messageId) {
        return messageRepository.findByMessageId(messageId);
    }

    // Tìm tất cả tin nhắn của một người nhận
    public List<Message> findByReceiver(User receiver) {
        return messageRepository.findByReceiver(receiver);
    }

    // Tìm tin nhắn mới nhất giữa hai người
    public Optional<Message> findLatestMessage(User sender, User receiver) {
        return messageRepository.findTopBySenderAndReceiverOrderBySentAtDesc(sender, receiver);
    }

    // Lưu tin nhắn mới
    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }
}
