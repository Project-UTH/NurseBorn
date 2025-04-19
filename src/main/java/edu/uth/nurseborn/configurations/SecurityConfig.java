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
@EnableMethodSecurity(prePostEnabled = true)
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
                    logger.debug("Vô hiệu hóa CSRF cho API, nhưng bật cho form web");
                    csrf.ignoringRequestMatchers("/api/**");
                })
                .sessionManagement(session -> {
                    logger.debug("Cấu hình session stateless cho API, stateful cho web");
                    session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
                })
                .authorizeHttpRequests(auth -> {
                    logger.debug("Cấu hình quyền truy cập cho các endpoint");

                    // Public endpoints
                    auth
                            .requestMatchers("/", "/login", "/register", "/logout", "/role-selection", "/register/nurse", "/register/family").permitAll()
                            .requestMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**").permitAll()
                            .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/logout", "/api/auth/refresh-token").permitAll()
                            .requestMatchers("/api/feedbacks/**", "/api/reports/**").permitAll()
                            .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                            // Admin endpoints
                            .requestMatchers("/admin/**", "/review-nurse-profile", "/nurse/approve/**", "/nurse/reject/**").hasRole("ADMIN")
                            .requestMatchers("/statistics-table/**", "/statistics/**").hasRole("ADMIN")

                            // Family endpoints
                            .requestMatchers("/family/**", "/nursepage", "/nurse_review/**").hasRole("FAMILY")

                            // Nurse endpoints
                            .requestMatchers("/nurse/income").hasRole("NURSE") // Thêm để cho phép y tá truy cập thống kê thu nhập
                            .requestMatchers(HttpMethod.POST, "/api/nurse-availability").hasRole("NURSE")
                            .requestMatchers(HttpMethod.PUT, "/api/nurse-availability/**").hasRole("NURSE")
                            .requestMatchers(HttpMethod.DELETE, "/api/nurse-availability/**").hasRole("NURSE")
                            .requestMatchers(HttpMethod.GET, "/api/nurse-availability/**").hasAnyRole("NURSE", "FAMILY", "ADMIN")
                            .requestMatchers("/api/nurse-profiles/**").hasRole("NURSE")
                            .requestMatchers("/nurse-profile", "/update-nurse").hasRole("NURSE")
                            .requestMatchers("/nurse-availability", "/nurse-schedule").hasRole("NURSE")
                            .requestMatchers("/nurse/pending-bookings", "/nurse/accept-booking").hasRole("NURSE")

                            // API endpoints with role-based access
                            .requestMatchers("/api/family-profiles/**").hasRole("FAMILY")
                            .requestMatchers("/api/admin/**").hasRole("ADMIN")
                            .requestMatchers("/api/**").permitAll()
                            .requestMatchers("/user-profile").hasAnyRole("FAMILY", "NURSE", "ADMIN")
                            .requestMatchers("/update-user").hasAnyRole("FAMILY", "NURSE", "ADMIN")
                            .requestMatchers("/nurse-service/**").hasAnyRole("NURSE", "FAMILY")

                            // Web endpoints
                            .requestMatchers("/dashboard", "/create-profile", "/manage-services", "/feedbacks", "/messages").authenticated()
                            .requestMatchers("/hired-nurses").hasRole("FAMILY")
                            .requestMatchers("/job-requests", "/track-income").hasRole("NURSE")
                            .requestMatchers("/nursepage/**").permitAll()
                            .requestMatchers("/nurse_review/**").permitAll()

                            .anyRequest().authenticated();
                })
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureHandler((request, response, exception) -> {
                            request.setAttribute("error", exception.getMessage());
                            response.sendRedirect("/login?error");
                        }))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("token"))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        logger.info("SecurityFilterChain đã được cấu hình thành công");
        return http.build();
    }
}