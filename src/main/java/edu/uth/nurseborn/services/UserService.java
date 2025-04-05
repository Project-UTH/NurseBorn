package edu.uth.nurseborn.services;

import edu.uth.nurseborn.dtos.LoginRequestDTO;
import edu.uth.nurseborn.dtos.LoginResponseDTO;
import edu.uth.nurseborn.dtos.NurseProfileDTO;
import edu.uth.nurseborn.dtos.FamilyProfileDTO;
import edu.uth.nurseborn.dtos.RegisterRequestDTO;
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

    @Autowired
    private NurseProfileService nurseProfileService;

    @Autowired
    private FamilyProfileService familyProfileService;

    @Transactional
    public UserDTO registerUser(RegisterRequestDTO requestDTO) {
        logger.debug("Bắt đầu transaction để đăng ký người dùng: {}", requestDTO);

        UserDTO userDTO = requestDTO.getUser();
        if (userDTO == null) {
            logger.warn("Thông tin người dùng không được cung cấp");
            throw new IllegalArgumentException("Thông tin người dùng là bắt buộc");
        }

        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            logger.warn("Username đã tồn tại");
            throw new DuplicateUsernameException("Username " + userDTO.getUsername() + " đã được sử dụng");
        }

        // Kiểm tra xem email đã tồn tại chưa
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            logger.warn("Email đã tồn tại: {}", userDTO.getEmail());
            throw new DuplicateEmailException("Email " + userDTO.getEmail() + " đã được sử dụng");
        }

        try {
            User user = modelMapper.map(userDTO, User.class);
            user.setPasswordHash(passwordEncoder.encode(userDTO.getPassword()));
            // Ánh xạ Role thủ công
            user.setRole(Role.valueOf(userDTO.getRole().toUpperCase()));
            logger.debug("User entity trước khi lưu: {}", user);
            User savedUser = userRepository.save(user);
            userRepository.flush(); // Flush dữ liệu ngay lập tức
            logger.info("Đã lưu người dùng thành công: {}", savedUser.getUsername());

            // Tạo NurseProfile nếu role là "Nurse"
            if ("NURSE".equalsIgnoreCase(userDTO.getRole())) {
                NurseProfileDTO nurseProfileDTO = requestDTO.getNurseProfile();
                if (nurseProfileDTO == null) {
                    logger.warn("Thông tin NurseProfile không được cung cấp cho user với role 'Nurse'");
                    throw new IllegalArgumentException("Thông tin NurseProfile là bắt buộc cho user với role 'Nurse'");
                }
                nurseProfileDTO.setUserId(savedUser.getUserId());
                nurseProfileService.createNurseProfile(savedUser.getUserId(), nurseProfileDTO);
            }
            // Tạo FamilyProfile nếu role là "Family"
            else if ("FAMILY".equalsIgnoreCase(userDTO.getRole())) {
                FamilyProfileDTO familyProfileDTO = requestDTO.getFamilyProfile();
                if (familyProfileDTO == null) {
                    logger.warn("Thông tin FamilyProfile không được cung cấp cho user với role 'Family'");
                    throw new IllegalArgumentException("Thông tin FamilyProfile là bắt buộc cho user với role 'Family'");
                }
                familyProfileDTO.setUserId(savedUser.getUserId());
                familyProfileService.createFamilyProfile(savedUser.getUserId(), familyProfileDTO);
            }

            // Ánh xạ entity trở lại DTO để trả về
            UserDTO responseDTO = modelMapper.map(savedUser, UserDTO.class);
            responseDTO.setRole(savedUser.getRole().name().toLowerCase());
            return responseDTO;
        } catch (DataIntegrityViolationException e) {
            logger.error("Lỗi khi lưu người dùng: {}", e.getMessage());
            throw new DuplicateEmailException("Email " + userDTO.getEmail() + " đã được sử dụng");
        } catch (IllegalArgumentException e) {
            logger.warn("Giá trị role không hợp lệ: {}", userDTO.getRole());
            throw new IllegalArgumentException("Giá trị role không hợp lệ: " + userDTO.getRole());
        } catch (Exception e) {
            logger.error("Lỗi không mong muốn khi lưu người dùng: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi lưu người dùng: " + e.getMessage());
        }
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        if (passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            String token = jwtService.taoToken(user.getUsername());
            LoginResponseDTO response = modelMapper.map(user, LoginResponseDTO.class);
            // Ánh xạ Role thủ công
            response.setRole(user.getRole().name().toLowerCase());
            response.setToken(token);
            return response;
        }
        throw new RuntimeException("Sai mật khẩu");
    }
}