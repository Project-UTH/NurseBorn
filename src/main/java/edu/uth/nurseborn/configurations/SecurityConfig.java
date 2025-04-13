package edu.uth.nurseborn.configurations;

import edu.uth.nurseborn.filters.JwtTokenFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity()
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private JwtTokenFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        logger.info("Tạo bean PasswordEncoder với BCryptPasswordEncoder");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        logger.info("Tạo bean AuthenticationManager");
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
                    logger.debug("Cấu hình quyền truy cập cho các endpoint");

                    auth
                            .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                            .requestMatchers("/api/auth/logout", "/api/auth/refresh-token").permitAll()
                            .requestMatchers("/home").permitAll()
                            .requestMatchers("/api/feedbacks/**").permitAll()
                            .requestMatchers("/api/reports/**").permitAll()
                            .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                            .requestMatchers(HttpMethod.POST, "/api/nurse-availability").hasRole("NURSE")
                            .requestMatchers(HttpMethod.PUT, "/api/nurse-availability/**").hasRole("NURSE")
                            .requestMatchers(HttpMethod.DELETE, "/api/nurse-availability/**").hasRole("NURSE")
                            .requestMatchers(HttpMethod.GET, "/api/nurse-availability/**").hasAnyRole("NURSE", "FAMILY", "ADMIN")

                            .requestMatchers("/api/nurse-profiles/**").hasRole("NURSE")
                            .requestMatchers("/api/family-profiles/**").hasRole("FAMILY")
                            .requestMatchers("/api/admin/**").hasRole("ADMIN")

                            .requestMatchers("/api/**").permitAll()

                            .anyRequest().authenticated();
                })
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        logger.info("SecurityFilterChain đã được cấu hình thành công");
        return http.build();
    }
}