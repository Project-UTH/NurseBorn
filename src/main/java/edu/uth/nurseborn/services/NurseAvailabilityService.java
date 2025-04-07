package edu.uth.nurseborn.services;

import edu.uth.nurseborn.dtos.NurseAvailabilityDTO;
import edu.uth.nurseborn.models.NurseAvailability;
import edu.uth.nurseborn.models.NurseProfile;
import edu.uth.nurseborn.models.enums.DayOfWeek;
import edu.uth.nurseborn.repositories.NurseAvailabilityRepository;
import edu.uth.nurseborn.repositories.NurseProfileRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NurseAvailabilityService {

    private static final Logger logger = LoggerFactory.getLogger(NurseAvailabilityService.class);

    private final NurseAvailabilityRepository nurseAvailabilityRepository;
    private final NurseProfileRepository nurseProfileRepository;

    public NurseAvailabilityService(NurseAvailabilityRepository nurseAvailabilityRepository,
                                    NurseProfileRepository nurseProfileRepository) {
        this.nurseAvailabilityRepository = nurseAvailabilityRepository;
        this.nurseProfileRepository = nurseProfileRepository;
    }

    @Transactional
    public NurseAvailabilityDTO createNurseAvailability(NurseAvailabilityDTO nurseAvailabilityDTO) {
        logger.debug("Bắt đầu tạo lịch sẵn sàng: {}", nurseAvailabilityDTO);

        validateAvailabilityDTO(nurseAvailabilityDTO);

        // Chuyển đổi nurseProfileId từ Integer sang Long để gọi repository
        Long nurseProfileId = nurseAvailabilityDTO.getNurseProfileId().longValue();
        NurseProfile nurseProfile = nurseProfileRepository.findById(nurseProfileId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nurse profile với ID: " + nurseProfileId));

        DayOfWeek dayOfWeek = DayOfWeek.valueOf(nurseAvailabilityDTO.getDayOfWeek().toUpperCase());
        LocalTime startTime = nurseAvailabilityDTO.getStartTime().toLocalTime();
        LocalTime endTime = nurseAvailabilityDTO.getEndTime().toLocalTime();

        if (isTimeOverlapping(nurseProfile, dayOfWeek, startTime, endTime, null)) {
            throw new IllegalArgumentException("Thời gian này chồng lấp với lịch sẵn sàng khác trong " + dayOfWeek);
        }

        NurseAvailability nurseAvailability = mapToEntity(nurseAvailabilityDTO, nurseProfile);

        logger.debug("NurseAvailability entity trước khi lưu: {}", nurseAvailability);

        NurseAvailability savedAvailability = nurseAvailabilityRepository.save(nurseAvailability);
        nurseAvailabilityRepository.flush();
        logger.info("Đã tạo lịch sẵn sàng thành công với ID: {}", savedAvailability.getAvailabilityId());

        return mapToDTO(savedAvailability);
    }

    @Transactional
    public NurseAvailabilityDTO updateNurseAvailability(Integer availabilityId, NurseAvailabilityDTO nurseAvailabilityDTO) {
        logger.debug("Bắt đầu cập nhật lịch sẵn sàng với ID: {}", availabilityId);

        validateAvailabilityDTO(nurseAvailabilityDTO);

        NurseAvailability existingAvailability = nurseAvailabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch sẵn sàng với ID: " + availabilityId));

        Long nurseProfileId = nurseAvailabilityDTO.getNurseProfileId().longValue();
        NurseProfile nurseProfile = nurseProfileRepository.findById(nurseProfileId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nurse profile với ID: " + nurseProfileId));

        DayOfWeek dayOfWeek = DayOfWeek.valueOf(nurseAvailabilityDTO.getDayOfWeek().toUpperCase());
        LocalTime startTime = nurseAvailabilityDTO.getStartTime().toLocalTime();
        LocalTime endTime = nurseAvailabilityDTO.getEndTime().toLocalTime();

        if (isTimeOverlapping(nurseProfile, dayOfWeek, startTime, endTime, availabilityId)) {
            throw new IllegalArgumentException("Thời gian này chồng lấp với lịch sẵn sàng khác trong " + dayOfWeek);
        }

        existingAvailability.setNurseProfile(nurseProfile);
        existingAvailability.setDayOfWeek(dayOfWeek);
        existingAvailability.setStartTime(startTime);
        existingAvailability.setEndTime(endTime);

        logger.debug("NurseAvailability entity trước khi cập nhật: {}", existingAvailability);

        NurseAvailability updatedAvailability = nurseAvailabilityRepository.save(existingAvailability);
        logger.info("Đã cập nhật lịch sẵn sàng thành công với ID: {}", updatedAvailability.getAvailabilityId());

        return mapToDTO(updatedAvailability);
    }

    public List<NurseAvailabilityDTO> getAvailabilitiesByNurseProfileId(Integer nurseProfileId) {
        logger.debug("Tìm lịch sẵn sàng với nurseProfileId: {}", nurseProfileId);

        Long nurseProfileIdLong = nurseProfileId.longValue();
        NurseProfile nurseProfile = nurseProfileRepository.findById(nurseProfileIdLong)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nurse profile với ID: " + nurseProfileIdLong));

        List<NurseAvailability> availabilities = nurseAvailabilityRepository.findByNurseProfile(nurseProfile);
        return availabilities.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public NurseAvailabilityDTO getNurseAvailabilityById(Integer availabilityId) {
        logger.debug("Tìm lịch sẵn sàng với availabilityId: {}", availabilityId);

        NurseAvailability nurseAvailability = nurseAvailabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch sẵn sàng với ID: " + availabilityId));

        return mapToDTO(nurseAvailability);
    }

    @Transactional
    public void deleteNurseAvailability(Integer availabilityId) {
        logger.debug("Xóa lịch sẵn sàng với availabilityId: {}", availabilityId);

        NurseAvailability nurseAvailability = nurseAvailabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch sẵn sàng với ID: " + availabilityId));

        nurseAvailabilityRepository.delete(nurseAvailability);
        logger.info("Đã xóa lịch sẵn sàng với availabilityId: {}", availabilityId);
    }

    public List<NurseAvailabilityDTO> getAvailabilitiesByDayOfWeek(Integer nurseProfileId, String dayOfWeek) {
        logger.debug("Tìm lịch sẵn sàng với nurseProfileId: {} và dayOfWeek: {}", nurseProfileId, dayOfWeek);

        Long nurseProfileIdLong = nurseProfileId.longValue();
        NurseProfile nurseProfile = nurseProfileRepository.findById(nurseProfileIdLong)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nurse profile với ID: " + nurseProfileIdLong));

        List<NurseAvailability> availabilities = nurseAvailabilityRepository.findByNurseProfileAndDayOfWeek(
                nurseProfile, DayOfWeek.valueOf(dayOfWeek.toUpperCase()));

        return availabilities.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private void validateAvailabilityDTO(NurseAvailabilityDTO dto) {
        if (dto.getNurseProfileId() == null) {
            throw new IllegalArgumentException("NurseProfileId không được để trống");
        }
        if (dto.getDayOfWeek() == null || dto.getDayOfWeek().isEmpty()) {
            throw new IllegalArgumentException("DayOfWeek không được để trống");
        }
        if (dto.getStartTime() == null || dto.getEndTime() == null) {
            throw new IllegalArgumentException("Thời gian bắt đầu và kết thúc không được để trống");
        }
        try {
            DayOfWeek.valueOf(dto.getDayOfWeek().toUpperCase());
            LocalTime startTime = dto.getStartTime().toLocalTime();
            LocalTime endTime = dto.getEndTime().toLocalTime();
            if (startTime.isAfter(endTime)) {
                throw new IllegalArgumentException("Giờ bắt đầu phải trước giờ kết thúc");
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("DayOfWeek không hợp lệ: " + e.getMessage());
        } catch (Exception e) {
            throw new IllegalArgumentException("Định dạng thời gian không hợp lệ: " + e.getMessage());
        }
    }

    private NurseAvailability mapToEntity(NurseAvailabilityDTO dto, NurseProfile nurseProfile) {
        NurseAvailability nurseAvailability = new NurseAvailability();
        nurseAvailability.setNurseProfile(nurseProfile);
        nurseAvailability.setDayOfWeek(DayOfWeek.valueOf(dto.getDayOfWeek().toUpperCase()));
        nurseAvailability.setStartTime(dto.getStartTime().toLocalTime());
        nurseAvailability.setEndTime(dto.getEndTime().toLocalTime());
        return nurseAvailability;
    }

    private NurseAvailabilityDTO mapToDTO(NurseAvailability nurseAvailability) {
        NurseAvailabilityDTO dto = new NurseAvailabilityDTO();
        dto.setAvailabilityId(nurseAvailability.getAvailabilityId());
        // Chuyển đổi Long thành Integer cho nurseProfileId
        dto.setNurseProfileId(nurseAvailability.getNurseProfile().getNurseProfileId().intValue());
        dto.setDayOfWeek(nurseAvailability.getDayOfWeek().name().toLowerCase());
        dto.setStartTime(Time.valueOf(nurseAvailability.getStartTime()));
        dto.setEndTime(Time.valueOf(nurseAvailability.getEndTime()));
        return dto;
    }

    private boolean isTimeOverlapping(NurseProfile nurseProfile, DayOfWeek dayOfWeek,
                                      LocalTime startTime, LocalTime endTime, Integer excludeId) {
        List<NurseAvailability> existingAvailabilities = nurseAvailabilityRepository
                .findByNurseProfileAndDayOfWeek(nurseProfile, dayOfWeek);

        for (NurseAvailability availability : existingAvailabilities) {
            if (excludeId != null && availability.getAvailabilityId().equals(excludeId)) {
                continue; // Bỏ qua bản ghi đang cập nhật
            }
            if (!(endTime.isBefore(availability.getStartTime()) || startTime.isAfter(availability.getEndTime()))) {
                return true; // Có chồng lấp
            }
        }
        return false;
    }
}