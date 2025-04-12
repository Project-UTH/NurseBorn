package edu.uth.nurseborn.services;

import edu.uth.nurseborn.dtos.NurseAvailabilityDTO;
import edu.uth.nurseborn.models.Booking;
import edu.uth.nurseborn.models.NurseAvailability;
import edu.uth.nurseborn.models.NurseProfile;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.models.enums.DayOfWeek;
import edu.uth.nurseborn.models.enums.Role;
import edu.uth.nurseborn.repositories.BookingRepository;
import edu.uth.nurseborn.repositories.NurseAvailabilityRepository;
import edu.uth.nurseborn.repositories.NurseProfileRepository;
import edu.uth.nurseborn.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NurseAvailabilityService {

    private static final Logger logger = LoggerFactory.getLogger(NurseAvailabilityService.class);

    @Autowired
    private NurseAvailabilityRepository nurseAvailabilityRepository;

    @Autowired
    private NurseProfileRepository nurseProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * Tạo một lịch trống mới cho y tá.
     *
     * @param nurseAvailabilityDTO DTO chứa thông tin lịch trống
     * @return NurseAvailabilityDTO đã được tạo
     */
    @Transactional
    public NurseAvailabilityDTO createNurseAvailability(NurseAvailabilityDTO nurseAvailabilityDTO) {
        logger.debug("Tạo lịch trống mới cho nurseProfileId: {}", nurseAvailabilityDTO.getNurseProfileId());

        // Kiểm tra nurse profile tồn tại
        Optional<NurseProfile> nurseProfileOptional = nurseProfileRepository.findById(
                Long.valueOf(nurseAvailabilityDTO.getNurseProfileId()));
        if (!nurseProfileOptional.isPresent()) {
            logger.warn("Không tìm thấy NurseProfile với ID: {}", nurseAvailabilityDTO.getNurseProfileId());
            throw new RuntimeException("Không tìm thấy NurseProfile với ID: " + nurseAvailabilityDTO.getNurseProfileId());
        }
        NurseProfile nurseProfile = nurseProfileOptional.get();

        // Kiểm tra role của user
        User nurse = nurseProfile.getUser();
        if (nurse.getRole() != Role.NURSE) {
            logger.warn("User với userId: {} không có role 'NURSE', role hiện tại: {}",
                    nurse.getUserId(), nurse.getRole());
            throw new IllegalArgumentException("User phải có role 'NURSE' để tạo lịch trống");
        }

        // Kiểm tra nurse profile đã được phê duyệt
        if (!nurseProfile.getApproved()) {
            logger.warn("NurseProfile với ID: {} chưa được phê duyệt", nurseProfile.getNurseProfileId());
            throw new IllegalStateException("NurseProfile chưa được phê duyệt để tạo lịch trống");
        }

        // Validate dayOfWeek
        DayOfWeek dayOfWeek;
        try {
            dayOfWeek = DayOfWeek.valueOf(nurseAvailabilityDTO.getDayOfWeek().toUpperCase());
        } catch (IllegalArgumentException ex) {
            logger.warn("Ngày không hợp lệ: {}", nurseAvailabilityDTO.getDayOfWeek());
            throw new IllegalArgumentException("Ngày không hợp lệ: " + nurseAvailabilityDTO.getDayOfWeek());
        }

        // Kiểm tra thời gian hợp lệ
        LocalTime startTime = nurseAvailabilityDTO.getStartTime().toLocalTime();
        LocalTime endTime = nurseAvailabilityDTO.getEndTime().toLocalTime();
        if (!startTime.isBefore(endTime)) {
            logger.warn("startTime phải nhỏ hơn endTime: startTime={}, endTime={}", startTime, endTime);
            throw new IllegalArgumentException("startTime phải nhỏ hơn endTime");
        }

        // Kiểm tra xung đột lịch trống
        List<NurseAvailability> conflictingAvailabilities = nurseAvailabilityRepository
                .findByNurseProfileIdAndDayOfWeekAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                        nurseAvailabilityDTO.getNurseProfileId(), dayOfWeek, endTime, startTime);
        if (!conflictingAvailabilities.isEmpty()) {
            logger.warn("Xung đột lịch trống cho nurseProfileId: {}, dayOfWeek: {}",
                    nurseAvailabilityDTO.getNurseProfileId(), dayOfWeek);
            throw new IllegalStateException("Lịch trống bị xung đột với các slot hiện có");
        }

        // Kiểm tra xung đột với booking
        LocalDateTime startDateTime = LocalDateTime.of(LocalDate.now(), startTime);
        LocalDateTime endDateTime = LocalDateTime.of(LocalDate.now(), endTime);
        List<Booking> conflictingBookings = bookingRepository.findByNurseAndStartTimeBetween(
                nurse, startDateTime, endDateTime);
        conflictingBookings.addAll(bookingRepository.findByNurseAndStartTimeBetween(
                nurse, endDateTime, startDateTime));
        if (!conflictingBookings.isEmpty()) {
            logger.warn("Xung đột với booking hiện có cho nurseProfileId: {}, dayOfWeek: {}",
                    nurseAvailabilityDTO.getNurseProfileId(), dayOfWeek);
            throw new IllegalStateException("Lịch trống xung đột với booking hiện có");
        }

        try {
            // Ánh xạ DTO sang entity
            NurseAvailability nurseAvailability = new NurseAvailability();
            nurseAvailability.setNurseProfile(nurseProfile);
            nurseAvailability.setDayOfWeek(dayOfWeek);
            nurseAvailability.setStartTime(startTime);
            nurseAvailability.setEndTime(endTime);

            logger.debug("NurseAvailability entity trước khi lưu: {}", nurseAvailability);
            NurseAvailability savedAvailability = nurseAvailabilityRepository.save(nurseAvailability);
            nurseAvailabilityRepository.flush();
            logger.info("Đã lưu lịch trống thành công cho nurseProfileId: {}",
                    savedAvailability.getNurseProfile().getNurseProfileId());

            // Ánh xạ entity trở lại DTO
            return modelMapper.map(savedAvailability, NurseAvailabilityDTO.class);
        } catch (Exception ex) {
            logger.error("Lỗi khi lưu lịch trống: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi lưu lịch trống: " + ex.getMessage());
        }
    }

    /**
     * Lấy danh sách lịch trống theo nurseProfileId.
     *
     * @param nurseProfileId ID của NurseProfile
     * @return Danh sách NurseAvailabilityDTO
     */
    public List<NurseAvailabilityDTO> getAvailabilitiesByNurseProfileId(Integer nurseProfileId) {
        logger.debug("Lấy danh sách lịch trống cho nurseProfileId: {}", nurseProfileId);

        // Kiểm tra nurse profile tồn tại
        Optional<NurseProfile> nurseProfileOptional = nurseProfileRepository.findById(
                Long.valueOf(nurseProfileId));
        if (!nurseProfileOptional.isPresent()) {
            logger.warn("Không tìm thấy NurseProfile với ID: {}", nurseProfileId);
            throw new RuntimeException("Không tìm thấy NurseProfile với ID: " + nurseProfileId);
        }

        List<NurseAvailability> availabilities = nurseAvailabilityRepository.findByNurseProfileId(nurseProfileId);
        return availabilities.stream()
                .map(availability -> modelMapper.map(availability, NurseAvailabilityDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Lấy lịch trống theo availabilityId.
     *
     * @param availabilityId ID của lịch trống
     * @return NurseAvailabilityDTO
     */
    public NurseAvailabilityDTO getNurseAvailabilityById(Integer availabilityId) {
        logger.debug("Lấy lịch trống với availabilityId: {}", availabilityId);

        Optional<NurseAvailability> availabilityOptional = nurseAvailabilityRepository.findById(availabilityId);
        if (!availabilityOptional.isPresent()) {
            logger.warn("Không tìm thấy lịch trống với ID: {}", availabilityId);
            throw new RuntimeException("Không tìm thấy lịch trống với ID: " + availabilityId);
        }

        return modelMapper.map(availabilityOptional.get(), NurseAvailabilityDTO.class);
    }

    /**
     * Cập nhật thông tin lịch trống.
     *
     * @param availabilityId ID của lịch trống
     * @param nurseAvailabilityDTO DTO chứa thông tin cập nhật
     * @return NurseAvailabilityDTO đã được cập nhật
     */
    @Transactional
    public NurseAvailabilityDTO updateNurseAvailability(Integer availabilityId,
                                                        NurseAvailabilityDTO nurseAvailabilityDTO) {
        logger.debug("Cập nhật lịch trống với availabilityId: {}", availabilityId);

        Optional<NurseAvailability> availabilityOptional = nurseAvailabilityRepository.findById(availabilityId);
        if (!availabilityOptional.isPresent()) {
            logger.warn("Không tìm thấy lịch trống với ID: {}", availabilityId);
            throw new RuntimeException("Không tìm thấy lịch trống với ID: " + availabilityId);
        }
        NurseAvailability nurseAvailability = availabilityOptional.get();

        // Kiểm tra role của user
        User nurse = nurseAvailability.getNurseProfile().getUser();
        if (nurse.getRole() != Role.NURSE) {
            logger.warn("User với userId: {} không có role 'NURSE', role hiện tại: {}",
                    nurse.getUserId(), nurse.getRole());
            throw new IllegalArgumentException("User phải có role 'NURSE' để cập nhật lịch trống");
        }

        // Kiểm tra nurse profile đã được phê duyệt
        NurseProfile nurseProfile = nurseAvailability.getNurseProfile();
        if (!nurseProfile.getApproved()) {
            logger.warn("NurseProfile với ID: {} chưa được phê duyệt", nurseProfile.getNurseProfileId());
            throw new IllegalStateException("NurseProfile chưa được phê duyệt để cập nhật lịch trống");
        }

        // Validate dayOfWeek nếu được cung cấp
        DayOfWeek dayOfWeek = nurseAvailability.getDayOfWeek();
        if (nurseAvailabilityDTO.getDayOfWeek() != null) {
            try {
                dayOfWeek = DayOfWeek.valueOf(nurseAvailabilityDTO.getDayOfWeek().toUpperCase());
            } catch (IllegalArgumentException ex) {
                logger.warn("Ngày không hợp lệ: {}", nurseAvailabilityDTO.getDayOfWeek());
                throw new IllegalArgumentException("Ngày không hợp lệ: " + nurseAvailabilityDTO.getDayOfWeek());
            }
        }

        // Validate thời gian nếu được cung cấp
        LocalTime startTime = nurseAvailability.getStartTime();
        LocalTime endTime = nurseAvailability.getEndTime();
        if (nurseAvailabilityDTO.getStartTime() != null) {
            startTime = nurseAvailabilityDTO.getStartTime().toLocalTime();
        }
        if (nurseAvailabilityDTO.getEndTime() != null) {
            endTime = nurseAvailabilityDTO.getEndTime().toLocalTime();
        }
        if (startTime != null && endTime != null && !startTime.isBefore(endTime)) {
            logger.warn("startTime phải nhỏ hơn endTime: startTime={}, endTime={}", startTime, endTime);
            throw new IllegalArgumentException("startTime phải nhỏ hơn endTime");
        }

        // Kiểm tra xung đột lịch trống (loại trừ chính bản ghi đang cập nhật)
        List<NurseAvailability> conflictingAvailabilities = nurseAvailabilityRepository
                .findByNurseProfileIdAndDayOfWeekAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                        nurseAvailability.getNurseProfile().getNurseProfileId(), dayOfWeek, endTime, startTime);
        conflictingAvailabilities.removeIf(a -> a.getAvailabilityId().equals(availabilityId));
        if (!conflictingAvailabilities.isEmpty()) {
            logger.warn("Xung đột lịch trống cho nurseProfileId: {}, dayOfWeek: {}",
                    nurseAvailability.getNurseProfile().getNurseProfileId(), dayOfWeek);
            throw new IllegalStateException("Lịch trống bị xung đột với các slot hiện có");
        }

        // Kiểm tra xung đột với booking
        LocalDateTime startDateTime = LocalDateTime.of(LocalDate.now(), startTime);
        LocalDateTime endDateTime = LocalDateTime.of(LocalDate.now(), endTime);
        List<Booking> conflictingBookings = bookingRepository.findByNurseAndStartTimeBetween(
                nurse, startDateTime, endDateTime);
        conflictingBookings.addAll(bookingRepository.findByNurseAndStartTimeBetween(
                nurse, endDateTime, startDateTime));
        if (!conflictingBookings.isEmpty()) {
            logger.warn("Xung đột với booking hiện có cho nurseProfileId: {}, dayOfWeek: {}",
                    nurseAvailability.getNurseProfile().getNurseProfileId(), dayOfWeek);
            throw new IllegalStateException("Lịch trống xung đột với booking hiện có");
        }

        try {
            // Cập nhật các trường
            nurseAvailability.setDayOfWeek(dayOfWeek);
            nurseAvailability.setStartTime(startTime);
            nurseAvailability.setEndTime(endTime);

            logger.debug("NurseAvailability entity trước khi cập nhật: {}", nurseAvailability);
            NurseAvailability updatedAvailability = nurseAvailabilityRepository.save(nurseAvailability);
            nurseAvailabilityRepository.flush();
            logger.info("Đã cập nhật lịch trống thành công cho availabilityId: {}", availabilityId);

            return modelMapper.map(updatedAvailability, NurseAvailabilityDTO.class);
        } catch (Exception ex) {
            logger.error("Lỗi khi cập nhật lịch trống: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi cập nhật lịch trống: " + ex.getMessage());
        }
    }

    /**
     * Xóa lịch trống theo availabilityId.
     *
     * @param availabilityId ID của lịch trống
     */
    @Transactional
    public void deleteNurseAvailability(Integer availabilityId) {
        logger.debug("Xóa lịch trống với availabilityId: {}", availabilityId);

        Optional<NurseAvailability> availabilityOptional = nurseAvailabilityRepository.findById(availabilityId);
        if (!availabilityOptional.isPresent()) {
            logger.warn("Không tìm thấy lịch trống với ID: {}", availabilityId);
            throw new RuntimeException("Không tìm thấy lịch trống với ID: " + availabilityId);
        }

        // Kiểm tra role của user
        User nurse = availabilityOptional.get().getNurseProfile().getUser();
        if (nurse.getRole() != Role.NURSE) {
            logger.warn("User với userId: {} không có role 'NURSE', role hiện tại: {}",
                    nurse.getUserId(), nurse.getRole());
            throw new IllegalArgumentException("User phải có role 'NURSE' để xóa lịch trống");
        }

        try {
            nurseAvailabilityRepository.delete(availabilityOptional.get());
            nurseAvailabilityRepository.flush();
            logger.info("Đã xóa lịch trống thành công cho availabilityId: {}", availabilityId);
        } catch (Exception ex) {
            logger.error("Lỗi khi xóa lịch trống: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi xóa lịch trống: " + ex.getMessage());
        }
    }
}