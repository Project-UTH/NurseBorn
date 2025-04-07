package edu.uth.nurseborn.repositories;

import edu.uth.nurseborn.models.AdminAction;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.models.enums.ActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminActionRepository extends JpaRepository<AdminAction, Integer> {

    // Tìm tất cả hành động admin theo admin (User)
    List<AdminAction> findByAdmin(User admin);

    // Tìm tất cả hành động admin theo actionType
    List<AdminAction> findByActionType(ActionType actionType);

    // Tìm hành động theo admin và actionType kết hợp (trả về Optional thay vì List nếu muốn duy nhất)
    Optional<AdminAction> findByAdminAndActionType(User admin, ActionType actionType);

    // Tìm tất cả hành động theo target user
    List<AdminAction> findByTarget(User target);

    // Tìm hành động theo khoảng thời gian actionDate
    List<AdminAction> findByActionDateBetween(LocalDateTime start, LocalDateTime end);

    // Đếm số hành động theo actionType
    Long countByActionType(ActionType actionType);
}