package edu.uth.nurseborn.repositories;

import edu.uth.nurseborn.models.Token;
import edu.uth.nurseborn.models.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Token findByToken(String token);

    @Transactional
    @Modifying
    @Query("DELETE FROM Token t WHERE t.user = :user")
    void deleteByUser(User user);

    List<Token> findByRevokedFalseAndExpiresAtAfter(LocalDateTime currentTime);

    @Transactional
    @Modifying
    @Query("DELETE FROM Token t WHERE t.revoked = true OR t.expiresAt < :currentTime")
    int deleteByRevokedTrueOrExpiresAtBefore(LocalDateTime currentTime);
}