package edu.uth.nurseborn.repositories;

import edu.uth.nurseborn.models.FamilyProfile;
import edu.uth.nurseborn.models.Report;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.models.enums.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Integer> {
    // Tìm tất cả báo cáo theo admin
    List<Report> findByAdmin(User admin);

    // Tìm tất cả báo cáo theo admin_user_id
    List<Report> findByAdminUserId(Long adminUserId);

    // Tìm tất cả báo cáo theo reportType
    List<Report> findByReportType(ReportType reportType);

    // Tìm tất cả báo cáo theo admin và reportType
    List<Report> findByAdminAndReportType(User admin, ReportType reportType);

    // Tìm tất cả báo cáo trong khoảng thời gian generatedAt
    List<Report> findByGeneratedAtBetween(LocalDateTime start, LocalDateTime end);

    // Tìm một báo cáo duy nhất theo reportId
    Optional<Report> findByReportId(Integer reportId);

}