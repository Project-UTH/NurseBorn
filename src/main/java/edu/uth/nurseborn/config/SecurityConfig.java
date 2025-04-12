package edu.uth.nurseborn.config;

import edu.uth.nurseborn.jwt.JwtFilter;
import edu.uth.nurseborn.jwt.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod; // Import đúng cho HttpMethod

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private JwtService jwtService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Đang cấu hình SecurityFilterChain...");
        http
                .csrf(csrf -> {
                    logger.debug("Vô hiệu hóa CSRF");
                    csrf.disable();
                })
                .sessionManagement(session -> {
                    logger.debug("Cấu hình session stateless");
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                .authorizeHttpRequests(auth -> {
                    logger.debug("Cấu hình quyền truy cập");
                    auth
                            .requestMatchers("/api/auth/**").permitAll()
                            .requestMatchers("/home").permitAll()
                            .requestMatchers("/api/feedbacks/**").permitAll()
                            .requestMatchers("/api/reports/**").permitAll()
                            // Bảo vệ endpoint /api/nurse-availability/**
                            .requestMatchers(HttpMethod.POST, "/api/nurse-availability").hasRole("nurse")
                            .requestMatchers(HttpMethod.PUT, "/api/nurse-availability/**").hasRole("nurse")
                            .requestMatchers(HttpMethod.DELETE, "/api/nurse-availability/**").hasRole("nurse")
                            .requestMatchers(HttpMethod.GET, "/api/nurse-availability/**").hasAnyRole("nurse", "family", "admin")
                            // Các endpoint /api/** khác
                            .requestMatchers("/api/family/**").hasRole("family")
                            .requestMatchers("/api/nurse/**").hasRole("nurse")
                            .requestMatchers("/api/admin/**").hasRole("admin")
                            .requestMatchers("/api/**").permitAll()
                            .anyRequest().authenticated();
                })
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        logger.info("SecurityFilterChain đã được cấu hình thành công");
        return http.build();
    }
}