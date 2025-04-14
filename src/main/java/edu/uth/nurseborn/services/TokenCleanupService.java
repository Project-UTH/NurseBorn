package edu.uth.nurseborn.services;

import edu.uth.nurseborn.models.Token;
import edu.uth.nurseborn.repositories.TokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TokenCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(TokenCleanupService.class);

    @Autowired
    private TokenRepository tokenRepository;

    @Scheduled(cron = "0 0 0 * * ?") // Chạy mỗi ngày lúc 0h
    public void cleanupExpiredTokens() {
        logger.info("Bắt đầu dọn dẹp token hết hạn hoặc bị thu hồi...");
        int deletedCount = tokenRepository.deleteByRevokedTrueOrExpiresAtBefore(LocalDateTime.now());
        logger.info("Đã xóa {} token hết hạn hoặc bị thu hồi", deletedCount);
    }

    public void cleanupToken(String token) {
        Token existingToken = tokenRepository.findByToken(token);
        if (existingToken != null) {
            existingToken.setRevoked(true);
            tokenRepository.save(existingToken);
            logger.info("Đã thu hồi token: {}", token);
        }
    }
}