package edu.uth.nurseborn.services;

import edu.uth.nurseborn.dtos.BookingDTO;
import edu.uth.nurseborn.models.Booking;
import edu.uth.nurseborn.models.NurseAvailability;
import edu.uth.nurseborn.models.NurseProfile;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.models.enums.BookingStatus;
import edu.uth.nurseborn.models.enums.ServiceType;
import edu.uth.nurseborn.repositories.BookingRepository;
import edu.uth.nurseborn.repositories.NurseAvailabilityRepository;
import edu.uth.nurseborn.repositories.NurseProfileRepository;
import edu.uth.nurseborn.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NurseAvailabilityRepository nurseAvailabilityRepository;

    @Autowired
    private NurseProfileRepository nurseProfileRepository;

    // Ánh xạ các ngày từ tiếng Việt sang tiếng Anh
    private static final Map<String, String> DAY_OF_WEEK_MAPPING = new HashMap<>();

    static {
        DAY_OF_WEEK_MAPPING.put("Chủ Nhật", "SUNDAY");
        DAY_OF_WEEK_MAPPING.put("Thứ Hai", "MONDAY");
        DAY_OF_WEEK_MAPPING.put("Thứ Ba", "TUESDAY");
        DAY_OF_WEEK_MAPPING.put("Thứ Tư", "WEDNESDAY");
        DAY_OF_WEEK_MAPPING.put("Thứ Năm", "THURSDAY");
        DAY_OF_WEEK_MAPPING.put("Thứ Sáu", "FRIDAY");
        DAY_OF_WEEK_MAPPING.put("Thứ Bảy", "SATURDAY");
    }

    @Transactional
    public BookingDTO createBooking(BookingDTO bookingDTO, Long familyUserId) {
        logger.debug("Tạo đặt lịch mới cho familyUserId: {} và nurseUserId: {}", familyUserId, bookingDTO.getNurseUserId());

        // Kiểm tra dữ liệu đầu vào
        if (bookingDTO.getNurseUserId() == null) {
            throw new IllegalArgumentException("NurseUserId không được để trống");
        }
        if (bookingDTO.getBookingDate() == null) {
            throw new IllegalArgumentException("Ngày đặt lịch không được để trống");
        }
        if (bookingDTO.getServiceType() == null) {
            throw new IllegalArgumentException("Loại dịch vụ không được để trống");
        }
        if ("HOURLY".equals(bookingDTO.getServiceType())) {
            if (bookingDTO.getStartTime() == null || bookingDTO.getEndTime() == null) {
                throw new IllegalArgumentException("Giờ bắt đầu và giờ kết thúc không được để trống khi chọn dịch vụ theo giờ");
            }
            // Kiểm tra endTime phải lớn hơn startTime
            if (bookingDTO.getStartTime() != null && bookingDTO.getEndTime() != null &&
                    bookingDTO.getEndTime().isBefore(bookingDTO.getStartTime())) {
                throw new IllegalArgumentException("Giờ kết thúc phải lớn hơn giờ bắt đầu");
            }
        } else {
            // Nếu không phải HOURLY, đặt startTime và endTime thành null để tránh lưu giá trị không cần thiết
            bookingDTO.setStartTime(null);
            bookingDTO.setEndTime(null);
        }
        if (bookingDTO.getPrice() == null) {
            throw new IllegalArgumentException("Giá không được để trống");
        }

        // Lấy thông tin khách hàng
        User familyUser = userRepository.findById(familyUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với ID: " + familyUserId));

        // Lấy thông tin y tá
        User nurseUser = userRepository.findById(bookingDTO.getNurseUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy y tá với ID: " + bookingDTO.getNurseUserId()));

        // Kiểm tra vai trò
        if (!"FAMILY".equalsIgnoreCase(familyUser.getRole().name())) {
            logger.warn("User {} không phải FAMILY", familyUserId);
            throw new IllegalArgumentException("Người dùng phải có vai trò FAMILY");
        }
        if (!"NURSE".equalsIgnoreCase(nurseUser.getRole().name())) {
            logger.warn("User {} không phải NURSE", bookingDTO.getNurseUserId());
            throw new IllegalArgumentException("Người dùng phải có vai trò NURSE");
        }

        // Kiểm tra NurseProfile đã được phê duyệt
        NurseProfile nurseProfile = nurseProfileRepository.findByUserUserId(nurseUser.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy NurseProfile cho y tá với ID: " + nurseUser.getUserId()));
        if (!nurseProfile.getApproved()) {
            logger.warn("NurseProfile của y tá {} chưa được phê duyệt", nurseUser.getUsername());
            throw new IllegalArgumentException("Y tá chưa được phê duyệt");
        }

        // Kiểm tra lịch làm việc của y tá
        LocalDate bookingDate = bookingDTO.getBookingDate();
        logger.info("Booking Date: {}", bookingDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        String dayOfWeek = bookingDate.getDayOfWeek().toString();
        logger.info("Calculated Day of Week: {}", dayOfWeek);
        List<NurseAvailability> availabilities = nurseAvailabilityRepository.findByNurseProfileUserUserId(nurseUser.getUserId());
        List<String> availableDaysList = availabilities.stream()
                .map(availability -> {
                    String day = availability.getDayOfWeek().trim();
                    return DAY_OF_WEEK_MAPPING.getOrDefault(day, day).toUpperCase();
                })
                .collect(Collectors.toList());
        logger.info("Available Days: {}", availableDaysList);
        boolean isAvailable = availableDaysList.stream()
                .anyMatch(day -> day.equalsIgnoreCase(dayOfWeek));

        if (!isAvailable) {
            String availableDays = String.join(", ", availableDaysList);
            logger.warn("Y tá {} không làm việc vào ngày {}. Các ngày làm việc: {}", nurseUser.getUsername(), dayOfWeek, availableDays);
            throw new IllegalArgumentException("Y tá không làm việc vào ngày đã chọn. Các ngày làm việc: " + availableDays);
        }

        // Kiểm tra xung đột lịch
        List<Booking> existingBookings = bookingRepository.findByNurseUserUserIdAndBookingDateAndStatusNot(
                nurseUser.getUserId(), bookingDate, BookingStatus.CANCELLED);

        LocalTime startTime = bookingDTO.getStartTime();
        LocalTime endTime = bookingDTO.getEndTime();

        for (Booking existingBooking : existingBookings) {
            if ("DAILY".equals(bookingDTO.getServiceType()) || "WEEKLY".equals(bookingDTO.getServiceType())) {
                // Nếu đặt lịch DAILY hoặc WEEKLY, không cho phép có lịch khác trong ngày đó
                throw new IllegalArgumentException("Xung đột lịch cho y tá " + nurseUser.getUsername() + " vào ngày " + bookingDate);
            } else if ("HOURLY".equals(bookingDTO.getServiceType())) {
                // Nếu đặt lịch HOURLY, kiểm tra xem có lịch DAILY/WEEKLY nào không
                if ("DAILY".equals(existingBooking.getServiceType()) || "WEEKLY".equals(existingBooking.getServiceType())) {
                    throw new IllegalArgumentException("Xung đột lịch cho y tá " + nurseUser.getUsername() + " vào ngày " + bookingDate + " (đã có lịch cả ngày)");
                }
                // Kiểm tra xung đột thời gian với lịch HOURLY khác
                LocalTime existingStart = existingBooking.getStartTime();
                LocalTime existingEnd = existingBooking.getEndTime();

                if (startTime != null && endTime != null && existingStart != null && existingEnd != null) {
                    if (!(endTime.isBefore(existingStart) || startTime.isAfter(existingEnd))) {
                        logger.warn("Xung đột lịch cho y tá {} vào khung giờ này", nurseUser.getUsername());
                        throw new IllegalArgumentException("Xung đột lịch cho y tá " + nurseUser.getUsername() + " vào khung giờ này");
                    }
                }
            }
        }

        // Tự động lấy giá từ BookingDTO (đã được tính ở giao diện)
        Double price = bookingDTO.getPrice();
        ServiceType serviceType = ServiceType.valueOf(bookingDTO.getServiceType());

        // Tạo Booking
        Booking booking = new Booking();
        booking.setFamilyUser(familyUser);
        booking.setNurseUser(nurseUser);
        booking.setServiceType(serviceType);
        booking.setBookingDate(bookingDate);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setPrice(price);
        booking.setNotes(bookingDTO.getNotes());
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());

        // Lưu vào CSDL
        Booking savedBooking = bookingRepository.save(booking);
        logger.info("Đã lưu đặt lịch thành công vào CSDL với ID: {}", savedBooking.getBookingId());

        return bookingDTO;
    }

    // Lấy thông tin người dùng theo username
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với username: " + username));
    }

    // Lấy danh sách lịch đặt của y tá theo trạng thái
    public List<Booking> getBookingsByNurseUserIdAndStatus(Long nurseUserId, BookingStatus status) {
        List<Booking> bookings = bookingRepository.findByNurseUserUserIdAndStatus(nurseUserId, status);
        logger.debug("Lấy danh sách lịch với nurseUserId={} và status={}: {} lịch", nurseUserId, status, bookings.size());
        for (Booking booking : bookings) {
            logger.debug("Booking: bookingId={}, bookingDate={}, dayOfWeek={}, status={}",
                    booking.getBookingId(), booking.getBookingDate(), booking.getBookingDate().getDayOfWeek(), booking.getStatus());
        }
        return bookings;
    }

    // Lấy danh sách lịch đặt của y tá theo nurseUserId
    public List<Booking> findByNurseUserUserIdAndStatus(Long nurseUserId, BookingStatus status) {
        return bookingRepository.findByNurseUserUserIdAndStatus(nurseUserId, status);
    }

    // Chấp nhận lịch đặt
    @Transactional
    public void acceptBooking(Long bookingId, Long nurseUserId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch đặt với ID: " + bookingId));

        // Kiểm tra xem lịch đặt có thuộc về y tá này không
        if (!booking.getNurseUser().getUserId().equals(nurseUserId)) {
            throw new IllegalArgumentException("Lịch đặt không thuộc về y tá này");
        }

        // Kiểm tra trạng thái hiện tại của lịch
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalArgumentException("Lịch đặt không ở trạng thái PENDING");
        }

        // Cập nhật trạng thái thành ACCEPTED
        booking.setStatus(BookingStatus.ACCEPTED);
        bookingRepository.save(booking);
        logger.info("Đã cập nhật trạng thái lịch đặt với ID: {} thành ACCEPTED", bookingId);
    }
}