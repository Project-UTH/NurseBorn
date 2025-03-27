package edu.uth.nurseborn.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Integer messageId;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking; // Có thể null

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "attachment")
    private String attachment;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @PrePersist
    public void prePersist() {
        this.sentAt = LocalDateTime.now();
    }

    // Getters, setters, constructors

    public Boolean getRead() {
        return isRead;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public String getAttachment() {
        return attachment;
    }

    public String getContent() {
        return content;
    }

    public Booking getBooking() {
        return booking;
    }

    public User getReceiver() {
        return receiver;
    }

    public User getSender() {
        return sender;
    }

    public Integer getMessageId() {
        return messageId;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public void setRead(Boolean read) {
        isRead = read;
    }

    public Message() {}

    public Message(Boolean isRead, String attachment, String content, Booking booking, User receiver, User sender) {
        this.isRead = isRead;
        this.attachment = attachment;
        this.content = content;
        this.booking = booking;
        this.receiver = receiver;
        this.sender = sender;
    }
}