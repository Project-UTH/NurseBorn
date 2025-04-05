package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.dtos.NurseProfileDTO;
import edu.uth.nurseborn.repositories.NurseProfileRepository;
import edu.uth.nurseborn.services.NurseProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nurse-profile")
public class NurseProfileController {
    private static final Logger logger = LoggerFactory.getLogger(NurseProfileController.class);

    @Autowired
    private NurseProfileService nurseProfileService;

    @PostMapping("/{userId}")
    public ResponseEntity<NurseProfileDTO> createNurseProfile(@PathVariable Long userId, @RequestBody NurseProfileDTO nurseProfileDTO) {
        logger.debug("Yêu cầu tạo NurseProfile cho userId: {}", userId);
        NurseProfileDTO createdProfile = nurseProfileService.createNurseProfile(userId, nurseProfileDTO);
        return ResponseEntity.ok(createdProfile);
    }

    @PutMapping("/{userId}")
    public  ResponseEntity<NurseProfileDTO> updateNurseProfile(@PathVariable Long userId, @RequestBody NurseProfileDTO nurseProfileDTO) {
        logger.debug("Yêu cầu cập nhật NurseProfile cho userId: {}", userId);
        NurseProfileDTO updatedProfile = nurseProfileService.updateNurseProfile(userId, nurseProfileDTO);
        return ResponseEntity.ok(updatedProfile);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<NurseProfileDTO> getNurseProfile(@PathVariable Long userId) {
        logger.debug("Yêu cầu lấy NurseProfile cho userId: {}", userId);
        NurseProfileDTO nurseProfileDTO = nurseProfileService.getNurseProfileByUserId(userId);
        return ResponseEntity.ok(nurseProfileDTO);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<NurseProfileDTO> deleteNurseProfile(@PathVariable Long userId) {
        logger.debug("Yêu cầu xóa NurseProfile cho userId: {}", userId);
        nurseProfileService.deleteNurseProfile(userId);
        return ResponseEntity.ok().build();
    }
}
