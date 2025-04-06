package edu.uth.nurseborn.repositories;

import edu.uth.nurseborn.models.NurseService;
import edu.uth.nurseborn.models.enums.ServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository extends JpaRepository<NurseService, Integer> {

    // Tìm dịch vụ theo nurse_user_id
    List<NurseService> findByNurseUserId(Integer nurseUserId);

    // Tìm dịch vụ theo nurse_user_id và status
    List<NurseService> findByNurseUserIdAndStatus(Integer nurseUserId, ServiceStatus status);

    // Tìm dịch vụ theo service_name (có thể tìm kiếm gần đúng nếu cần)
    List<NurseService> findByServiceNameContainingIgnoreCase(String serviceName);

    // Tìm dịch vụ theo service_id
    Optional<NurseService> findByServiceId(Integer serviceId);
}