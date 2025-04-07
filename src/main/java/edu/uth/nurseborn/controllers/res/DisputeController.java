package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.dtos.DisputeDTO;
import edu.uth.nurseborn.services.DisputeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/disputes")
public class DisputeController {
    private static final Logger logger = LoggerFactory.getLogger(DisputeController.class);

    @Autowired
    private DisputeService disputeService;

    // Tạo một dispute mới
    @PostMapping
    public ResponseEntity<DisputeDTO> createDispute(@RequestBody DisputeDTO disputeDTO) {
        logger.debug("Yêu cầu tạo Dispute với dữ liệu: {}", disputeDTO);
        DisputeDTO createdDispute = disputeService.createDispute(disputeDTO);
        return ResponseEntity.ok(createdDispute);
    }

    // Lấy thông tin dispute theo disputeId
    @GetMapping("/{disputeId}")
    public ResponseEntity<DisputeDTO> getDisputeById(@PathVariable Integer disputeId) {
        logger.debug("Yêu cầu lấy Dispute với disputeId: {}", disputeId);
        DisputeDTO disputeDTO = disputeService.getDisputeById(disputeId);
        return ResponseEntity.ok(disputeDTO);
    }

    // Cập nhật trạng thái của dispute
    @PutMapping("/{disputeId}/status")
    public ResponseEntity<DisputeDTO> updateDisputeStatus(@PathVariable Integer disputeId, @RequestBody String status) {
        logger.debug("Yêu cầu cập nhật trạng thái Dispute với disputeId: {} thành {}", disputeId, status);
        DisputeDTO updatedDispute = disputeService.updateDisputeStatus(disputeId, status);
        return ResponseEntity.ok(updatedDispute);
    }

    // Cập nhật thông tin dispute
    @PutMapping("/{disputeId}")
    public ResponseEntity<DisputeDTO> updateDispute(@PathVariable Integer disputeId, @RequestBody DisputeDTO disputeDTO) {
        logger.debug("Yêu cầu cập nhật Dispute với disputeId: {}", disputeId);
        DisputeDTO updatedDispute = disputeService.updateDispute(disputeId, disputeDTO);
        return ResponseEntity.ok(updatedDispute);
    }

    // Xóa dispute
    @DeleteMapping("/{disputeId}")
    public ResponseEntity<Void> deleteDispute(@PathVariable Integer disputeId) {
        logger.debug("Yêu cầu xóa Dispute với disputeId: {}", disputeId);
        disputeService.deleteDispute(disputeId);
        return ResponseEntity.noContent().build();
    }

    // Lấy tất cả disputes
    @GetMapping
    public ResponseEntity<List<DisputeDTO>> getAllDisputes() {
        logger.debug("Yêu cầu lấy tất cả Disputes");
        List<DisputeDTO> disputes = disputeService.getAllDisputes();
        return ResponseEntity.ok(disputes);
    }

    // Lấy disputes theo bookingId
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<DisputeDTO>> getDisputesByBookingId(@PathVariable Integer bookingId) {
        logger.debug("Yêu cầu lấy Disputes theo bookingId: {}", bookingId);
        List<DisputeDTO> disputes = disputeService.getDisputesByBookingId(bookingId);
        return ResponseEntity.ok(disputes);
    }

    // Lấy disputes theo raisedByUserId
    @GetMapping("/user/{raisedByUserId}")
    public ResponseEntity<List<DisputeDTO>> getDisputesByRaisedByUserId(@PathVariable Long raisedByUserId) {
        logger.debug("Yêu cầu lấy Disputes theo raisedByUserId: {}", raisedByUserId);
        List<DisputeDTO> disputes = disputeService.getDisputesByRaisedByUserId(raisedByUserId);
        return ResponseEntity.ok(disputes);
    }

    // Lấy disputes theo status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<DisputeDTO>> getDisputesByStatus(@PathVariable String status) {
        logger.debug("Yêu cầu lấy Disputes theo status: {}", status);
        List<DisputeDTO> disputes = disputeService.getDisputesByStatus(status);
        return ResponseEntity.ok(disputes);
    }
}