package edu.uth.nurseborn.configurations;

import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.models.enums.Role;
import edu.uth.nurseborn.repositories.UserRepository;
import edu.uth.nurseborn.services.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Lớp khởi tạo dữ liệu ban đầu khi ứng dụng khởi động.
 */
@Configuration
public class DataInitializerConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializerConfig.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BookingService bookingService; // Thêm BookingService để gọi syncBookingsToNurseIncomes

    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            logger.info("Khởi tạo dữ liệu ban đầu...");

            // Kiểm tra xem tài khoản admin đã tồn tại chưa
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPasswordHash(passwordEncoder.encode("admin123"));
                admin.setEmail("admin@nurseborn.com");
                admin.setRole(Role.ADMIN);
                admin.setFullName("Admin User");
                admin.setPhoneNumber("1234567890");
                admin.setAddress("123 Admin St");
                admin.setVerified(true);

                userRepository.save(admin);
                logger.info("Tạo tài khoản admin thành công: username=admin, password=admin123");
            } else {
                logger.info("Tài khoản admin đã tồn tại, bỏ qua khởi tạo.");
            }

            // Đồng bộ dữ liệu từ bookings sang nurse_incomes
            try {
                bookingService.syncBookingsToNurseIncomes();
                logger.info("Đồng bộ dữ liệu từ bookings sang nurse_incomes thành công.");
            } catch (Exception e) {
                logger.error("Lỗi khi đồng bộ dữ liệu từ bookings sang nurse_incomes: {}", e.getMessage(), e);
            }
        };
    }
}