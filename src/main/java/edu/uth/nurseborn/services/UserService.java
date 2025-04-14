package edu.uth.nurseborn.services;

import edu.uth.nurseborn.dtos.LoginRequestDTO;
import edu.uth.nurseborn.dtos.LoginResponseDTO;
import edu.uth.nurseborn.dtos.NurseProfileDTO;
import edu.uth.nurseborn.dtos.FamilyProfileDTO;
import edu.uth.nurseborn.dtos.RegisterRequestDTO;
import edu.uth.nurseborn.dtos.UserDTO;
import edu.uth.nurseborn.exception.DuplicateEmailException;
import edu.uth.nurseborn.exception.DuplicateUsernameException;
import edu.uth.nurseborn.components.JwtTokenUtils;
import edu.uth.nurseborn.models.Token;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.models.enums.Role;
import edu.uth.nurseborn.repositories.TokenRepository;
import edu.uth.nurseborn.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtils jwtTokenUtil;

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

        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            logger.warn("Email đã tồn tại: {}", userDTO.getEmail());
            throw new DuplicateEmailException("Email " + userDTO.getEmail() + " đã được sử dụng");
        }

        try {
            User user = modelMapper.map(userDTO, User.class);
            user.setPasswordHash(passwordEncoder.encode(userDTO.getPassword()));
            user.setRole(Role.valueOf(userDTO.getRole().toUpperCase()));
            logger.debug("User entity trước khi lưu: {}", user);
            User savedUser = userRepository.save(user);
            userRepository.flush();
            logger.info("Đã lưu người dùng thành công: {}", savedUser.getUsername());

            if ("NURSE".equalsIgnoreCase(userDTO.getRole())) {
                NurseProfileDTO nurseProfileDTO = requestDTO.getNurseProfile();
                if (nurseProfileDTO == null) {
                    logger.warn("Thông tin NurseProfile không được cung cấp cho user với role 'Nurse'");
                    throw new IllegalArgumentException("Thông tin NurseProfile là bắt buộc cho user với role 'Nurse'");
                }
                nurseProfileDTO.setUserId(savedUser.getUserId());
                nurseProfileService.createNurseProfile(savedUser.getUserId(), nurseProfileDTO);
            }
            else if ("FAMILY".equalsIgnoreCase(userDTO.getRole())) {
                FamilyProfileDTO familyProfileDTO = requestDTO.getFamilyProfile();
                if (familyProfileDTO == null) {
                    logger.warn("Thông tin FamilyProfile không được cung cấp cho user với role 'Family'");
                    throw new IllegalArgumentException("Thông tin FamilyProfile là bắt buộc cho user với role 'Family'");
                }
                familyProfileDTO.setUserId(savedUser.getUserId());
                familyProfileService.createFamilyProfile(savedUser.getUserId(), familyProfileDTO);
            }

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
            try {
                String token = jwtTokenUtil.generateToken(user);
                Date expirationDate = jwtTokenUtil.extractClaim(token, Claims::getExpiration);
                LocalDateTime expiresAt = Instant.ofEpochMilli(expirationDate.getTime())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                Token tokenEntity = new Token();
                tokenEntity.setToken(token);
                tokenEntity.setUser(user);
                tokenEntity.setRevoked(false);
                tokenEntity.setExpiresAt(expiresAt);
                tokenRepository.save(tokenEntity);

                LoginResponseDTO response = modelMapper.map(user, LoginResponseDTO.class);
                response.setToken(token);
                response.setRole(user.getRole().name().toLowerCase());

                if ("NURSE".equalsIgnoreCase(user.getRole().name())) {
                    try {
                        NurseProfileDTO nurseProfileDTO = nurseProfileService.getNurseProfileByUserId(user.getUserId());
                        response.setNurseProfile(nurseProfileDTO);
                    } catch (Exception e) {
                        logger.warn("Không tìm thấy NurseProfile cho userId: {}", user.getUserId());
                    }
                } else if ("FAMILY".equalsIgnoreCase(user.getRole().name())) {
                    try {
                        FamilyProfileDTO familyProfileDTO = familyProfileService.getFamilyProfileByUserId(user.getUserId());
                        response.setFamilyProfile(familyProfileDTO);
                    } catch (Exception e) {
                        logger.warn("Không tìm thấy FamilyProfile cho userId: {}", user.getUserId());
                    }
                }

                return response;
            } catch (Exception e) {
                throw new RuntimeException("Cannot generate token: " + e.getMessage());
            }
        }
        throw new RuntimeException("Sai mật khẩu");
    }
}