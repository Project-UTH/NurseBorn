package edu.uth.nurseborn.services;

import edu.uth.nurseborn.dtos.NurseAvailabilityDTO;
import edu.uth.nurseborn.models.NurseAvailability;
import edu.uth.nurseborn.models.NurseProfile;
import edu.uth.nurseborn.repositories.NurseAvailabilityRepository;
import edu.uth.nurseborn.repositories.NurseProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class NurseAvailabilityService {

    private static final Logger logger = LoggerFactory.getLogger(NurseAvailabilityService.class);

    @Autowired
    private NurseAvailabilityRepository nurseAvailabilityRepository;

    @Autowired
    private NurseProfileRepository nurseProfileRepository;

    @Transactional
    public NurseAvailabilityDTO createOrUpdateAvailability(Long userId, NurseAvailabilityDTO availabilityDTO) {
        logger.debug("Tạo hoặc cập nhật lịch làm việc cho userId: {}", userId);

        Optional<NurseProfile> nurseProfileOptional = nurseProfileRepository.findByUserUserId(userId);
        if (!nurseProfileOptional.isPresent()) {
            logger.warn("Không tìm thấy NurseProfile cho userId: {}", userId);
            throw new RuntimeException("Không tìm thấy NurseProfile cho user với ID: " + userId);
        }

        NurseProfile nurseProfile = nurseProfileOptional.get();
        if (!"NURSE".equals(nurseProfile.getUser().getRole().name())) {
            logger.warn("User với userId: {} không có role 'NURSE', role hiện tại: {}", userId, nurseProfile.getUser().getRole());
            throw new IllegalArgumentException("User phải có role 'NURSE' để tạo lịch làm việc");
        }

        try {
            // Xóa lịch cũ
            nurseAvailabilityRepository.deleteByNurseProfileNurseProfileId(nurseProfile.getNurseProfileId());
            logger.debug("Đã xóa lịch làm việc cũ cho nurseProfileId: {}", nurseProfile.getNurseProfileId());

            // Tạo lịch mới
            List<NurseAvailability> availabilities = new ArrayList<>();
            if (availabilityDTO.getSelectedDays() != null && !availabilityDTO.getSelectedDays().isEmpty()) {
                for (String day : availabilityDTO.getSelectedDays()) {
                    NurseAvailability availability = new NurseAvailability();
                    availability.setNurseProfile(nurseProfile);
                    availability.setDayOfWeek(day);
                    availabilities.add(availability);
                }
                nurseAvailabilityRepository.saveAll(availabilities);
                nurseAvailabilityRepository.flush();
                logger.info("Đã lưu lịch làm việc mới cho userId: {} với {} ngày", userId, availabilities.size());
            } else {
                logger.info("Không có ngày nào được chọn, lịch làm việc trống cho userId: {}", userId);
            }

            return availabilityDTO;
        } catch (Exception ex) {
            logger.error("Lỗi khi lưu lịch làm việc: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi lưu lịch làm việc: " + ex.getMessage());
        }
    }

    public NurseAvailabilityDTO getAvailabilityByUserId(Long userId) {
        logger.debug("Lấy lịch làm việc cho userId: {}", userId);

        Optional<NurseProfile> nurseProfileOptional = nurseProfileRepository.findByUserUserId(userId);
        if (!nurseProfileOptional.isPresent()) {
            logger.warn("Không tìm thấy NurseProfile cho userId: {}", userId);
            throw new RuntimeException("Không tìm thấy NurseProfile cho user với ID: " + userId);
        }

        List<NurseAvailability> availabilities = nurseAvailabilityRepository.findByNurseProfileNurseProfileId(nurseProfileOptional.get().getNurseProfileId());
        NurseAvailabilityDTO responseDTO = new NurseAvailabilityDTO();
        responseDTO.setUserId(userId);
        responseDTO.setSelectedDays(availabilities.stream()
                .map(NurseAvailability::getDayOfWeek)
                .toList());
        logger.info("Đã lấy lịch làm việc cho userId: {} với {} ngày", userId, responseDTO.getSelectedDays().size());
        return responseDTO;
    }
}