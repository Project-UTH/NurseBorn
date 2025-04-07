package edu.uth.nurseborn.repositories;

import edu.uth.nurseborn.models.NurseAvailability;
import edu.uth.nurseborn.models.NurseProfile;
import edu.uth.nurseborn.models.enums.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface NurseAvailabilityRepository extends JpaRepository<NurseAvailability, Integer> {

    // Tìm tất cả lịch sẵn sàng của một nurse profile
    List<NurseAvailability> findByNurseProfile(NurseProfile nurseProfile);

    // Tìm lịch sẵn sàng theo nurse profile và ngày trong tuần
    List<NurseAvailability> findByNurseProfileAndDayOfWeek(NurseProfile nurseProfile, DayOfWeek dayOfWeek);

    // Tìm lịch sẵn sàng theo nurse profile, ngày trong tuần và thời gian bắt đầu
    NurseAvailability findByNurseProfileAndDayOfWeekAndStartTime(
            NurseProfile nurseProfile, DayOfWeek dayOfWeek, LocalTime startTime);

    // Tìm lịch sẵn sàng trong khoảng thời gian cho một nurse profile
    List<NurseAvailability> findByNurseProfileAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(
            NurseProfile nurseProfile, LocalTime startTime, LocalTime endTime);

    // Kiểm tra xem nurse profile đã có lịch sẵn sàng cho ngày cụ thể chưa
    boolean existsByNurseProfileAndDayOfWeek(NurseProfile nurseProfile, DayOfWeek dayOfWeek);

    // Đếm số lượng lịch sẵn sàng của một nurse profile
    Long countByNurseProfile(NurseProfile nurseProfile);

    // Xóa tất cả lịch sẵn sàng của một nurse profile
    void deleteByNurseProfile(NurseProfile nurseProfile);
}