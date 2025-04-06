package edu.uth.nurseborn.services;

import edu.uth.nurseborn.dtos.ReportDTO;
import edu.uth.nurseborn.exception.ReportNotFoundException;
import edu.uth.nurseborn.models.Report;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.models.enums.ReportType;
import edu.uth.nurseborn.repositories.ReportRepository;
import edu.uth.nurseborn.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    // Tạo mới một báo cáo
    @Transactional
    public ReportDTO createReport(ReportDTO baoCaoDTO) {
        logger.debug("Bắt đầu transaction để tạo báo cáo: {}", baoCaoDTO);

        // Lấy User (admin) từ adminUserId
        User admin = userRepository.findById(baoCaoDTO.getAdminUserId())
                .orElseThrow(() -> {
                    logger.warn("Không tìm thấy admin với ID: {}", baoCaoDTO.getAdminUserId());
                    return new ReportNotFoundException("Admin không tồn tại với ID: " + baoCaoDTO.getAdminUserId());
                });

        // Kiểm tra reportType hợp lệ
        ReportType reportType;
        try {
            reportType = ReportType.valueOf(baoCaoDTO.getReportType().toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Loại báo cáo không hợp lệ: {}", baoCaoDTO.getReportType());
            throw new IllegalArgumentException("Loại báo cáo phải là SERVICE_DEMAND, USER_ACTIVITY, hoặc PLATFORM_PERFORMANCE");
        }

        try {
            Report baoCao = new Report();
            baoCao.setAdmin(admin);
            baoCao.setReportType(reportType);
            baoCao.setData(baoCaoDTO.getData());
            // generatedAt sẽ được set tự động bởi @PrePersist

            logger.debug("Báo cáo entity trước khi lưu: {}", baoCao);
            Report baoCaoDaLuu = reportRepository.save(baoCao);
            reportRepository.flush();
            logger.info("Đã lưu báo cáo thành công với ID: {}", baoCaoDaLuu.getReportId());

            return modelMapper.map(baoCaoDaLuu, ReportDTO.class);
        } catch (Exception e) {
            logger.error("Lỗi khi lưu báo cáo: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi tạo báo cáo: " + e.getMessage());
        }
    }

    // Lấy báo cáo theo reportId
    public ReportDTO getReportById(Integer reportId) {
        logger.debug("Tìm báo cáo với ID: {}", reportId);
        return reportRepository.findByReportId(reportId)
                .map(baoCao -> modelMapper.map(baoCao, ReportDTO.class))
                .orElseThrow(() -> {
                    logger.warn("Không tìm thấy báo cáo với ID: {}", reportId);
                    return new ReportNotFoundException("Không tìm thấy báo cáo với ID: " + reportId);
                });
    }

    // Lấy tất cả báo cáo theo adminUserId
    public List<ReportDTO> getReportsByAdmin(Long adminUserId) { // Thay Integer thành Long
        logger.debug("Tìm tất cả báo cáo cho admin ID: {}", adminUserId);
        List<Report> reports = reportRepository.findByAdminUserId(adminUserId);
        return reports.stream()
                .map(report -> modelMapper.map(report, ReportDTO.class))
                .collect(Collectors.toList());
    }

    // Lấy tất cả báo cáo theo reportType
    public List<ReportDTO> getReportsByType(String reportType) {
        logger.debug("Tìm tất cả báo cáo theo loại: {}", reportType);
        ReportType type;
        try {
            type = ReportType.valueOf(reportType.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Loại báo cáo không hợp lệ: {}", reportType);
            throw new IllegalArgumentException("Loại báo cáo phải là SERVICE_DEMAND, USER_ACTIVITY, hoặc PLATFORM_PERFORMANCE");
        }

        List<Report> reports = reportRepository.findByReportType(type);
        return reports.stream()
                .map(report -> modelMapper.map(report, ReportDTO.class))
                .collect(Collectors.toList());
    }

    // Lấy tất cả báo cáo theo adminUserId và reportType
    public List<ReportDTO> getReportsByAdminAndType(Long adminUserId, String reportType) { // Thay Integer thành Long
        logger.debug("Tìm tất cả báo cáo cho admin ID: {} và loại: {}", adminUserId, reportType);
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> {
                    logger.warn("Không tìm thấy admin với ID: {}", adminUserId);
                    return new ReportNotFoundException("Admin không tồn tại với ID: " + adminUserId);
                });

        ReportType type;
        try {
            type = ReportType.valueOf(reportType.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Loại báo cáo không hợp lệ: {}", reportType);
            throw new IllegalArgumentException("Loại báo cáo phải là SERVICE_DEMAND, USER_ACTIVITY, hoặc PLATFORM_PERFORMANCE");
        }

        List<Report> reports = reportRepository.findByAdminAndReportType(admin, type);
        return reports.stream()
                .map(report -> modelMapper.map(report, ReportDTO.class))
                .collect(Collectors.toList());
    }

    // Lấy tất cả báo cáo trong khoảng thời gian
    public List<ReportDTO> getReportsByTimeRange(LocalDateTime start, LocalDateTime end) {
        logger.debug("Tìm tất cả báo cáo trong khoảng thời gian từ {} đến {}", start, end);
        if (start.isAfter(end)) {
            logger.warn("Thời gian bắt đầu không thể sau thời gian kết thúc: start={}, end={}", start, end);
            throw new IllegalArgumentException("Thời gian bắt đầu phải trước thời gian kết thúc");
        }

        List<Report> reports = reportRepository.findByGeneratedAtBetween(start, end);
        return reports.stream()
                .map(report -> modelMapper.map(report, ReportDTO.class))
                .collect(Collectors.toList());
    }
}