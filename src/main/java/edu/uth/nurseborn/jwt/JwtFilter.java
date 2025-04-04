package edu.uth.nurseborn.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        logger.debug("Đang xử lý request: {}", requestURI);

        // Bỏ qua các endpoint công khai
        if (requestURI.startsWith("/api/auth/") || requestURI.equals("/home")) {
            logger.debug("Bỏ qua xác thực token cho endpoint công khai: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // Lấy header Authorization trực tiếp
        String authorizationHeader = request.getHeader("Authorization");
        String token = jwtService.layTokenTuHeader(authorizationHeader);

        if (token != null && jwtService.xacThucToken(token)) {
            try {
                String username = jwtUtil.layTenNguoiDungTuToken(token);
                logger.debug("Trích xuất username từ token: {}", username);

                UserDetails userDetails = jwtService.loadUserByUsername(username);
                if (userDetails != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.info("Xác thực thành công cho người dùng: {}", username);
                }
            } catch (Exception e) {
                logger.error("Lỗi khi xác thực token: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Token không hợp lệ: " + e.getMessage() + "\"}");
                return;
            }
        } else if (token != null) {
            logger.warn("Token không hợp lệ: {}", token);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Token không hợp lệ\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}