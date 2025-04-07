package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.dtos.AdminActionDTO;
import edu.uth.nurseborn.services.AdminActionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin-actions")
public class AdminActionController {

    private static final Logger logger = LoggerFactory.getLogger(AdminActionController.class);

    private final AdminActionService adminActionService;

    public AdminActionController(AdminActionService adminActionService) {
        this.adminActionService = adminActionService;
    }

    // Tạo một hành động admin mới
    @PostMapping("/create")
    public ResponseEntity<AdminActionDTO> createAdminAction(@RequestBody AdminActionDTO adminActionDTO) {
        logger.debug("Yêu cầu tạo hành động admin từ adminUserId: {}", adminActionDTO.getAdminUserId());
        AdminActionDTO createdAction = adminActionService.createAdminAction(adminActionDTO);
        logger.info("Đã tạo hành động admin thành công với actionId: {}", createdAction.getActionId());
        return ResponseEntity.ok(createdAction);
    }

    // Lấy danh sách hành động admin theo adminUserId
    @GetMapping("/by-admin/{adminUserId}")
    public ResponseEntity<List<AdminActionDTO>> getActionsByAdminId(@PathVariable Integer adminUserId) {
        logger.debug("Yêu cầu lấy danh sách hành động admin cho adminUserId: {}", adminUserId);
        List<AdminActionDTO> actions = adminActionService.getActionsByAdminId(adminUserId);
        logger.info("Đã lấy thành công danh sách hành động admin cho adminUserId: {}", adminUserId);
        return ResponseEntity.ok(actions);
    }

    // Lấy chi tiết hành động admin theo actionId
    @GetMapping("/{actionId}")
    public ResponseEntity<AdminActionDTO> getAdminActionById(@PathVariable Integer actionId) {
        logger.debug("Yêu cầu lấy chi tiết hành động admin với actionId: {}", actionId);
        AdminActionDTO action = adminActionService.getAdminActionById(actionId);
        logger.info("Đã lấy thành công chi tiết hành động admin với actionId: {}", actionId);
        return ResponseEntity.ok(action);
    }

    // Xóa hành động admin theo actionId
    @DeleteMapping("/{actionId}")
    public ResponseEntity<Void> deleteAdminAction(@PathVariable Integer actionId) {
        logger.debug("Yêu cầu xóa hành động admin với actionId: {}", actionId);
        adminActionService.deleteAdminAction(actionId);
        logger.info("Đã xóa thành công hành động admin với actionId: {}", actionId);
        return ResponseEntity.noContent().build();
    }

    // Lấy danh sách hành động admin trong khoảng thời gian
    @GetMapping("/by-date-range")
    public ResponseEntity<List<AdminActionDTO>> getActionsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        logger.debug("Yêu cầu lấy danh sách hành động admin từ {} đến {}", startDate, endDate);
        List<AdminActionDTO> actions = adminActionService.getActionsByDateRange(startDate, endDate);
        logger.info("Đã lấy thành công danh sách hành động admin từ {} đến {}, số lượng: {}", startDate, endDate, actions.size());
        return ResponseEntity.ok(actions);
    }
}