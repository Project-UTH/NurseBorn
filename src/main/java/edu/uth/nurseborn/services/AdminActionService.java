package edu.uth.nurseborn.services;

import edu.uth.nurseborn.dtos.AdminActionDTO;
import edu.uth.nurseborn.models.AdminAction;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.models.enums.ActionType;
import edu.uth.nurseborn.models.enums.Role;
import edu.uth.nurseborn.repositories.AdminActionRepository;
import edu.uth.nurseborn.repositories.UserRepository;
import org.hibernate.stat.Statistics;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminActionService {

    private static final Logger logger = LoggerFactory.getLogger(AdminActionService.class);

    private final AdminActionRepository adminActionRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;


    // Constructor injection
    public AdminActionService(AdminActionRepository adminActionRepository, UserRepository userRepository, ModelMapper modelMapper) {
        this.adminActionRepository = adminActionRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public AdminActionDTO createAdminAction(AdminActionDTO adminActionDTO) {
        logger.debug("Bắt đầu tạo hành động admin: {}", adminActionDTO);

        try {
            // Chuyển Integer sang Long
            User admin = userRepository.findById(adminActionDTO.getAdminUserId().longValue())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy admin với ID: " + adminActionDTO.getAdminUserId()));
            User target = userRepository.findById(adminActionDTO.getTargetUserId().longValue())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy target với ID: " + adminActionDTO.getTargetUserId()));

            // Ánh xạ DTO sang entity
            AdminAction adminAction = new AdminAction();
            adminAction.setAdmin(admin);
            adminAction.setActionType(ActionType.valueOf(adminActionDTO.getActionType().toUpperCase()));
            adminAction.setTarget(target);
            adminAction.setDescription(adminActionDTO.getDescription());
            // @PrePersist sẽ tự động set actionDate

            logger.debug("AdminAction entity trước khi lưu: {}", adminAction);

            // Lưu vào cơ sở dữ liệu
            AdminAction savedAction = adminActionRepository.save(adminAction);
            adminActionRepository.flush();
            logger.info("Đã tạo hành động admin thành công với ID: {}", savedAction.getActionId());

            return mapToDTO(savedAction);
        } catch (IllegalArgumentException e) {
            logger.error("ActionType không hợp lệ: {}", adminActionDTO.getActionType());
            throw new IllegalArgumentException("ActionType không hợp lệ: " + adminActionDTO.getActionType());
        } catch (Exception e) {
            logger.error("Lỗi khi tạo hành động admin: {}", e.getMessage());
            throw new RuntimeException("Không thể tạo hành động admin: " + e.getMessage());
        }
    }

    public List<AdminActionDTO> getActionsByAdminId(Integer adminUserId) {
        logger.debug("Tìm hành động admin với adminUserId: {}", adminUserId);
        User admin = userRepository.findById(adminUserId.longValue())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy admin với ID: " + adminUserId));
        List<AdminAction> actions = adminActionRepository.findByAdmin(admin);
        return actions.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Lấy chi tiết hành động admin theo actionId
    public AdminActionDTO getAdminActionById(Integer actionId) {
        logger.debug("Tìm hành động admin với actionId: {}", actionId);
        AdminAction adminAction = adminActionRepository.findById(actionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hành động admin với ID: " + actionId));
        return mapToDTO(adminAction);
    }

    // Xóa hành động admin theo actionId
    @Transactional
    public void deleteAdminAction(Integer actionId) {
        logger.debug("Xóa hành động admin với actionId: {}", actionId);
        AdminAction adminAction = adminActionRepository.findById(actionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hành động admin với ID: " + actionId));
        adminActionRepository.delete(adminAction);
        logger.info("Đã xóa hành động admin với actionId: {}", actionId);
    }

    // Lấy danh sách hành động admin trong khoảng thời gian
    public List<AdminActionDTO> getActionsByDateRange(String startDate, String endDate) {
        logger.debug("Tìm hành động admin từ {} đến {}", startDate, endDate);
        // Chuyển đổi String thành LocalDateTime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse(startDate, formatter);
        LocalDateTime end = LocalDateTime.parse(endDate, formatter);

        List<AdminAction> actions = adminActionRepository.findByActionDateBetween(start, end);
        return actions.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private AdminActionDTO mapToDTO(AdminAction adminAction) {
        AdminActionDTO dto = new AdminActionDTO();
        dto.setActionId(adminAction.getActionId());
        dto.setAdminUserId(adminAction.getAdmin().getUserId().intValue());
        dto.setActionType(adminAction.getActionType().name().toLowerCase());
        dto.setTargetUserId(adminAction.getTarget().getUserId().intValue());
        dto.setDescription(adminAction.getDescription());
        dto.setActionDate(adminAction.getActionDate());
        return dto;
    }

}