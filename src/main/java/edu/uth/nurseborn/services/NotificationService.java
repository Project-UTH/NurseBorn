package edu.uth.nurseborn.services;

import edu.uth.nurseborn.models.Booking;
import edu.uth.nurseborn.models.Notification;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.repositories.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional
    public void createNotification(User user, String message, Booking booking) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setBooking(booking);
        notification.setIsRead(false);
        notificationRepository.save(notification);
        logger.info("Created notification for user {}: {}", user.getUsername(), message);
    }

    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalse(user);
    }

    public List<Notification> getAllNotificationsForUser(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with ID: " + notificationId));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }
}