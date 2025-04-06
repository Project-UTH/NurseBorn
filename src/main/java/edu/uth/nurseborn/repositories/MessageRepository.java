package edu.uth.nurseborn.repositories;

import edu.uth.nurseborn.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Integer> {

    @Query("SELECT m FROM Message m WHERE (m.sender.userId = :senderId AND m.receiver.userId = :receiverId) " +
            "OR (m.sender.userId = :receiverId AND m.receiver.userId = :senderId) ORDER BY m.sentAt ASC")
    List<Message> findConversationBetweenUsers(Integer senderId, Integer receiverId);

    List<Message> findByBookingBookingId(Integer bookingId);

    List<Message> findByReceiverUserIdAndIsReadFalse(Integer receiverId);

    List<Message> findBySenderUserId(Integer senderId);

    List<Message> findByReceiverUserId(Integer receiverId);

    Optional<Message> findByMessageId(Integer messageId);
}
