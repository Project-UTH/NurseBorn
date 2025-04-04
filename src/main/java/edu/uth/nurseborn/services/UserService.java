package edu.uth.nurseborn.services;

import edu.uth.nurseborn.dtos.LoginRequestDTO;
import edu.uth.nurseborn.dtos.LoginResponseDTO;
import edu.uth.nurseborn.dtos.UserDTO;
import edu.uth.nurseborn.exception.DuplicateEmailException;
import edu.uth.nurseborn.exception.DuplicateUsernameException;
import edu.uth.nurseborn.jwt.JwtService;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.models.enums.Role;
import edu.uth.nurseborn.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public void registerUser(UserDTO userDTO) {
        logger.debug("Bắt đầu transaction để đăng ký người dùng: {}", userDTO);

        if(userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            logger.warn("Username đã tồn tại");
            throw new DuplicateUsernameException("Username " + userDTO.getUsername() + "đã được sử dụng");
        }

        // Kiểm tra xem email đã tồn tại chưa
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            logger.warn("Email đã tồn tại: {}", userDTO.getEmail());
            throw new DuplicateEmailException("Email " + userDTO.getEmail() + " đã được sử dụng");
        }

        try {
            User user = modelMapper.map(userDTO, User.class);
            user.setPasswordHash(passwordEncoder.encode(userDTO.getPassword()));
            user.setRole(Role.valueOf(userDTO.getRole()));
            logger.debug("User entity trước khi lưu: {}", user);
            User savedUser = userRepository.save(user);
            userRepository.flush(); // Flush dữ liệu ngay lập tức
            logger.info("Đã lưu người dùng thành công: {}", savedUser.getUsername());
        } catch (DataIntegrityViolationException e) {
            logger.error("Lỗi khi lưu người dùng: {}", e.getMessage());
            throw new DuplicateEmailException("Email " + userDTO.getEmail() + " đã được sử dụng");
        } catch (Exception e) {
            logger.error("Lỗi không mong muốn khi lưu người dùng: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi lưu người dùng: " + e.getMessage());
        }

        logger.debug("Transaction đã được commit thành công");
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        if (passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            String token = jwtService.taoToken(user.getUsername());
            LoginResponseDTO response = modelMapper.map(user, LoginResponseDTO.class);
            response.setToken(token);
            return response;
        }
        throw new RuntimeException("Sai mật khẩu");
    }
}