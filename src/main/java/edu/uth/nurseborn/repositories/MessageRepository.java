package edu.uth.nurseborn.repositories;

import edu.uth.nurseborn.models.Message;
import edu.uth.nurseborn.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {

    @Query("SELECT m FROM Message m WHERE (m.sender.userId = :senderId AND m.receiver.userId = :receiverId) " +
            "OR (m.sender.userId = :receiverId AND m.receiver.userId = :senderId) " +
            "ORDER BY m.sentAt ASC")
    List<Message> findConversationBetweenUsers(Long senderId, Long receiverId);

    List<Message> findByBookingBookingId(Integer bookingId);

    @Query("SELECT m FROM Message m WHERE m.receiver.userId = :receiverId AND m.isRead = false")
    List<Message> findUnreadMessagesByReceiver(Long receiverId);

    List<Message> findBySenderUserId(Long senderId);

    List<Message> findByReceiverUserId(Long receiverId);

    Optional<Message> findByMessageId(Integer messageId);

    @Query("SELECT DISTINCT u FROM User u WHERE u.userId IN " +
            "(SELECT DISTINCT m.receiver.userId FROM Message m WHERE m.sender.userId = :userId) " +
            "OR u.userId IN " +
            "(SELECT DISTINCT m.sender.userId FROM Message m WHERE m.receiver.userId = :userId)")
    List<User> findConversationPartners(Long userId);
}