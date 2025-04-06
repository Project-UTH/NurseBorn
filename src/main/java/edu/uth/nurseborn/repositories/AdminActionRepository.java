package edu.uth.nurseborn.repositories;

import edu.uth.nurseborn.models.AdminAction;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.models.enums.ActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdminActionRepository extends JpaRepository<AdminAction, Integer> {

    // 🔍 Tìm tất cả hành động của 1 admin cụ thể
    List<AdminAction> findByAdmin(User admin);

    // 🔍 Tìm tất cả hành động tác động lên 1 người dùng
    List<AdminAction> findByTarget(User target);

    // 🔍 Tìm theo loại hành động
    List<AdminAction> findByActionType(ActionType actionType);

    // 🔍 Tìm theo admin + actionType
    List<AdminAction> findByAdminAndActionType(User admin, ActionType actionType);

    // 🕵️‍♀️ Tìm toàn bộ lịch sử hành động giữa 1 admin và 1 target
    List<AdminAction> findByAdminAndTarget(User admin, User target);

    // 🗓️ Tìm tất cả hành động trong khoảng thời gian
    @Query("SELECT a FROM AdminAction a WHERE a.actionDate >= :from AND a.actionDate <= :to")
    List<AdminAction> findAllBetweenDates(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
