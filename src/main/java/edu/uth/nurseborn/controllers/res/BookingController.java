package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.dtos.BookingDTO;
import edu.uth.nurseborn.services.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

    // Tạo một booking mới
    @PostMapping
    public ResponseEntity<BookingDTO> createBooking(@RequestBody BookingDTO bookingDTO) {
        logger.debug("Yêu cầu tạo Booking với dữ liệu: {}", bookingDTO);
        BookingDTO createdBooking = bookingService.createBooking(bookingDTO);
        return ResponseEntity.ok(createdBooking);
    }

    // Cập nhật trạng thái của booking
    @PutMapping("/{bookingId}/status")
    public ResponseEntity<BookingDTO> updateBookingStatus(@PathVariable Integer bookingId, @RequestBody String status) {
        logger.debug("Yêu cầu cập nhật trạng thái Booking với bookingId: {} thành {}", bookingId, status);
        BookingDTO updatedBooking = bookingService.updateBookingStatus(bookingId, status);
        return ResponseEntity.ok(updatedBooking);
    }

    // Lấy thông tin booking theo bookingId
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable Integer bookingId) {
        logger.debug("Yêu cầu lấy Booking với bookingId: {}", bookingId);
        BookingDTO bookingDTO = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(bookingDTO);
    }

    // Lấy tất cả bookings
    @GetMapping
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        logger.debug("Yêu cầu lấy tất cả Bookings");
        List<BookingDTO> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    // Lấy bookings theo familyUserId
    @GetMapping("/family/{familyUserId}")
    public ResponseEntity<List<BookingDTO>> getBookingsByFamilyUserId(@PathVariable Integer familyUserId) {
        logger.debug("Yêu cầu lấy Bookings theo familyUserId: {}", familyUserId);
        List<BookingDTO> bookings = bookingService.getBookingsByFamilyUserId(familyUserId);
        return ResponseEntity.ok(bookings);
    }

    // Lấy bookings theo nurseUserId
    @GetMapping("/nurse/{nurseUserId}")
    public ResponseEntity<List<BookingDTO>> getBookingsByNurseUserId(@PathVariable Integer nurseUserId) {
        logger.debug("Yêu cầu lấy Bookings theo nurseUserId: {}", nurseUserId);
        List<BookingDTO> bookings = bookingService.getBookingsByNurseUserId(nurseUserId);
        return ResponseEntity.ok(bookings);
    }

    // Xóa booking
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Integer bookingId) {
        logger.debug("Yêu cầu xóa Booking với bookingId: {}", bookingId);
        bookingService.deleteBooking(bookingId);
        return ResponseEntity.ok().build();
    }
}