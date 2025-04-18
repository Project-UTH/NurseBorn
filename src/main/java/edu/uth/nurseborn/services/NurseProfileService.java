package edu.uth.nurseborn.services;

import edu.uth.nurseborn.dtos.AdminActionDTO;
import edu.uth.nurseborn.dtos.CertificateDTO;
import edu.uth.nurseborn.dtos.NurseProfileDTO;
import edu.uth.nurseborn.exception.UserNotFoundException;
import edu.uth.nurseborn.models.Certificate;
import edu.uth.nurseborn.models.NurseProfile;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.models.enums.ActionType;
import edu.uth.nurseborn.repositories.CertificateRepository;
import edu.uth.nurseborn.repositories.NurseProfileRepository;
import edu.uth.nurseborn.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.ArrayList;
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
    private CertificateRepository certificateRepository;

    @Autowired
    private AdminActionService adminActionService;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public NurseProfileDTO createNurseProfile(Long userId, NurseProfileDTO nurseProfileDTO) {
        logger.debug("Tạo NurseProfile cho userId: {}", userId);
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            logger.warn("Không tìm thấy user với userId: {}", userId);
            throw new UserNotFoundException("Không tìm thấy user với ID: " + userId);
        }

        User user = userOptional.get();

        if (!"NURSE".equals(user.getRole().name())) {
            logger.warn("User với userId: {} không có role 'Nurse', role hiện tại: {}", userId, user.getRole());
            throw new IllegalArgumentException("User phải có role 'Nurse' để tạo NurseProfile");
        }

        Optional<NurseProfile> existingProfile = nurseProfileRepository.findByUserUserId(userId);
        if (existingProfile.isPresent()) {
            logger.warn("NurseProfile đã tồn tại cho userId: {}", userId);
            throw new IllegalStateException("NurseProfile đã tồn tại cho user này");
        }

        try {
            NurseProfile nurseProfile = modelMapper.map(nurseProfileDTO, NurseProfile.class);
            nurseProfile.setUser(user);

            // Xử lý danh sách certificates
            List<Certificate> certificates = new ArrayList<>();
            if (nurseProfileDTO.getCertificates() != null && !nurseProfileDTO.getCertificates().isEmpty()) {
                for (CertificateDTO certificateDTO : nurseProfileDTO.getCertificates()) {
                    Certificate certificate = modelMapper.map(certificateDTO, Certificate.class);
                    // Thiết lập mối quan hệ với NurseProfile
                    certificate.setNurseProfile(nurseProfile);
                    certificates.add(certificate);
                }
            }
            nurseProfile.setCertificates(certificates);

            logger.debug("NurseProfile entity trước khi lưu: {}", nurseProfile);
            NurseProfile savedProfile = nurseProfileRepository.save(nurseProfile);
            nurseProfileRepository.flush();
            logger.info("Đã lưu NurseProfile thành công cho user: {}", savedProfile.getUser().getUsername());

            NurseProfileDTO responseDTO = modelMapper.map(savedProfile, NurseProfileDTO.class);
            List<Certificate> savedCertificates = certificateRepository.findByNurseProfileUserUserId(userId);
            List<CertificateDTO> certificateDTOs = savedCertificates.stream()
                    .map(certificate -> modelMapper.map(certificate, CertificateDTO.class))
                    .collect(Collectors.toList());
            responseDTO.setCertificates(certificateDTOs);
            return responseDTO;
        } catch (Exception ex) {
            logger.error("Lỗi không mong muốn khi lưu NurseProfile: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi lưu NurseProfile: " + ex.getMessage());
        }
    }

    @Transactional
    public NurseProfileDTO updateNurseProfile(Long userId, NurseProfileDTO nurseProfileDTO) {
        logger.debug("Cập nhật NurseProfile cho userId: {}", userId);
        Optional<NurseProfile> nurseProfileOptional = nurseProfileRepository.findByUserUserId(userId);
        if (!nurseProfileOptional.isPresent()) {
            logger.warn("Không tìm thấy NurseProfile cho userId: {}", userId);
            throw new RuntimeException("Không tìm thấy NurseProfile cho user với ID: " + userId);
        }

        NurseProfile nurseProfile = nurseProfileOptional.get();

        try {
            // Ánh xạ dữ liệu từ DTO sang entity, trừ certificates
            modelMapper.map(nurseProfileDTO, nurseProfile);

            // Xử lý danh sách certificates
            nurseProfile.getCertificates().clear(); // Xóa các certificate cũ
            List<Certificate> certificates = new ArrayList<>();
            if (nurseProfileDTO.getCertificates() != null && !nurseProfileDTO.getCertificates().isEmpty()) {
                for (CertificateDTO certificateDTO : nurseProfileDTO.getCertificates()) {
                    Certificate certificate = modelMapper.map(certificateDTO, Certificate.class);
                    // Thiết lập mối quan hệ với NurseProfile
                    certificate.setNurseProfile(nurseProfile);
                    certificates.add(certificate);
                }
            }
            nurseProfile.setCertificates(certificates);

            logger.debug("NurseProfile entity trước khi cập nhật: {}", nurseProfile);
            NurseProfile updatedProfile = nurseProfileRepository.save(nurseProfile);
            nurseProfileRepository.flush();
            logger.info("Đã cập nhật NurseProfile thành công cho userId: {}", userId);

            NurseProfileDTO responseDTO = modelMapper.map(updatedProfile, NurseProfileDTO.class);
            List<Certificate> savedCertificates = certificateRepository.findByNurseProfileUserUserId(userId);
            List<CertificateDTO> certificateDTOs = savedCertificates.stream()
                    .map(certificate -> modelMapper.map(certificate, CertificateDTO.class))
                    .collect(Collectors.toList());
            responseDTO.setCertificates(certificateDTOs);
            return responseDTO;
        } catch (Exception ex) {
            logger.error("Lỗi không mong muốn khi cập nhật NurseProfile: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi cập nhật NurseProfile: " + ex.getMessage());
        }
    }

    public NurseProfileDTO getNurseProfileByUserId(Long userId) {
        logger.debug("Lấy NurseProfile cho userId: {}", userId);
        Optional<NurseProfile> nurseProfileOptional = nurseProfileRepository.findByUserUserId(userId);
        if (!nurseProfileOptional.isPresent()) {
            logger.warn("Không tìm thấy NurseProfile cho userId: {}", userId);
            throw new RuntimeException("Không tìm thấy NurseProfile cho user với ID: " + userId);
        }

        NurseProfile nurseProfile = nurseProfileOptional.get();
        NurseProfileDTO responseDTO = modelMapper.map(nurseProfile, NurseProfileDTO.class);
        responseDTO.setFullName(nurseProfile.getUser().getFullName());
        responseDTO.setIsVerified(nurseProfile.getUser().getVerified());
        List<Certificate> certificates = certificateRepository.findByNurseProfileUserUserId(userId);
        List<CertificateDTO> certificateDTOs = certificates.stream()
                .map(certificate -> modelMapper.map(certificate, CertificateDTO.class))
                .collect(Collectors.toList());
        responseDTO.setCertificates(certificateDTOs);
        String certificateNames = certificates.stream()
                .map(Certificate::getCertificateName)
                .collect(Collectors.joining(", "));
        responseDTO.setCertificateNames(certificateNames.isEmpty() ? "Chưa có" : certificateNames);
        return responseDTO;
    }
    @Transactional
    public void deleteNurseProfile(Long userId) {
        logger.debug("Xóa NurseProfile cho userId: {}", userId);
        Optional<NurseProfile> nurseProfileOptional = nurseProfileRepository.findByUserUserId(userId);
        if (!nurseProfileOptional.isPresent()) {
            logger.warn("Không tìm thấy NurseProfile cho userId: {}", userId);
            throw new RuntimeException("Không tìm thấy NurseProfile cho user với ID: " + userId);
        }

        try {
            List<Certificate> certificates = certificateRepository.findByNurseProfileUserUserId(userId);
            for (Certificate certificate : certificates) {
                new File(certificate.getFilePath()).delete();
            }
            certificateRepository.deleteAll(certificates);

            nurseProfileRepository.delete(nurseProfileOptional.get());
            nurseProfileRepository.flush();
            logger.info("Đã xóa NurseProfile và chứng chỉ thành công cho userId: {}", userId);
        } catch (Exception ex) {
            logger.error("Lỗi không mong muốn khi xóa NurseProfile: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi xóa NurseProfile: " + ex.getMessage());
        }
    }

    @Transactional
    public void updateApprovalStatus(Long userId, Boolean isApproved, User admin) {
        logger.debug("Cập nhật trạng thái phê duyệt cho userId: {} thành {}", userId, isApproved);
        Optional<NurseProfile> nurseProfileOptional = nurseProfileRepository.findByUserUserId(userId);
        if (!nurseProfileOptional.isPresent()) {
            logger.warn("Không tìm thấy NurseProfile cho userId: {}", userId);
            throw new RuntimeException("Không tìm thấy hồ sơ y tá cho user với ID: " + userId);
        }

        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            logger.warn("Không tìm thấy User với userId: {}", userId);
            throw new UserNotFoundException("Không tìm thấy user với ID: " + userId);
        }

        NurseProfile nurseProfile = nurseProfileOptional.get();
        User user = userOptional.get();

        if (isApproved) {
            // Phê duyệt: Cập nhật trạng thái
            nurseProfile.setApproved(true);
            user.setVerified(true);
            nurseProfileRepository.save(nurseProfile);
            userRepository.save(user);

            // Lưu hành động APPROVE_USER
            AdminActionDTO adminActionDTO = new AdminActionDTO();
            adminActionDTO.setActionId(null);
            adminActionDTO.setAdminUserId(admin.getUserId().intValue());
            adminActionDTO.setActionType(ActionType.APPROVE_USER.name().toLowerCase());
            adminActionDTO.setTargetUserId(user.getUserId().intValue());
            adminActionDTO.setDescription("Phê duyệt hồ sơ y tá cho userId: " + userId);
            adminActionDTO.setActionDate(null);

            adminActionService.createAdminAction(adminActionDTO);
            logger.info("Đã phê duyệt hồ sơ y tá thành công cho userId: {}", userId);
        } else {
            // Từ chối: Xóa user và nurse profile
            try {
                // Xóa file vật lý của chứng chỉ
                List<Certificate> certificates = certificateRepository.findByNurseProfileUserUserId(userId);
                for (Certificate certificate : certificates) {
                    new File(certificate.getFilePath()).delete();
                }

                // Xóa NurseProfile (tự động xóa certificates do cascade)
                nurseProfileRepository.delete(nurseProfile);
                // Xóa User
                userRepository.delete(user);
                logger.info("Đã từ chối và xóa hồ sơ y tá cùng user thành công cho userId: {}", userId);
            } catch (Exception ex) {
                logger.error("Lỗi khi từ chối và xóa hồ sơ y tá: {}", ex.getMessage(), ex);
                throw new RuntimeException("Lỗi khi từ chối và xóa hồ sơ y tá: " + ex.getMessage());
            }
        }
    }

    public List<NurseProfileDTO> getAllNurseProfiles() {
        // Chỉ lấy các hồ sơ chưa được phê duyệt (isApproved = false)
        List<NurseProfile> nurseProfiles = nurseProfileRepository.findAll().stream()
                .filter(nurseProfile -> !nurseProfile.getApproved())
                .collect(Collectors.toList());

        return nurseProfiles.stream()
                .map(nurseProfile -> {
                    NurseProfileDTO dto = modelMapper.map(nurseProfile, NurseProfileDTO.class);
                    dto.setFullName(nurseProfile.getUser().getFullName());
                    dto.setIsVerified(nurseProfile.getUser().getVerified());
                    List<Certificate> certificates = certificateRepository.findByNurseProfileUserUserId(nurseProfile.getUser().getUserId());
                    List<CertificateDTO> certificateDTOs = certificates.stream()
                            .map(certificate -> modelMapper.map(certificate, CertificateDTO.class))
                            .collect(Collectors.toList());
                    dto.setCertificates(certificateDTOs);
                    String certificateNames = certificates.stream()
                            .map(Certificate::getCertificateName)
                            .collect(Collectors.joining(", "));
                    dto.setCertificateNames(certificateNames.isEmpty() ? "Chưa có" : certificateNames);
                    return dto;
                })
                .collect(Collectors.toList());
    }
}