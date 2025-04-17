package edu.uth.nurseborn.services;

import edu.uth.nurseborn.dtos.ServiceDTO;
import edu.uth.nurseborn.exception.UserNotFoundException;
import edu.uth.nurseborn.models.NurseProfile;
import edu.uth.nurseborn.models.NurseService;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.models.enums.Role;
import edu.uth.nurseborn.models.enums.ServiceStatus;
import edu.uth.nurseborn.repositories.NurseProfileRepository;
import edu.uth.nurseborn.repositories.ServiceRepository;
import edu.uth.nurseborn.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NurseServiceService {

    private static final Logger logger = LoggerFactory.getLogger(NurseServiceService.class);

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NurseProfileRepository nurseProfileRepository;

    @Autowired
    private ModelMapper modelMapper;

    // Phương thức mới: Lấy danh sách y tá thỏa mãn điều kiện
    public List<NurseProfile> getApprovedNurses() {
        logger.debug("Lấy danh sách y tá với role = NURSE, is_verified = true, và is_approved = true");

        // Bước 1: Lấy danh sách User có role = NURSE và is_verified = true
        List<User> nurses = userRepository.findByRoleAndIsVerified(Role.NURSE, true);
        if (nurses.isEmpty()) {
            logger.info("Không tìm thấy y tá nào với role = NURSE và is_verified = true");
            return List.of();
        }

        // Bước 2: Lấy danh sách user_id từ các User đã lọc
        List<Long> nurseUserIds = nurses.stream()
                .map(User::getUserId)
                .collect(Collectors.toList());

        // Bước 3: Lấy danh sách NurseProfile có user_id thuộc danh sách trên và is_approved = true
        List<NurseProfile> approvedNurseProfiles = nurseProfileRepository.findByUserUserIdInAndIsApproved(nurseUserIds, true);
        logger.info("Tìm thấy {} y tá thỏa mãn điều kiện", approvedNurseProfiles.size());

        return approvedNurseProfiles;
    }

    // Các phương thức hiện có của NurseServiceService
    @Transactional
    public ServiceDTO createService(String nurseUsername, ServiceDTO serviceDTO) {
        logger.debug("Tạo dịch vụ cho nurse: {}", nurseUsername);

        Optional<User> nurseOptional = userRepository.findByUsername(nurseUsername);
        if (!nurseOptional.isPresent()) {
            logger.warn("Không tìm thấy nurse với username: {}", nurseUsername);
            throw new UserNotFoundException("Không tìm thấy nurse với username: " + nurseUsername);
        }
        User nurse = nurseOptional.get();

        if (!"NURSE".equalsIgnoreCase(nurse.getRole().toString())) {
            logger.warn("User với username: {} không có role 'NURSE', role hiện tại: {}", nurseUsername, nurse.getRole());
            throw new IllegalArgumentException("User phải có role 'NURSE' để tạo dịch vụ");
        }

        if (serviceDTO.getServiceName() == null || serviceDTO.getServiceName().trim().isEmpty()) {
            logger.warn("Tên dịch vụ không được rỗng");
            throw new IllegalArgumentException("Tên dịch vụ không được rỗng");
        }
        if (serviceDTO.getPrice() == null || serviceDTO.getPrice() <= 0) {
            logger.warn("Giá dịch vụ phải lớn hơn 0");
            throw new IllegalArgumentException("Giá dịch vụ phải lớn hơn 0");
        }
        if (serviceDTO.getAvailabilitySchedule() == null || serviceDTO.getAvailabilitySchedule().trim().isEmpty()) {
            logger.warn("Lịch trống không được rỗng");
            throw new IllegalArgumentException("Lịch trống không được rỗng");
        }

        try {
            NurseService service = modelMapper.map(serviceDTO, NurseService.class);
            service.setNurse(nurse);
            if (serviceDTO.getStatus() != null) {
                service.setStatus(ServiceStatus.valueOf(serviceDTO.getStatus().toUpperCase()));
            } else {
                service.setStatus(ServiceStatus.ACTIVE);
            }

            logger.debug("NurseService entity trước khi lưu: {}", service);
            NurseService savedService = serviceRepository.save(service);
            serviceRepository.flush();
            logger.info("Đã tạo dịch vụ thành công cho nurse: {}", nurseUsername);

            ServiceDTO savedServiceDTO = modelMapper.map(savedService, ServiceDTO.class);
            savedServiceDTO.setStatus(savedService.getStatus().name());
            return savedServiceDTO;
        } catch (Exception ex) {
            logger.error("Lỗi khi tạo dịch vụ: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi tạo dịch vụ: " + ex.getMessage());
        }
    }

    @Transactional
    public ServiceDTO updateService(Integer serviceId, ServiceDTO serviceDTO) {
        logger.debug("Cập nhật dịch vụ với serviceId: {}", serviceId);

        Optional<NurseService> serviceOptional = serviceRepository.findByServiceId(serviceId);
        if (!serviceOptional.isPresent()) {
            logger.warn("Không tìm thấy dịch vụ với ID: {}", serviceId);
            throw new RuntimeException("Không tìm thấy dịch vụ với ID: " + serviceId);
        }

        NurseService service = serviceOptional.get();

        if (serviceDTO.getServiceName() == null || serviceDTO.getServiceName().trim().isEmpty()) {
            logger.warn("Tên dịch vụ không được rỗng");
            throw new IllegalArgumentException("Tên dịch vụ không được rỗng");
        }
        if (serviceDTO.getPrice() == null || serviceDTO.getPrice() <= 0) {
            logger.warn("Giá dịch vụ phải lớn hơn 0");
            throw new IllegalArgumentException("Giá dịch vụ phải lớn hơn 0");
        }
        if (serviceDTO.getAvailabilitySchedule() == null || serviceDTO.getAvailabilitySchedule().trim().isEmpty()) {
            logger.warn("Lịch trống không được rỗng");
            throw new IllegalArgumentException("Lịch trống không được rỗng");
        }

        try {
            modelMapper.map(serviceDTO, service);
            if (serviceDTO.getStatus() != null) {
                service.setStatus(ServiceStatus.valueOf(serviceDTO.getStatus().toUpperCase()));
            }

            logger.debug("NurseService entity trước khi cập nhật: {}", service);
            NurseService updatedService = serviceRepository.save(service);
            serviceRepository.flush();
            logger.info("Đã cập nhật dịch vụ thành công với serviceId: {}", serviceId);

            ServiceDTO updatedServiceDTO = modelMapper.map(updatedService, ServiceDTO.class);
            updatedServiceDTO.setStatus(updatedService.getStatus().name());
            return updatedServiceDTO;
        } catch (Exception ex) {
            logger.error("Lỗi khi cập nhật dịch vụ: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi cập nhật dịch vụ: " + ex.getMessage());
        }
    }

    public ServiceDTO getServiceById(Integer serviceId) {
        logger.debug("Lấy dịch vụ với serviceId: {}", serviceId);

        Optional<NurseService> serviceOptional = serviceRepository.findByServiceId(serviceId);
        if (!serviceOptional.isPresent()) {
            logger.warn("Không tìm thấy dịch vụ với ID: {}", serviceId);
            throw new RuntimeException("Không tìm thấy dịch vụ với ID: " + serviceId);
        }

        NurseService service = serviceOptional.get();
        ServiceDTO serviceDTO = modelMapper.map(service, ServiceDTO.class);
        serviceDTO.setStatus(service.getStatus().name());
        return serviceDTO;
    }

    public List<ServiceDTO> getServicesByNurse(String nurseUsername) {
        logger.debug("Lấy danh sách dịch vụ của nurse: {}", nurseUsername);

        Optional<User> nurseOptional = userRepository.findByUsername(nurseUsername);
        if (!nurseOptional.isPresent()) {
            logger.warn("Không tìm thấy nurse với username: {}", nurseUsername);
            throw new UserNotFoundException("Không tìm thấy nurse với username: " + nurseUsername);
        }

        List<NurseService> services = serviceRepository.findByNurseUserId(nurseOptional.get().getUserId().intValue());
        return services.stream()
                .map(service -> {
                    ServiceDTO dto = modelMapper.map(service, ServiceDTO.class);
                    dto.setStatus(service.getStatus().name());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<ServiceDTO> getActiveServicesByNurse(String nurseUsername) {
        logger.debug("Lấy danh sách dịch vụ active của nurse: {}", nurseUsername);

        Optional<User> nurseOptional = userRepository.findByUsername(nurseUsername);
        if (!nurseOptional.isPresent()) {
            logger.warn("Không tìm thấy nurse với username: {}", nurseUsername);
            throw new UserNotFoundException("Không tìm thấy nurse với username: " + nurseUsername);
        }

        List<NurseService> services = serviceRepository.findByNurseUserIdAndStatus(
                nurseOptional.get().getUserId().intValue(), ServiceStatus.ACTIVE);
        return services.stream()
                .map(service -> {
                    ServiceDTO dto = modelMapper.map(service, ServiceDTO.class);
                    dto.setStatus(service.getStatus().name());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<ServiceDTO> searchServicesByName(String serviceName) {
        logger.debug("Tìm kiếm dịch vụ với tên: {}", serviceName);

        List<NurseService> services = serviceRepository.findByServiceNameContainingIgnoreCase(serviceName);
        return services.stream()
                .map(service -> {
                    ServiceDTO dto = modelMapper.map(service, ServiceDTO.class);
                    dto.setStatus(service.getStatus().name());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteService(Integer serviceId) {
        logger.debug("Xóa dịch vụ với serviceId: {}", serviceId);

        Optional<NurseService> serviceOptional = serviceRepository.findByServiceId(serviceId);
        if (!serviceOptional.isPresent()) {
            logger.warn("Không tìm thấy dịch vụ với ID: {}", serviceId);
            throw new RuntimeException("Không tìm thấy dịch vụ với ID: " + serviceId);
        }

        try {
            serviceRepository.delete(serviceOptional.get());
            serviceRepository.flush();
            logger.info("Đã xóa dịch vụ thành công với serviceId: {}", serviceId);
        } catch (Exception ex) {
            logger.error("Lỗi khi xóa dịch vụ: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi xóa dịch vụ: " + ex.getMessage());
        }
    }

    @Transactional
    public void updateServiceStatus(Integer serviceId, ServiceStatus status) {
        logger.debug("Cập nhật trạng thái dịch vụ với serviceId: {}", serviceId);

        Optional<NurseService> serviceOptional = serviceRepository.findByServiceId(serviceId);
        if (!serviceOptional.isPresent()) {
            logger.warn("Không tìm thấy dịch vụ với ID: {}", serviceId);
            throw new RuntimeException("Không tìm thấy dịch vụ với ID: " + serviceId);
        }

        NurseService service = serviceOptional.get();
        service.setStatus(status);
        serviceRepository.save(service);
        serviceRepository.flush();
        logger.info("Đã cập nhật trạng thái dịch vụ với serviceId: {} thành {}", serviceId, status);
    }
}