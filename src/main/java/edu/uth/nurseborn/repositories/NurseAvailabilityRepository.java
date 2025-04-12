package edu.uth.nurseborn.repositories;

import edu.uth.nurseborn.models.NurseAvailability;
import edu.uth.nurseborn.models.enums.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

public interface NurseAvailabilityRepository extends JpaRepository<NurseAvailability, Integer> {
    // Tìm tất cả lịch trống theo nurse_profile_id
    @Query("SELECT na FROM NurseAvailability na WHERE na.nurseProfile.nurseProfileId = :nurseProfileId")
    List<NurseAvailability> findByNurseProfileId(@Param("nurseProfileId") Integer nurseProfileId);

    // Tìm lịch trống theo nurse_profile_id và ngày trong tuần
    @Query("SELECT na FROM NurseAvailability na WHERE na.nurseProfile.nurseProfileId = :nurseProfileId AND na.dayOfWeek = :dayOfWeek")
    List<NurseAvailability> findByNurseProfileIdAndDayOfWeek(@Param("nurseProfileId") Integer nurseProfileId, @Param("dayOfWeek") DayOfWeek dayOfWeek);

    // Tìm lịch trống theo nurse_profile_id, ngày trong tuần và khoảng thời gian giao nhau
    @Query("SELECT na FROM NurseAvailability na WHERE na.nurseProfile.nurseProfileId = :nurseProfileId " +
            "AND na.dayOfWeek = :dayOfWeek AND na.startTime <= :startTime AND na.endTime >= :endTime")
    List<NurseAvailability> findByNurseProfileIdAndDayOfWeekAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
            @Param("nurseProfileId") Integer nurseProfileId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);
}