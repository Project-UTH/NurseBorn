package edu.uth.nurseborn.jwt;

import edu.uth.nurseborn.exception.AuthenticationException;
import edu.uth.nurseborn.exception.UserNotFoundException;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class JwtService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        logger.debug("Đang tải thông tin người dùng: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Người dùng không tồn tại: {}", username);
                    return new UsernameNotFoundException("Người dùng không tồn tại: " + username);
                });
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

    public String taoToken(@NonNull String username) {
        logger.debug("Đang tạo token cho người dùng: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Người dùng không tồn tại: {}", username);
                    return new UserNotFoundException("Người dùng không tồn tại: " + username);
                });
        String token = jwtUtil.taoToken(user.getUsername(), user.getRole().name());
        logger.info("Token đã được tạo thành công cho người dùng: {}", username);
        return token;
    }

    // Sửa phương thức để nhận header Authorization thay vì HttpServletRequest
    public String layTokenTuHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            logger.debug("Đã tìm thấy token trong header: {}", authorizationHeader);
            return authorizationHeader.substring(7);
        }
        logger.warn("Không tìm thấy token trong header Authorization");
        return null;
    }

    public User layNguoiDungHienTai() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.error("Không có người dùng nào được xác thực");
            throw new AuthenticationException("Không có người dùng nào được xác thực");
        }
        String username = authentication.getName();
        logger.debug("Đang lấy thông tin người dùng hiện tại: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Người dùng không tồn tại: {}", username);
                    return new UserNotFoundException("Người dùng không tồn tại: " + username);
                });
    }

    public boolean xacThucToken(String token) {
        if (token == null) {
            logger.warn("Token không tồn tại");
            return false;
        }
        if (!jwtUtil.kiemTraToken(token)) {
            logger.warn("Token không hợp lệ: {}", token);
            return false;
        }
        String username = jwtUtil.layTenNguoiDungTuToken(token);
        logger.debug("Đang xác thực token cho người dùng: {}", username);
        UserDetails userDetails = loadUserByUsername(username);
        boolean isValid = userDetails != null;
        if (isValid) {
            logger.info("Token đã được xác thực thành công cho người dùng: {}", username);
        } else {
            logger.error("Xác thực token thất bại cho người dùng: {}", username);
        }
        return isValid;
    }
}