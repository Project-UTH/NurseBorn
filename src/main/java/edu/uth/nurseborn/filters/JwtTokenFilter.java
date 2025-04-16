package edu.uth.nurseborn.filters;

import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.components.JwtTokenUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    @Value("${api.prefix}")
    private String apiPrefix;

    private final UserDetailsService userDetailsService;
    private final JwtTokenUtils jwtTokenUtil;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Kiểm tra xem yêu cầu có nên bỏ qua token không
        if (isBypassToken(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = null;

        // Kiểm tra header Authorization
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        } else {
            // Nếu không có header, kiểm tra cookie
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("token".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }

        // Nếu không tìm thấy token, cho phép yêu cầu tiếp tục
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = jwtTokenUtil.extractUsername(token);
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtTokenUtil.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isBypassToken(@NonNull HttpServletRequest request) {
        final List<Pair<String, String>> bypassTokens = Arrays.asList(
                Pair.of(String.format("%s/auth/register", apiPrefix), "POST"),
                Pair.of(String.format("%s/auth/login", apiPrefix), "POST"),
                Pair.of("/home", "GET"),
                Pair.of("/login-h", "GET"),
                Pair.of("/review-nurse-profile", "POST"),
                Pair.of("/api-docs", "GET"),
                Pair.of("/api-docs/.*", "GET"),
                Pair.of("/swagger-resources", "GET"),
                Pair.of("/swagger-resources/.*", "GET"),
                Pair.of("/configuration/ui", "GET"),
                Pair.of("/configuration/security", "GET"),
                Pair.of("/swagger-ui/.*", "GET"),
                Pair.of("/swagger-ui.html", "GET"),
                Pair.of("/swagger-ui/index.html", "GET"),
                Pair.of("/", "GET"),
                Pair.of("/login", "GET"),
                Pair.of("/login", "POST"), // Thêm để bỏ qua POST /login
                Pair.of("/register", "GET"),
                Pair.of("/register", "POST"), // Thêm để bỏ qua POST /register
                Pair.of("/logout", "GET"),
                Pair.of("/role-selection", "GET"),
                Pair.of("/register/nurse", "GET"),
                Pair.of("/register/nurse", "POST"),
                Pair.of("/register/family", "GET"),
                Pair.of("/register/family", "POST"),
                Pair.of("/resources/**", "GET"),
                Pair.of("/static/**", "GET"),
                Pair.of("/css/**", "GET"),
                Pair.of("/js/**", "GET"),
                Pair.of("/nursepage","GET"),
                Pair.of("/nurse_review","GET"),
                Pair.of("/images/**", "GET"),
                Pair.of("/images/**", "GET"),
                Pair.of("/user-profile", "GET"),
                Pair.of("/update-user", "GET"),
                Pair.of("/update-user", "POST"),
                Pair.of("/nurse-profile", "GET")
        );

        String path = request.getServletPath();
        String method = request.getMethod();

        // Bỏ qua token cho các static files
        if (method.equalsIgnoreCase("GET") && (
                path.startsWith("/js/") ||
                        path.startsWith("/libs/") ||
                        path.startsWith("/assets/") ||
                        path.startsWith("/scss/") ||
                        path.startsWith("/fonts/") ||
                        path.startsWith("/tasks/") ||
                        path.startsWith("/css/") ||
                        path.startsWith("/images/") ||
                        path.startsWith("/swagger-ui/") ||
                        path.startsWith("/api-docs")
        )) {
            return true;
        }

        for (Pair<String, String> token : bypassTokens) {
            String bypassPath = token.getFirst();
            String bypassMethod = token.getSecond();
            if (pathMatcher.match(bypassPath, path) && method.equalsIgnoreCase(bypassMethod)) {
                return true;
            }
        }

        return false;
    }
}