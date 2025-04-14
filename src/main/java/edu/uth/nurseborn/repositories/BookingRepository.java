package edu.uth.nurseborn.repositories;

import edu.uth.nurseborn.models.Booking;
import edu.uth.nurseborn.models.FamilyProfile;
import edu.uth.nurseborn.models.NurseService;
import edu.uth.nurseborn.models.enums.BookingStatus;
import edu.uth.nurseborn.models.enums.ServiceType;
import edu.uth.nurseborn.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    // Tìm tất cả lịch đặt theo gia đình
    List<Booking> findByFamily(User family);

    // Tìm tất cả lịch đặt theo y tá
    List<Booking> findByNurse(User nurse);

    // Tìm tất cả lịch đặt theo dịch vụ
    List<Booking> findByNurseService(NurseService nurseService); // Sửa từ findByService thành findByNurseService

    // Tìm tất cả lịch đặt theo loại dịch vụ
    List<Booking> findByServiceType(ServiceType serviceType);

    // Tìm tất cả lịch đặt trong khoảng thời gian
    List<Booking> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    // Tìm tất cả lịch đặt của một y tá trong khoảng thời gian
    List<Booking> findByNurseAndStartTimeBetween(User nurse, LocalDateTime start, LocalDateTime end);

    // Thêm phương thức tìm Booking theo bookingId
    Optional<Booking> findByBookingId(Integer bookingId);

    List<Booking> findByNurseAndStatus(User nurse, BookingStatus status);
}