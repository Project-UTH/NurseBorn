package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.dtos.ReportDTO;
import edu.uth.nurseborn.services.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private ReportService reportService;

    // Tạo mới một báo cáo
    @PostMapping
    public ResponseEntity<ReportDTO> createReport(@RequestBody ReportDTO baoCaoDTO) {
        logger.info("Nhận yêu cầu tạo báo cáo: {}", baoCaoDTO);
        ReportDTO createdReport = reportService.createReport(baoCaoDTO);
        logger.info("Đã tạo báo cáo thành công với ID: {}", createdReport.getReportId());
        return new ResponseEntity<>(createdReport, HttpStatus.CREATED);
    }

    // Lấy báo cáo theo reportId
    @GetMapping("/{reportId}")
    public ResponseEntity<ReportDTO> getReportById(@PathVariable Integer reportId) {
        logger.info("Nhận yêu cầu lấy báo cáo với ID: {}", reportId);
        ReportDTO report = reportService.getReportById(reportId);
        logger.info("Đã lấy báo cáo thành công với ID: {}", reportId);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    // Lấy tất cả báo cáo theo adminUserId
    @GetMapping("/admin/{adminUserId}")
    public ResponseEntity<List<ReportDTO>> getReportsByAdmin(@PathVariable Long adminUserId) {
        logger.info("Nhận yêu cầu lấy tất cả báo cáo cho admin ID: {}", adminUserId);
        List<ReportDTO> reports = reportService.getReportsByAdmin(adminUserId);
        logger.info("Đã lấy {} báo cáo cho admin ID: {}", reports.size(), adminUserId);
        return new ResponseEntity<>(reports, HttpStatus.OK);
    }

    // Lấy tất cả báo cáo theo reportType
    @GetMapping("/type/{reportType}")
    public ResponseEntity<List<ReportDTO>> getReportsByType(@PathVariable String reportType) {
        logger.info("Nhận yêu cầu lấy tất cả báo cáo theo loại: {}", reportType);
        List<ReportDTO> reports = reportService.getReportsByType(reportType);
        logger.info("Đã lấy {} báo cáo theo loại: {}", reports.size(), reportType);
        return new ResponseEntity<>(reports, HttpStatus.OK);
    }

    // Lấy tất cả báo cáo theo adminUserId và reportType
    @GetMapping("/admin/{adminUserId}/type/{reportType}")
    public ResponseEntity<List<ReportDTO>> getReportsByAdminAndType(
            @PathVariable Long adminUserId,
            @PathVariable String reportType) {
        logger.info("Nhận yêu cầu lấy tất cả báo cáo cho admin ID: {} và loại: {}", adminUserId, reportType);
        List<ReportDTO> reports = reportService.getReportsByAdminAndType(adminUserId, reportType);
        logger.info("Đã lấy {} báo cáo cho admin ID: {} và loại: {}", reports.size(), adminUserId, reportType);
        return new ResponseEntity<>(reports, HttpStatus.OK);
    }

    // Lấy tất cả báo cáo trong khoảng thời gian
    @GetMapping("/time-range")
    public ResponseEntity<List<ReportDTO>> getReportsByTimeRange(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        logger.info("Nhận yêu cầu lấy tất cả báo cáo trong khoảng thời gian từ {} đến {}", start, end);
        List<ReportDTO> reports = reportService.getReportsByTimeRange(start, end);
        logger.info("Đã lấy {} báo cáo trong khoảng thời gian từ {} đến {}", reports.size(), start, end);
        return new ResponseEntity<>(reports, HttpStatus.OK);
    }
}