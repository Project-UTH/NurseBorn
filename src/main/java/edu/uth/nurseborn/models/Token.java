package edu.uth.nurseborn.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tokens")
@Getter
@Setter
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "revoked")
    private boolean revoked;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expires_at") // Đảm bảo ánh xạ đúng với cột trong database
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}