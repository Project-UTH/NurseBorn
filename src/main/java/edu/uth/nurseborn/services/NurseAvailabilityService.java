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

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NurseAvailabilityService {

    private static final Logger logger = LoggerFactory.getLogger(NurseAvailabilityService.class);

    private final NurseAvailabilityRepository nurseAvailabilityRepository;
    private final NurseProfileRepository nurseProfileRepository;
    private final ModelMapper modelMapper;

    public NurseAvailabilityService(NurseAvailabilityRepository nurseAvailabilityRepository,
                                    NurseProfileRepository nurseProfileRepository,
                                    ModelMapper modelMapper) {
        this.nurseAvailabilityRepository = nurseAvailabilityRepository;
        this.nurseProfileRepository = nurseProfileRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public NurseAvailabilityDTO createNurseAvailability(NurseAvailabilityDTO nurseAvailabilityDTO) {
        logger.debug("Bắt đầu tạo lịch sẵn sàng: {}", nurseAvailabilityDTO);

        validateAvailabilityDTO(nurseAvailabilityDTO);

        NurseProfile nurseProfile = nurseProfileRepository.findById(nurseAvailabilityDTO.getNurseProfileId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nurse profile với ID: " + nurseAvailabilityDTO.getNurseProfileId()));

        DayOfWeek dayOfWeek = DayOfWeek.valueOf(nurseAvailabilityDTO.getDayOfWeek().toUpperCase());
        LocalTime startTime = LocalTime.parse(nurseAvailabilityDTO.getStartTime());
        LocalTime endTime = LocalTime.parse(nurseAvailabilityDTO.getEndTime());

        if (isTimeOverlapping(nurseProfile, dayOfWeek, startTime, endTime, null)) {
            throw new IllegalArgumentException("Thời gian này chồng lấp với lịch sẵn sàng khác trong " + dayOfWeek);
        }

        NurseAvailability nurseAvailability = modelMapper.map(nurseAvailabilityDTO, NurseAvailability.class);
        nurseAvailability.setNurseProfile(nurseProfile);
        nurseAvailability.setDayOfWeek(dayOfWeek);
        nurseAvailability.setStartTime(startTime);
        nurseAvailability.setEndTime(endTime);

        logger.debug("NurseAvailability entity trước khi lưu: {}", nurseAvailability);

        NurseAvailability savedAvailability = nurseAvailabilityRepository.save(nurseAvailability);
        nurseAvailabilityRepository.flush();
        logger.info("Đã tạo lịch sẵn sàng thành công với ID: {}", savedAvailability.getAvailabilityId());

        return modelMapper.map(savedAvailability, NurseAvailabilityDTO.class);
    }

    @Transactional
    public NurseAvailabilityDTO updateNurseAvailability(Integer availabilityId, NurseAvailabilityDTO nurseAvailabilityDTO) {
        logger.debug("Bắt đầu cập nhật lịch sẵn sàng với ID: {}", availabilityId);

        validateAvailabilityDTO(nurseAvailabilityDTO);

        NurseAvailability existingAvailability = nurseAvailabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch sẵn sàng với ID: " + availabilityId));

        NurseProfile nurseProfile = nurseProfileRepository.findById(nurseAvailabilityDTO.getNurseProfileId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nurse profile với ID: " + nurseAvailabilityDTO.getNurseProfileId()));

        DayOfWeek dayOfWeek = DayOfWeek.valueOf(nurseAvailabilityDTO.getDayOfWeek().toUpperCase());
        LocalTime startTime = LocalTime.parse(nurseAvailabilityDTO.getStartTime());
        LocalTime endTime = LocalTime.parse(nurseAvailabilityDTO.getEndTime());

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

        return modelMapper.map(updatedAvailability, NurseAvailabilityDTO.class);
    }

    public List<NurseAvailabilityDTO> getAvailabilitiesByNurseProfileId(Long nurseProfileId) {
        logger.debug("Tìm lịch sẵn sàng với nurseProfileId: {}", nurseProfileId);

        NurseProfile nurseProfile = nurseProfileRepository.findById(nurseProfileId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nurse profile với ID: " + nurseProfileId));

        List<NurseAvailability> availabilities = nurseAvailabilityRepository.findByNurseProfile(nurseProfile);
        return availabilities.stream()
                .map(availability -> modelMapper.map(availability, NurseAvailabilityDTO.class))
                .collect(Collectors.toList());
    }

    public NurseAvailabilityDTO getNurseAvailabilityById(Integer availabilityId) {
        logger.debug("Tìm lịch sẵn sàng với availabilityId: {}", availabilityId);

        NurseAvailability nurseAvailability = nurseAvailabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch sẵn sàng với ID: " + availabilityId));

        return modelMapper.map(nurseAvailability, NurseAvailabilityDTO.class);
    }

    @Transactional
    public void deleteNurseAvailability(Integer availabilityId) {
        logger.debug("Xóa lịch sẵn sàng với availabilityId: {}", availabilityId);

        NurseAvailability nurseAvailability = nurseAvailabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch sẵn sàng với ID: " + availabilityId));

        nurseAvailabilityRepository.delete(nurseAvailability);
        logger.info("Đã xóa lịch sẵn sàng với availabilityId: {}", availabilityId);
    }

    public List<NurseAvailabilityDTO> getAvailabilitiesByDayOfWeek(Long nurseProfileId, String dayOfWeek) {
        logger.debug("Tìm lịch sẵn sàng với nurseProfileId: {} và dayOfWeek: {}", nurseProfileId, dayOfWeek);

        NurseProfile nurseProfile = nurseProfileRepository.findById(nurseProfileId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nurse profile với ID: " + nurseProfileId));

        List<NurseAvailability> availabilities = nurseAvailabilityRepository.findByNurseProfileAndDayOfWeek(
                nurseProfile, DayOfWeek.valueOf(dayOfWeek.toUpperCase()));

        return availabilities.stream()
                .map(availability -> modelMapper.map(availability, NurseAvailabilityDTO.class))
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
            LocalTime startTime = LocalTime.parse(dto.getStartTime());
            LocalTime endTime = LocalTime.parse(dto.getEndTime());
            if (startTime.isAfter(endTime)) {
                throw new IllegalArgumentException("Giờ bắt đầu phải trước giờ kết thúc");
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("DayOfWeek hoặc định dạng thời gian không hợp lệ: " + e.getMessage());
        } catch (Exception e) {
            throw new IllegalArgumentException("Định dạng thời gian không hợp lệ: " + e.getMessage());
        }
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