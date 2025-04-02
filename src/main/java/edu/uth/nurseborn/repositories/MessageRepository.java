package edu.uth.nurseborn.repositories;
import edu.uth.nurseborn.models.Message;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    // Tìm tin nhắn theo ID
    Optional<Message> findByMessageId(Integer messageId);

    // Tìm tất cả tin nhắn do một người gửi
    List<Message> findBySender(User sender);

    // Tìm tất cả tin nhắn của một người nhận
    List<Message> findByReceiver(User receiver);

    // Lấy tất cả tin nhắn liên quan đến một Booking cụ thể
    List<Message> findByBooking(Booking booking);

    // Tìm tất cả tin nhắn chưa đọc của một người nhận
    List<Message> findByReceiverAndIsReadFalse(User receiver);

    // Lấy tin nhắn mới nhất giữa hai người dùng
    Optional<Message> findTopBySenderAndReceiverOrderBySentAtDesc(User sender, User receiver);
}
