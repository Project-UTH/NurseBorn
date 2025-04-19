package edu.uth.nurseborn.repositories;

import edu.uth.nurseborn.models.Notification;
import edu.uth.nurseborn.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    List<Notification> findByUserAndIsReadFalse(User user); // Lấy thông báo chưa đọc
}