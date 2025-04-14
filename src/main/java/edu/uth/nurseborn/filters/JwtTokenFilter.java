package edu.uth.nurseborn.filters;

import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.components.JwtTokenUtils;
import jakarta.servlet.FilterChain;
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
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.aspectj.weaver.tools.cache.SimpleCacheFactory.path;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    @Value("${api.prefix}")
    private String apiPrefix;

    private final UserDetailsService userDetailsService;
    private final JwtTokenUtils jwtTokenUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws IOException {
        try {
            if (isBypassToken(request)) {
                filterChain.doFilter(request, response); // Bỏ qua kiểm tra token
                return;
            }

            final String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Authorization header is missing or does not start with Bearer");
                return;
            }

            final String token = authHeader.substring(7);
            final String username = jwtTokenUtil.extractUsername(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User userDetails = (User) userDetailsService.loadUserByUsername(username);
                if (jwtTokenUtil.validateToken(token, (UserDetails) userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    ((UserDetails) userDetails).getAuthorities()
                            );
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
            filterChain.doFilter(request, response); // Tiếp tục chuỗi filter
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token: " + e.getMessage());
        }
    }

    private boolean isBypassToken(@NonNull HttpServletRequest request) {
        final List<Pair<String, String>> bypassTokens = Arrays.asList(
                Pair.of(String.format("%s/auth/register", apiPrefix), "POST"),
                Pair.of(String.format("%s/auth/login", apiPrefix), "POST"),
                Pair.of("/home", "GET"),
                Pair.of("/login-h", "GET"),
                Pair.of("/api-docs", "GET"),
                Pair.of("/api-docs/.*", "GET"),
                Pair.of("/swagger-resources", "GET"),
                Pair.of("/swagger-resources/.*", "GET"),
                Pair.of("/configuration/ui", "GET"),
                Pair.of("/configuration/security", "GET"),
                Pair.of("/swagger-ui/.*", "GET"),
                Pair.of("/swagger-ui.html", "GET"),
                Pair.of("/swagger-ui/index.html", "GET")
        ); // ✅ Đóng đúng danh sách

        // ✅ Bỏ qua token cho các static files như js, css, fonts,...
        String path = request.getServletPath();
        String method = request.getMethod();

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
            if (path.matches(bypassPath) && method.equalsIgnoreCase(bypassMethod)) {
                return true;
            }
        }

        return false;
    }

}

