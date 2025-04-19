package edu.uth.nurseborn.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Integer messageId;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    @NotNull(message = "Người gửi không được để trống")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    @NotNull(message = "Người nhận không được để trống")
    private User receiver;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking; // Có thể null nếu không liên quan đến booking

    @Column(name = "content", nullable = false)
    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    private String content;

    @Column(name = "attachment")
    private String attachment; // Đường dẫn đến file đính kèm, có thể null

    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @PrePersist
    protected void onCreate() {
        this.sentAt = LocalDateTime.now();
        if (this.isRead == null) {
            this.isRead = false;
        }
    }

    // Constructors
    public Message() {}

    public Message(User sender, User receiver, Booking booking, String content, String attachment) {
        this.sender = sender;
        this.receiver = receiver;
        this.booking = booking;
        this.content = content;
        this.attachment = attachment;
    }

    // Getters and setters
    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    // Không có setter cho sentAt vì được quản lý bởi @PrePersist

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
}