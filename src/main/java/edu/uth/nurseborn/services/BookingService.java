package edu.uth.nurseborn.services;

import edu.uth.nurseborn.dtos.BookingDTO;
import edu.uth.nurseborn.exception.UserNotFoundException;
import edu.uth.nurseborn.models.Booking;
import edu.uth.nurseborn.models.NurseProfile;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.models.NurseService;
import edu.uth.nurseborn.repositories.BookingRepository;
import edu.uth.nurseborn.repositories.NurseProfileRepository;
import edu.uth.nurseborn.repositories.ServiceRepository;
import edu.uth.nurseborn.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NurseProfileRepository nurseProfileRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public BookingDTO createBooking(BookingDTO bookingDTO) {
        logger.debug("Tạo Booking với dữ liệu: {}", bookingDTO);

        // Kiểm tra start_time < end_time
        if (bookingDTO.getStartTime() == null || bookingDTO.getEndTime() == null ||
                !bookingDTO.getStartTime().isBefore(bookingDTO.getEndTime())) {
            logger.warn("start_time phải nhỏ hơn end_time: start_time={}, end_time={}",
                    bookingDTO.getStartTime(), bookingDTO.getEndTime());
            throw new IllegalArgumentException("start_time phải nhỏ hơn end_time");
        }

        // Kiểm tra family user
        Optional<User> familyOptional = userRepository.findById(bookingDTO.getFamilyUserId()); // Sửa unboxing
        if (!familyOptional.isPresent()) {
            logger.warn("Không tìm thấy family user với ID: {}", bookingDTO.getFamilyUserId());
            throw new UserNotFoundException("Không tìm thấy family user với ID: " + bookingDTO.getFamilyUserId());
        }
        User family = familyOptional.get();
        if (!"family".equalsIgnoreCase(family.getRole().name())) {
            logger.warn("User với ID: {} không có role 'family'", bookingDTO.getFamilyUserId());
            throw new IllegalArgumentException("User phải có role 'family' để tạo booking");
        }

        // Kiểm tra nurse user
        Optional<User> nurseOptional = userRepository.findById(bookingDTO.getNurseUserId()); // Sửa unboxing
        if (!nurseOptional.isPresent()) {
            logger.warn("Không tìm thấy nurse user với ID: {}", bookingDTO.getNurseUserId());
            throw new UserNotFoundException("Không tìm thấy nurse user với ID: " + bookingDTO.getNurseUserId());
        }
        User nurse = nurseOptional.get();
        if (!"nurse".equalsIgnoreCase(nurse.getRole().name())) {
            logger.warn("User với ID: {} không có role 'nurse'", bookingDTO.getNurseUserId());
            throw new IllegalArgumentException("User phải có role 'nurse' để tạo booking");
        }

        // Kiểm tra nurse có được phê duyệt không
        Optional<NurseProfile> nurseProfileOptional = nurseProfileRepository.findByUserUserId(nurse.getUserId());
        if (!nurseProfileOptional.isPresent() || !nurseProfileOptional.get().getApproved()) {
            logger.warn("Nurse với ID: {} chưa được phê duyệt", nurse.getUserId());
            throw new IllegalArgumentException("Nurse phải được phê duyệt trước khi nhận booking");
        }

        // Kiểm tra service
        Optional<NurseService> serviceOptional = serviceRepository.findById(bookingDTO.getServiceId());
        if (!serviceOptional.isPresent()) {
            logger.warn("Không tìm thấy service với ID: {}", bookingDTO.getServiceId());
            throw new IllegalArgumentException("Không tìm thấy service với ID: " + bookingDTO.getServiceId());
        }
        NurseService service = serviceOptional.get();

        try {
            Booking booking = modelMapper.map(bookingDTO, Booking.class);
            booking.setFamily(family);
            booking.setNurse(nurse);
            booking.setService(service);

            logger.debug("Booking entity trước khi lưu: {}", booking);
            Booking savedBooking = bookingRepository.save(booking);
            bookingRepository.flush();
            logger.info("Đã lưu Booking thành công với ID: {}", savedBooking.getBookingId());

            return modelMapper.map(savedBooking, BookingDTO.class);
        } catch (Exception ex) {
            logger.error("Lỗi khi lưu Booking: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi lưu Booking: " + ex.getMessage());
        }
    }

    public BookingDTO getBookingById(Integer bookingId) {
        logger.debug("Lấy Booking với ID: {}", bookingId);

        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (!bookingOptional.isPresent()) {
            logger.warn("Không tìm thấy Booking với ID: {}", bookingId);
            throw new RuntimeException("Không tìm thấy Booking với ID: " + bookingId);
        }

        Booking booking = bookingOptional.get();
        return modelMapper.map(booking, BookingDTO.class);
    }

    @Transactional
    public BookingDTO updateBookingStatus(Integer bookingId, String status) {
        logger.debug("Cập nhật trạng thái Booking với ID: {} thành {}", bookingId, status);

        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (!bookingOptional.isPresent()) {
            logger.warn("Không tìm thấy Booking với ID: {}", bookingId);
            throw new RuntimeException("Không tìm thấy Booking với ID: " + bookingId);
        }

        Booking booking = bookingOptional.get();
        try {
            booking.setStatus(edu.uth.nurseborn.models.enums.BookingStatus.valueOf(status.toUpperCase()));
            logger.debug("Booking entity trước khi cập nhật: {}", booking);
            Booking updatedBooking = bookingRepository.save(booking);
            bookingRepository.flush();
            logger.info("Đã cập nhật trạng thái Booking thành công với ID: {}", bookingId);

            return modelMapper.map(updatedBooking, BookingDTO.class);
        } catch (Exception ex) {
            logger.error("Lỗi khi cập nhật trạng thái Booking: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi cập nhật trạng thái Booking: " + ex.getMessage());
        }
    }

    @Transactional
    public void deleteBooking(Integer bookingId) {
        logger.debug("Xóa Booking với ID: {}", bookingId);

        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (!bookingOptional.isPresent()) {
            logger.warn("Không tìm thấy Booking với ID: {}", bookingId);
            throw new RuntimeException("Không tìm thấy Booking với ID: " + bookingId);
        }

        try {
            bookingRepository.delete(bookingOptional.get());
            bookingRepository.flush();
            logger.info("Đã xóa Booking thành công với ID: {}", bookingId);
        } catch (Exception ex) {
            logger.error("Lỗi khi xóa Booking: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi xóa Booking: " + ex.getMessage());
        }
    }

    public List<BookingDTO> getAllBookings() {
        logger.debug("Lấy tất cả Bookings");

        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream()
                .map(booking -> modelMapper.map(booking, BookingDTO.class))
                .collect(Collectors.toList());
    }

    public List<BookingDTO> getBookingsByFamilyUserId(Integer familyUserId) {
        logger.debug("Lấy Bookings theo family user ID: {}", familyUserId);

        // Lấy đối tượng User từ familyUserId
        Optional<User> familyOptional = userRepository.findById(familyUserId.longValue());
        if (!familyOptional.isPresent()) {
            logger.warn("Không tìm thấy family user với ID: {}", familyUserId);
            throw new UserNotFoundException("Không tìm thấy family user với ID: " + familyUserId);
        }
        User family = familyOptional.get();

        // Sử dụng findByFamily
        List<Booking> bookings = bookingRepository.findByFamily(family);
        return bookings.stream()
                .map(booking -> modelMapper.map(booking, BookingDTO.class))
                .collect(Collectors.toList());
    }

    public List<BookingDTO> getBookingsByNurseUserId(Integer nurseUserId) {
        logger.debug("Lấy Bookings theo nurse user ID: {}", nurseUserId);

        // Lấy đối tượng User từ nurseUserId
        Optional<User> nurseOptional = userRepository.findById(nurseUserId.longValue());
        if (!nurseOptional.isPresent()) {
            logger.warn("Không tìm thấy nurse user với ID: {}", nurseUserId);
            throw new UserNotFoundException("Không tìm thấy nurse user với ID: " + nurseUserId);
        }
        User nurse = nurseOptional.get();

        // Sử dụng findByNurse
        List<Booking> bookings = bookingRepository.findByNurse(nurse);
        return bookings.stream()
                .map(booking -> modelMapper.map(booking, BookingDTO.class))
                .collect(Collectors.toList());
    }

    // Thêm phương thức countBookings
    public long countBookings() {
        logger.debug("Đếm tổng số Bookings");
        return bookingRepository.count();
    }
}