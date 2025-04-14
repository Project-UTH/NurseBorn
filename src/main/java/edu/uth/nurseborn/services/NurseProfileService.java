package edu.uth.nurseborn.services;

import edu.uth.nurseborn.dtos.NurseProfileDTO;
import edu.uth.nurseborn.exception.UserNotFoundException;
import edu.uth.nurseborn.models.NurseProfile;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.repositories.NurseProfileRepository;
import edu.uth.nurseborn.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NurseProfileService {

    private static final Logger logger = LoggerFactory.getLogger(NurseProfileService.class);

    @Autowired
    private NurseProfileRepository nurseProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * Tạo một NurseProfile cho người dùng với role = "Nurse".
     * Phương thức này được gọi sau khi một User được tạo thành công.
     *
     * @param userId ID của người dùng
     * @param nurseProfileDTO DTO chứa thông tin NurseProfile
     * @return NurseProfileDTO đã được tạo
     */
    @Transactional
    public NurseProfileDTO createNurseProfile(Long userId, NurseProfileDTO nurseProfileDTO) {
        logger.debug("Tạo NurseProfile cho userId: {}", userId);

        // Tìm user theo userId
        Optional<User> userOptional = userRepository.findById(Long.valueOf(userId));
        if (!userOptional.isPresent()) {
            logger.warn("Không tìm thấy user với userId: {}", userId);
            throw new UserNotFoundException("Không tìm thấy user với ID: " + userId);
        }

        User user = userOptional.get();

        // Kiểm tra role của user
        if (!"NURSE".equals(user.getRole().name())) {
            logger.warn("User với userId: {} không có role 'Nurse', role hiện tại: {}", userId, user.getRole());
            throw new IllegalArgumentException("User phải có role 'Nurse' để tạo NurseProfile");
        }

        // Kiểm tra xem NurseProfile đã tồn tại chưa
        Optional<NurseProfile> existingProfile = nurseProfileRepository.findByUserUserId(userId);
        if (existingProfile.isPresent()) {
            logger.warn("NurseProfile đã tồn tại cho userId: {}", userId);
            throw new IllegalStateException("NurseProfile đã tồn tại cho user này");
        }

        try {
            // Ánh xạ DTO sang entity
            NurseProfile nurseProfile = modelMapper.map(nurseProfileDTO, NurseProfile.class);
            nurseProfile.setUser(user);

            logger.debug("NurseProfile entity trước khi lưu: {}", nurseProfile);
            NurseProfile savedProfile = nurseProfileRepository.save(nurseProfile);
            nurseProfileRepository.flush(); // Flush dữ liệu ngay lập tức
            logger.info("Đã lưu NurseProfile thành công cho user: {}", savedProfile.getUser().getUsername());

            // Ánh xạ entity trở lại DTO để trả về
            return modelMapper.map(savedProfile, NurseProfileDTO.class);
        } catch (Exception ex) {
            logger.error("Lỗi không mong muốn khi lưu NurseProfile: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi lưu NurseProfile: " + ex.getMessage());
        }
    }

    /**
     * Cập nhật thông tin NurseProfile.
     *
     * @param userId ID của người dùng
     * @param nurseProfileDTO DTO chứa thông tin cập nhật
     * @return NurseProfileDTO đã được cập nhật
     */
    @Transactional
    public NurseProfileDTO updateNurseProfile(Long userId, NurseProfileDTO nurseProfileDTO) {
        logger.debug("Cập nhật NurseProfile cho userId: {}", userId);

        // Kiểm tra xem NurseProfile có tồn tại không
        Optional<NurseProfile> nurseProfileOptional = nurseProfileRepository.findByUserUserId(userId);
        if (!nurseProfileOptional.isPresent()) {
            logger.warn("Không tìm thấy NurseProfile cho userId: {}", userId);
            throw new RuntimeException("Không tìm thấy NurseProfile cho user với ID: " + userId);
        }

        NurseProfile nurseProfile = nurseProfileOptional.get();

        try {
            // Ánh xạ DTO sang entity, bỏ qua các trường null
            modelMapper.map(nurseProfileDTO, nurseProfile);

            logger.debug("NurseProfile entity trước khi cập nhật: {}", nurseProfile);
            NurseProfile updatedProfile = nurseProfileRepository.save(nurseProfile);
            nurseProfileRepository.flush(); // Flush dữ liệu ngay lập tức
            logger.info("Đã cập nhật NurseProfile thành công cho userId: {}", userId);

            // Ánh xạ entity trở lại DTO để trả về
            return modelMapper.map(updatedProfile, NurseProfileDTO.class);
        } catch (Exception ex) {
            logger.error("Lỗi không mong muốn khi cập nhật NurseProfile: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi cập nhật NurseProfile: " + ex.getMessage());
        }
    }

    /**
     * Lấy thông tin NurseProfile theo userId.
     *
     * @param userId ID của người dùng
     * @return NurseProfileDTO
     */
    public NurseProfileDTO getNurseProfileByUserId(Long userId) {
        logger.debug("Lấy NurseProfile cho userId: {}", userId);

        Optional<NurseProfile> nurseProfileOptional = nurseProfileRepository.findByUserUserId(userId);
        if (!nurseProfileOptional.isPresent()) {
            logger.warn("Không tìm thấy NurseProfile cho userId: {}", userId);
            throw new RuntimeException("Không tìm thấy NurseProfile cho user với ID: " + userId);
        }

        NurseProfile nurseProfile = nurseProfileOptional.get();
        // Ánh xạ entity sang DTO
        return modelMapper.map(nurseProfile, NurseProfileDTO.class);
    }

    /**
     * Xóa NurseProfile theo userId.
     *
     * @param userId ID của người dùng
     */
    @Transactional
    public void deleteNurseProfile(Long userId) {
        logger.debug("Xóa NurseProfile cho userId: {}", userId);

        Optional<NurseProfile> nurseProfileOptional = nurseProfileRepository.findByUserUserId(userId);
        if (!nurseProfileOptional.isPresent()) {
            logger.warn("Không tìm thấy NurseProfile cho userId: {}", userId);
            throw new RuntimeException("Không tìm thấy NurseProfile cho user với ID: " + userId);
        }

        try {
            nurseProfileRepository.delete(nurseProfileOptional.get());
            nurseProfileRepository.flush(); // Flush dữ liệu ngay lập tức
            logger.info("Đã xóa NurseProfile thành công cho userId: {}", userId);
        } catch (Exception ex) {
            logger.error("Lỗi không mong muốn khi xóa NurseProfile: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi xóa NurseProfile: " + ex.getMessage());
        }
    }
    // Thêm phương thức getAllNurseProfiles
    public List<NurseProfileDTO> getAllNurseProfiles() {
        List<NurseProfile> nurseProfiles = nurseProfileRepository.findAll();
        return nurseProfiles.stream()
                .map(nurseProfile -> modelMapper.map(nurseProfile, NurseProfileDTO.class))
                .collect(Collectors.toList());
    }
}