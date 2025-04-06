package edu.uth.nurseborn.services;

import edu.uth.nurseborn.models.AdminAction;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.models.enums.ActionType;
import edu.uth.nurseborn.repositories.AdminActionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminActionService {

    private final AdminActionRepository adminActionRepository;

    @Autowired
    public AdminActionService(AdminActionRepository adminActionRepository) {
        this.adminActionRepository = adminActionRepository;
    }

    // 👉 Ghi log hành động admin
    public AdminAction logAction(User admin, ActionType actionType, User target, String description) {
        AdminAction action = new AdminAction(admin, actionType, target, description);
        return adminActionRepository.save(action);
    }

    // 👉 Lấy các hành động theo admin
    public List<AdminAction> getActionsByAdmin(User admin) {
        return adminActionRepository.findByAdmin(admin);
    }

    // 👉 Lấy các hành động theo user bị ảnh hưởng
    public List<AdminAction> getActionsByTarget(User target) {
        return adminActionRepository.findByTarget(target);
    }

    // 👉 Lấy theo loại hành động
    public List<AdminAction> getActionsByType(ActionType actionType) {
        return adminActionRepository.findByActionType(actionType);
    }

    // 👉 Lấy các hành động trong khoảng thời gian
    public List<AdminAction> getActionsBetween(LocalDateTime from, LocalDateTime to) {
        return adminActionRepository.findAllBetweenDates(from, to);
    }

    // 👉 Lấy các hành động giữa 1 admin và 1 người dùng
    public List<AdminAction> getActionsByAdminAndTarget(User admin, User target) {
        return adminActionRepository.findByAdminAndTarget(admin, target);
    }
}
