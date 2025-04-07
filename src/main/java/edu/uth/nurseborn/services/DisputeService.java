package edu.uth.nurseborn.services;

import edu.uth.nurseborn.dtos.DisputeDTO;
import edu.uth.nurseborn.exception.UserNotFoundException;
import edu.uth.nurseborn.models.Booking;
import edu.uth.nurseborn.models.Dispute;
import edu.uth.nurseborn.models.User;
import edu.uth.nurseborn.models.enums.DisputeStatus;
import edu.uth.nurseborn.repositories.BookingRepository;
import edu.uth.nurseborn.repositories.DisputeRepository;
import edu.uth.nurseborn.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DisputeService {

    private static final Logger logger = LoggerFactory.getLogger(DisputeService.class);

    @Autowired
    private DisputeRepository disputeRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public DisputeDTO createDispute(DisputeDTO disputeDTO) {
        logger.debug("Tạo Dispute với dữ liệu: {}", disputeDTO);

        // Kiểm tra booking
        Optional<Booking> bookingOptional = bookingRepository.findById(disputeDTO.getBookingId());
        if (bookingOptional.isEmpty()) {
            logger.warn("Không tìm thấy Booking với ID: {}", disputeDTO.getBookingId());
            throw new UserNotFoundException("Không tìm thấy Booking với ID: " + disputeDTO.getBookingId());
        }
        Booking booking = bookingOptional.get();

        // Kiểm tra raisedByUserId
        Optional<User> raisedByUserOptional = userRepository.findById(disputeDTO.getRaisedByUserId().longValue());
        if (raisedByUserOptional.isEmpty()) {
            logger.warn("Không tìm thấy User với ID: {}", disputeDTO.getRaisedByUserId());
            throw new UserNotFoundException("Không tìm thấy User với ID: " + disputeDTO.getRaisedByUserId());
        }
        User raisedByUser = raisedByUserOptional.get();
        if (!"family".equalsIgnoreCase(raisedByUser.getRole().name()) &&
                !"nurse".equalsIgnoreCase(raisedByUser.getRole().name())) {
            logger.warn("User với ID: {} không có role 'family' hoặc 'nurse'", disputeDTO.getRaisedByUserId());
            throw new IllegalArgumentException("User phải có role 'family' hoặc 'nurse' để tạo dispute");
        }

        // Kiểm tra raisedByUserId có liên quan đến booking không
        if (!raisedByUser.getUserId().equals(booking.getFamily().getUserId()) &&
                !raisedByUser.getUserId().equals(booking.getNurse().getUserId())) {
            logger.warn("User với ID: {} không liên quan đến Booking với ID: {}",
                    disputeDTO.getRaisedByUserId(), disputeDTO.getBookingId());
            throw new IllegalArgumentException("User phải là family hoặc nurse của booking để tạo dispute");
        }

        try {
            Dispute dispute = modelMapper.map(disputeDTO, Dispute.class);
            Dispute savedDispute = disputeRepository.save(dispute);
            disputeRepository.flush();
            logger.info("Đã lưu Dispute thành công với ID: {}", savedDispute.getDisputeId());

            return modelMapper.map(savedDispute, DisputeDTO.class);
        } catch (Exception ex) {
            logger.error("Lỗi khi lưu Dispute: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi lưu Dispute: " + ex.getMessage());
        }
    }

    public DisputeDTO getDisputeById(Integer disputeId) {
        logger.debug("Lấy Dispute với ID: {}", disputeId);

        Optional<Dispute> disputeOptional = disputeRepository.findById(disputeId);
        if (disputeOptional.isEmpty()) {
            logger.warn("Không tìm thấy Dispute với ID: {}", disputeId);
            throw new UserNotFoundException("Không tìm thấy Dispute với ID: " + disputeId);
        }

        Dispute dispute = disputeOptional.get();
        return modelMapper.map(dispute, DisputeDTO.class);
    }

    @Transactional
    public DisputeDTO updateDisputeStatus(Integer disputeId, String status) {
        logger.debug("Cập nhật trạng thái Dispute với ID: {} thành {}", disputeId, status);

        Optional<Dispute> disputeOptional = disputeRepository.findById(disputeId);
        if (disputeOptional.isEmpty()) {
            logger.warn("Không tìm thấy Dispute với ID: {}", disputeId);
            throw new UserNotFoundException("Không tìm thấy Dispute với ID: " + disputeId);
        }

        Dispute dispute = disputeOptional.get();
        try {
            dispute.setStatus(edu.uth.nurseborn.models.enums.DisputeStatus.valueOf(status.toUpperCase()));
            if (status.equalsIgnoreCase("RESOLVED") || status.equalsIgnoreCase("CLOSED")) {
                dispute.setResolvedAt(LocalDateTime.now());
            }
            logger.debug("Dispute entity trước khi cập nhật: {}", dispute);
            Dispute updatedDispute = disputeRepository.save(dispute);
            disputeRepository.flush();
            logger.info("Đã cập nhật trạng thái Dispute thành công với ID: {}", disputeId);

            return modelMapper.map(updatedDispute, DisputeDTO.class);
        } catch (Exception ex) {
            logger.error("Lỗi khi cập nhật trạng thái Dispute: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi cập nhật trạng thái Dispute: " + ex.getMessage());
        }
    }

    @Transactional
    public void deleteDispute(Integer disputeId) {
        logger.debug("Xóa Dispute với ID: {}", disputeId);

        Optional<Dispute> disputeOptional = disputeRepository.findById(disputeId);
        if (disputeOptional.isEmpty()) {
            logger.warn("Không tìm thấy Dispute với ID: {}", disputeId);
            throw new UserNotFoundException("Không tìm thấy Dispute với ID: " + disputeId);
        }

        try {
            disputeRepository.delete(disputeOptional.get());
            disputeRepository.flush();
            logger.info("Đã xóa Dispute thành công với ID: {}", disputeId);
        } catch (Exception ex) {
            logger.error("Lỗi khi xóa Dispute: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi xóa Dispute: " + ex.getMessage());
        }
    }

    public List<DisputeDTO> getAllDisputes() {
        logger.debug("Lấy tất cả Disputes");

        List<Dispute> disputes = disputeRepository.findAll();
        return disputes.stream()
                .map(dispute -> modelMapper.map(dispute, DisputeDTO.class))
                .collect(Collectors.toList());
    }

    public List<DisputeDTO> getDisputesByBookingId(Integer bookingId) {
        logger.debug("Lấy Disputes theo booking ID: {}", bookingId);

        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isEmpty()) {
            logger.warn("Không tìm thấy Booking với ID: {}", bookingId);
            throw new UserNotFoundException("Không tìm thấy Booking với ID: " + bookingId);
        }

        List<Dispute> disputes = disputeRepository.findByBookingBookingId(bookingId);
        return disputes.stream()
                .map(dispute -> modelMapper.map(dispute, DisputeDTO.class))
                .collect(Collectors.toList());
    }

    public List<DisputeDTO> getDisputesByRaisedByUserId(Long raisedByUserId) {
        logger.debug("Lấy Disputes theo raisedByUserId: {}", raisedByUserId);

        Optional<User> userOptional = userRepository.findById(raisedByUserId);
        if (userOptional.isEmpty()) {
            logger.warn("Không tìm thấy User với ID: {}", raisedByUserId);
            throw new UserNotFoundException("Không tìm thấy User với ID: " + raisedByUserId);
        }

        List<Dispute> disputes = disputeRepository.findByRaisedByUserId(raisedByUserId.longValue());
        return disputes.stream()
                .map(dispute -> modelMapper.map(dispute, DisputeDTO.class))
                .collect(Collectors.toList());
    }

    public List<DisputeDTO> getDisputesByStatus(String status) {
        logger.debug("Lấy Disputes theo trạng thái: {}", status);

        try {
            DisputeStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Trạng thái không hợp lệ: {}", status);
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + status);
        }

        List<Dispute> disputes = disputeRepository.findByStatus(status);
        return disputes.stream()
                .map(dispute -> modelMapper.map(dispute, DisputeDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public DisputeDTO updateDispute(Integer disputeId, DisputeDTO disputeDTO) {
        logger.debug("Cập nhật Dispute với ID: {}", disputeId);

        Optional<Dispute> disputeOptional = disputeRepository.findById(disputeId);
        if (disputeOptional.isEmpty()) {
            logger.warn("Không tìm thấy Dispute với ID: {}", disputeId);
            throw new UserNotFoundException("Không tìm thấy Dispute với ID: " + disputeId);
        }

        Dispute dispute = disputeOptional.get();
        try {
            dispute.setDescription(disputeDTO.getDescription());
            dispute.setStatus(DisputeStatus.valueOf(disputeDTO.getStatus().toUpperCase()));
            if (disputeDTO.getStatus().equalsIgnoreCase("RESOLVED") ||
                    disputeDTO.getStatus().equalsIgnoreCase("CLOSED")) {
                dispute.setResolvedAt(LocalDateTime.now());
            }

            logger.debug("Dispute entity trước khi cập nhật: {}", dispute);
            Dispute updatedDispute = disputeRepository.save(dispute);
            disputeRepository.flush();
            logger.info("Đã cập nhật Dispute thành công với ID: {}", disputeId);

            return modelMapper.map(updatedDispute, DisputeDTO.class);
        } catch (Exception ex) {
            logger.error("Lỗi khi cập nhật Dispute: {}", ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi cập nhật Dispute: " + ex.getMessage());
        }
    }
}

