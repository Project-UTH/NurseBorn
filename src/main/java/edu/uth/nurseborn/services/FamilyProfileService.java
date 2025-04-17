package edu.uth.nurseborn.services;

import edu.uth.nurseborn.dtos.FamilyProfileDTO;
import edu.uth.nurseborn.exception.UserNotFoundException;
import edu.uth.nurseborn.models.FamilyProfile;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.repositories.FamilyProfileRepository;
import edu.uth.nurseborn.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class FamilyProfileService {

    private static final Logger logger = LoggerFactory.getLogger(FamilyProfileService.class);

    @Autowired
    private FamilyProfileRepository familyProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public FamilyProfileDTO createFamilyProfile(Long userId, FamilyProfileDTO familyProfileDTO) {
        logger.debug("Tạo FamilyProfile cho userId: {}", userId);

        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            logger.warn("Không tìm thấy user với userId: {}", userId);
            throw new UserNotFoundException("Không tìm thấy user với ID: " + userId);
        }

        User user = userOptional.get();
        if (!"FAMILY".equals(user.getRole().name())) {
            logger.warn("User với userId: {} không có role 'Family', role hiện tại: {}", userId, user.getRole());
            throw new IllegalArgumentException("User phải có role 'Family' để tạo FamilyProfile");
        }

        Optional<FamilyProfile> existingProfile = familyProfileRepository.findByUserUserId(userId);
        if (existingProfile.isPresent()) {
            logger.warn("FamilyProfile đã tồn tại cho userId: {}", userId);
            throw new IllegalStateException("FamilyProfile đã tồn tại cho user này");
        }

        try {
            FamilyProfile familyProfile = modelMapper.map(familyProfileDTO, FamilyProfile.class);
            familyProfile.setUser(user);

            logger.debug("FamilyProfile entity trước khi lưu: {}", familyProfile);
            FamilyProfile savedProfile = familyProfileRepository.save(familyProfile);
            familyProfileRepository.flush();
            logger.info("Đã lưu FamilyProfile thành công cho user: {}", savedProfile.getUser().getUsername());

            return modelMapper.map(savedProfile, FamilyProfileDTO.class);
        } catch (Exception ex) {
            logger.error("Lỗi không mong muốn khi lưu FamilyProfile: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi lưu FamilyProfile: " + ex.getMessage());
        }
    }

    @Transactional
    public FamilyProfileDTO updateFamilyProfile(Long userId, FamilyProfileDTO familyProfileDTO) {
        logger.debug("Cập nhật FamilyProfile cho userId: {}", userId);

        Optional<FamilyProfile> familyProfileOptional = familyProfileRepository.findByUserUserId(userId);
        if (!familyProfileOptional.isPresent()) {
            logger.warn("Không tìm thấy FamilyProfile cho userId: {}", userId);
            throw new RuntimeException("Không tìm thấy FamilyProfile cho user với ID: " + userId);
        }

        FamilyProfile familyProfile = familyProfileOptional.get();

        try {
            modelMapper.map(familyProfileDTO, familyProfile);

            logger.debug("FamilyProfile entity trước khi cập nhật: {}", familyProfile);
            FamilyProfile updatedProfile = familyProfileRepository.save(familyProfile);
            familyProfileRepository.flush();
            logger.info("Đã cập nhật FamilyProfile thành công cho userId: {}", userId);

            return modelMapper.map(updatedProfile, FamilyProfileDTO.class);
        } catch (Exception ex) {
            logger.error("Lỗi không mong muốn khi cập nhật FamilyProfile: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi cập nhật FamilyProfile: " + ex.getMessage());
        }
    }

    public FamilyProfileDTO getFamilyProfileByUserId(Long userId) {
        logger.debug("Lấy FamilyProfile cho userId: {}", userId);

        Optional<FamilyProfile> familyProfileOptional = familyProfileRepository.findByUserUserId(userId);
        if (!familyProfileOptional.isPresent()) {
            logger.warn("Không tìm thấy FamilyProfile cho userId: {}. Trả về DTO rỗng.", userId);
            FamilyProfileDTO emptyProfile = new FamilyProfileDTO();
            emptyProfile.setUserId(userId);
            return emptyProfile;
        }

        FamilyProfile familyProfile = familyProfileOptional.get();
        return modelMapper.map(familyProfile, FamilyProfileDTO.class);
    }

    @Transactional
    public void deleteFamilyProfile(Long userId) {
        logger.debug("Xóa FamilyProfile cho userId: {}", userId);

        Optional<FamilyProfile> familyProfileOptional = familyProfileRepository.findByUserUserId(userId);
        if (!familyProfileOptional.isPresent()) {
            logger.warn("Không tìm thấy FamilyProfile cho userId: {}", userId);
            throw new RuntimeException("Không tìm thấy FamilyProfile cho user với ID: " + userId);
        }

        try {
            familyProfileRepository.delete(familyProfileOptional.get());
            familyProfileRepository.flush();
            logger.info("Đã xóa FamilyProfile thành công cho userId: {}", userId);
        } catch (Exception ex) {
            logger.error("Lỗi không mong muốn khi xóa FamilyProfile: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi xóa FamilyProfile: " + ex.getMessage());
        }
    }
}