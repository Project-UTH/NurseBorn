package edu.uth.nurseborn.services;

import edu.uth.nurseborn.dtos.CertificateDTO;
import edu.uth.nurseborn.dtos.NurseProfileDTO;
import edu.uth.nurseborn.exception.UserNotFoundException;
import edu.uth.nurseborn.models.Certificate;
import edu.uth.nurseborn.models.NurseProfile;
import edu.uth.nurseborn.models.User;
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
            // Ánh xạ NurseProfileDTO sang NurseProfile, nhưng không ánh xạ certificates
            NurseProfile nurseProfile = new NurseProfile();
            nurseProfile.setUser(user);
            nurseProfile.setLocation(nurseProfileDTO.getLocation());
            nurseProfile.setSkills(nurseProfileDTO.getSkills());
            nurseProfile.setExperienceYears(nurseProfileDTO.getExperienceYears());
            nurseProfile.setHourlyRate(nurseProfileDTO.getHourlyRate());
            nurseProfile.setDailyRate(nurseProfileDTO.getDailyRate());
            nurseProfile.setWeeklyRate(nurseProfileDTO.getWeeklyRate());
            nurseProfile.setBio(nurseProfileDTO.getBio());
            nurseProfile.setProfileImage(nurseProfileDTO.getProfileImage());
            nurseProfile.setApproved(nurseProfileDTO.getApproved());

            // Lưu NurseProfile trước để có ID
            logger.debug("NurseProfile entity trước khi lưu: {}", nurseProfile);
            NurseProfile savedProfile = nurseProfileRepository.save(nurseProfile);
            nurseProfileRepository.flush();

            // Tạo danh sách Certificate và gán vào savedProfile
            if (nurseProfileDTO.getCertificates() != null && !nurseProfileDTO.getCertificates().isEmpty()) {
                List<Certificate> certificateEntities = nurseProfileDTO.getCertificates().stream()
                        .map(dto -> {
                            Certificate certificate = new Certificate();
                            certificate.setNurseProfile(savedProfile);
                            certificate.setCertificateName(dto.getCertificateName());
                            certificate.setFilePath(dto.getFilePath());
                            return certificate;
                        })
                        .collect(Collectors.toList());
                savedProfile.setCertificates(certificateEntities);

                // Lưu lại NurseProfile để cascade lưu Certificates
                nurseProfileRepository.save(savedProfile);
            }

            logger.info("Đã lưu NurseProfile thành công cho user: {}", savedProfile.getUser().getUsername());

            NurseProfileDTO responseDTO = modelMapper.map(savedProfile, NurseProfileDTO.class);
            return responseDTO;
        } catch (Exception ex) {
            logger.error("Lỗi không mong muốn khi lưu NurseProfile: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi lưu NurseProfile: " + ex.getMessage(), ex);
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
            modelMapper.map(nurseProfileDTO, nurseProfile);

            // Cập nhật chứng chỉ nếu có
            if (nurseProfileDTO.getCertificates() != null) {
                List<Certificate> existingCertificates = certificateRepository.findByNurseProfileUserUserId(userId);
                for (Certificate certificate : existingCertificates) {
                    new File(certificate.getFilePath()).delete();
                }
                certificateRepository.deleteAll(existingCertificates);

                List<Certificate> newCertificates = nurseProfileDTO.getCertificates().stream()
                        .map(dto -> {
                            Certificate certificate = new Certificate();
                            certificate.setNurseProfile(nurseProfile);
                            certificate.setCertificateName(dto.getCertificateName());
                            certificate.setFilePath(dto.getFilePath());
                            return certificate;
                        })
                        .collect(Collectors.toList());
                nurseProfile.setCertificates(newCertificates);
            }

            logger.debug("NurseProfile entity trước khi cập nhật: {}", nurseProfile);
            NurseProfile updatedProfile = nurseProfileRepository.save(nurseProfile);
            nurseProfileRepository.flush();
            logger.info("Đã cập nhật NurseProfile thành công cho userId: {}", userId);

            return modelMapper.map(updatedProfile, NurseProfileDTO.class);
        } catch (Exception ex) {
            logger.error("Lỗi không mong muốn khi cập nhật NurseProfile: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi cập nhật NurseProfile: " + ex.getMessage(), ex);
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
        return modelMapper.map(nurseProfile, NurseProfileDTO.class);
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
            NurseProfile nurseProfile = nurseProfileOptional.get();
            List<Certificate> certificates = certificateRepository.findByNurseProfileUserUserId(userId);
            for (Certificate certificate : certificates) {
                new File(certificate.getFilePath()).delete();
            }
            certificateRepository.deleteAll(certificates);

            nurseProfileRepository.delete(nurseProfile);
            nurseProfileRepository.flush();
            logger.info("Đã xóa NurseProfile và chứng chỉ thành công cho userId: {}", userId);
        } catch (Exception ex) {
            logger.error("Lỗi không mong muốn khi xóa NurseProfile: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi xóa NurseProfile: " + ex.getMessage(), ex);
        }
    }

    public List<NurseProfileDTO> getAllNurseProfiles() {
        List<NurseProfile> nurseProfiles = nurseProfileRepository.findAll();
        return nurseProfiles.stream()
                .map(nurseProfile -> modelMapper.map(nurseProfile, NurseProfileDTO.class))
                .collect(Collectors.toList());
    }
}