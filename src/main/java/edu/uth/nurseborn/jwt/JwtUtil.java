package edu.uth.nurseborn.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String taoToken(String username, String role) {
        logger.debug("Đang tạo token cho người dùng: {}", username);
        try {
            String token = Jwts.builder()
                    .setSubject(username)
                    .claim("role", role)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();
            logger.info("Token đã được tạo thành công cho người dùng: {}", username);
            return token;
        } catch (Exception e) {
            logger.error("Lỗi khi tạo token cho người dùng {}: {}", username, e.getMessage());
            throw new RuntimeException("Không thể tạo token: " + e.getMessage());
        }
    }

    public String layTenNguoiDungTuToken(String token) {
        logger.debug("Đang trích xuất username từ token");
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException e) {
            logger.error("Lỗi khi trích xuất username từ token: {}", e.getMessage());
            throw new RuntimeException("Token không hợp lệ: " + e.getMessage());
        }
    }

    public String layVaiTroTuToken(String token) {
        logger.debug("Đang trích xuất role từ token");
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("role", String.class);
        } catch (JwtException e) {
            logger.error("Lỗi khi trích xuất role từ token: {}", e.getMessage());
            throw new RuntimeException("Token không hợp lệ: " + e.getMessage());
        }
    }

    public boolean kiemTraToken(String token) {
        logger.debug("Đang kiểm tra tính hợp lệ của token");
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            logger.info("Token hợp lệ");
            return true;
        } catch (JwtException e) {
            logger.error("Token không hợp lệ: {}", e.getMessage());
            return false;
        }
    }

    public Date layThoiGianHetHanTuToken(String token) {
        logger.debug("Đang trích xuất thời gian hết hạn từ token");
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
        } catch (JwtException e) {
            logger.error("Lỗi khi trích xuất thời gian hết hạn từ token: {}", e.getMessage());
            throw new RuntimeException("Token không hợp lệ: " + e.getMessage());
        }
    }
}