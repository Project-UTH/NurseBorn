package edu.uth.nurseborn.repositories;

import edu.uth.nurseborn.models.Message;
import edu.uth.nurseborn.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Integer> {

    @Query("SELECT m FROM Message m WHERE (m.sender.userId = :senderId AND m.receiver.userId = :receiverId) " +
            "OR (m.sender.userId = :receiverId AND m.receiver.userId = :senderId) ORDER BY m.sentAt ASC")
    List<Message> findConversationBetweenUsers(Long senderId, Long receiverId);

    List<Message> findByBookingBookingId(Integer bookingId);

    List<Message> findByReceiverUserIdAndIsReadFalse(Long receiverId);

    List<Message> findBySenderUserId(Long senderId);

    List<Message> findByReceiverUserId(Long receiverId);

    Optional<Message> findByMessageId(Integer messageId);

    List<Message> findBySenderOrReceiver(User sender, User receiver);
}
