package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.dtos.ServiceDTO;
import edu.uth.nurseborn.models.enums.ServiceStatus;
import edu.uth.nurseborn.services.NurseServiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nurse-service")
public class NurseServiceController {

    private static final Logger logger = LoggerFactory.getLogger(NurseServiceController.class);

    @Autowired
    private NurseServiceService nurseServiceService;

    // Endpoint tạo dịch vụ mới cho nurse
    @PostMapping("")
    public ResponseEntity<ServiceDTO> createService(@RequestParam String nurseUsername, @RequestBody ServiceDTO serviceDTO) {
        logger.debug("Yêu cầu tạo dịch vụ cho nurse: {}", nurseUsername);
        ServiceDTO createdService = nurseServiceService.createService(nurseUsername, serviceDTO);
        return ResponseEntity.ok(createdService);
    }

    // Endpoint cập nhật dịch vụ
    @PutMapping("/{serviceId}")
    public ResponseEntity<ServiceDTO> updateService(@PathVariable Integer serviceId, @RequestBody ServiceDTO serviceDTO) {
        logger.debug("Yêu cầu cập nhật dịch vụ với serviceId: {}", serviceId);
        ServiceDTO updatedService = nurseServiceService.updateService(serviceId, serviceDTO);
        return ResponseEntity.ok(updatedService);
    }

    // Endpoint lấy thông tin dịch vụ theo ID
    @GetMapping("/{serviceId}")
    public ResponseEntity<ServiceDTO> getServiceById(@PathVariable Integer serviceId) {
        logger.debug("Yêu cầu lấy dịch vụ với serviceId: {}", serviceId);
        ServiceDTO service = nurseServiceService.getServiceById(serviceId);
        return ResponseEntity.ok(service);
    }

    // Endpoint lấy danh sách dịch vụ của nurse
    @GetMapping("/nurse")
    public ResponseEntity<List<ServiceDTO>> getServicesByNurse(@RequestParam String nurseUsername) {
        logger.debug("Yêu cầu lấy danh sách dịch vụ của nurse: {}", nurseUsername);
        List<ServiceDTO> services = nurseServiceService.getServicesByNurse(nurseUsername);
        return ResponseEntity.ok(services);
    }

    // Endpoint lấy danh sách dịch vụ active của nurse
    @GetMapping("/nurse/active")
    public ResponseEntity<List<ServiceDTO>> getActiveServicesByNurse(@RequestParam String nurseUsername) {
        logger.debug("Yêu cầu lấy danh sách dịch vụ active của nurse: {}", nurseUsername);
        List<ServiceDTO> services = nurseServiceService.getActiveServicesByNurse(nurseUsername);
        return ResponseEntity.ok(services);
    }

    // Endpoint tìm kiếm dịch vụ theo tên
    @GetMapping("/search")
    public ResponseEntity<List<ServiceDTO>> searchServicesByName(@RequestParam String serviceName) {
        logger.debug("Yêu cầu tìm kiếm dịch vụ với tên: {}", serviceName);
        List<ServiceDTO> services = nurseServiceService.searchServicesByName(serviceName);
        return ResponseEntity.ok(services);
    }

    // Endpoint xóa dịch vụ
    @DeleteMapping("/{serviceId}")
    public ResponseEntity<String> deleteService(@PathVariable Integer serviceId) {
        logger.debug("Yêu cầu xóa dịch vụ với serviceId: {}", serviceId);
        nurseServiceService.deleteService(serviceId);
        return ResponseEntity.ok("Xóa dịch vụ thành công");
    }

    // Endpoint cập nhật trạng thái dịch vụ
    @PutMapping("/{serviceId}/status")
    public ResponseEntity<String> updateServiceStatus(@PathVariable Integer serviceId, @RequestParam ServiceStatus status) {
        logger.debug("Yêu cầu cập nhật trạng thái dịch vụ với serviceId: {}", serviceId);
        nurseServiceService.updateServiceStatus(serviceId, status);
        return ResponseEntity.ok("Cập nhật trạng thái dịch vụ thành công");
    }
}