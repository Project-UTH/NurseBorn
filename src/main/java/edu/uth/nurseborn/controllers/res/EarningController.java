package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.dtos.EarningDTO;
import edu.uth.nurseborn.exception.UserNotFoundException;
import edu.uth.nurseborn.services.EarningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/earnings")
public class EarningController {

    private static final Logger logger = LoggerFactory.getLogger(EarningController.class);

    @Autowired
    private EarningService earningService;

    /**
     * Tạo một bản ghi thu nhập mới.
     *
     * @param earningDTO DTO chứa thông tin thu nhập
     * @return ResponseEntity chứa EarningDTO đã tạo
     */
    @PostMapping
    public ResponseEntity<EarningDTO> createEarning(@RequestBody EarningDTO earningDTO) {
        logger.debug("Yêu cầu tạo thu nhập mới: {}", earningDTO);
        try {
            EarningDTO createdEarning = earningService.createEarning(earningDTO);
            return new ResponseEntity<>(createdEarning, HttpStatus.CREATED);
        } catch (UserNotFoundException ex) {
            logger.warn("Không tìm thấy user: {}", ex.getMessage());
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (IllegalStateException ex) {
            logger.warn("Thu nhập đã tồn tại: {}", ex.getMessage());
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        } catch (RuntimeException ex) {
            logger.error("Lỗi khi tạo thu nhập: {}", ex.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Lấy danh sách thu nhập theo nurseUserId.
     *
     * @param nurseUserId ID của y tá
     * @return ResponseEntity chứa danh sách EarningDTO
     */
    @GetMapping("/nurse/{nurseUserId}")
    public ResponseEntity<List<EarningDTO>> getEarningsByNurseUserId(@PathVariable Long nurseUserId) {
        logger.debug("Yêu cầu lấy danh sách thu nhập cho nurseUserId: {}", nurseUserId);
        try {
            List<EarningDTO> earnings = earningService.getEarningsByNurseUserId(nurseUserId);
            return new ResponseEntity<>(earnings, HttpStatus.OK);
        } catch (UserNotFoundException ex) {
            logger.warn("Không tìm thấy user: {}", ex.getMessage());
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Lấy thông tin thu nhập theo earningId.
     *
     * @param earningId ID của thu nhập
     * @return ResponseEntity chứa EarningDTO
     */
    @GetMapping("/{earningId}")
    public ResponseEntity<EarningDTO> getEarningById(@PathVariable Integer earningId) {
        logger.debug("Yêu cầu lấy thu nhập với earningId: {}", earningId);
        try {
            EarningDTO earning = earningService.getEarningById(earningId);
            return new ResponseEntity<>(earning, HttpStatus.OK);
        } catch (RuntimeException ex) {
            logger.warn("Không tìm thấy thu nhập: {}", ex.getMessage());
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Cập nhật thông tin thu nhập.
     *
     * @param earningId ID của thu nhập
     * @param earningDTO DTO chứa thông tin cập nhật
     * @return ResponseEntity chứa EarningDTO đã cập nhật
     */
    @PutMapping("/{earningId}")
    public ResponseEntity<EarningDTO> updateEarning(@PathVariable Integer earningId, @RequestBody EarningDTO earningDTO) {
        logger.debug("Yêu cầu cập nhật thu nhập với earningId: {}", earningId);
        try {
            EarningDTO updatedEarning = earningService.updateEarning(earningId, earningDTO);
            return new ResponseEntity<>(updatedEarning, HttpStatus.OK);
        } catch (RuntimeException ex) {
            logger.warn("Lỗi khi cập nhật thu nhập: {}", ex.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Xóa thu nhập theo earningId.
     *
     * @param earningId ID của thu nhập
     * @return ResponseEntity
     */
    @DeleteMapping("/{earningId}")
    public ResponseEntity<Void> deleteEarning(@PathVariable Integer earningId) {
        logger.debug("Yêu cầu xóa thu nhập với earningId: {}", earningId);
        try {
            earningService.deleteEarning(earningId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException ex) {
            logger.warn("Lỗi khi xóa thu nhập: {}", ex.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}