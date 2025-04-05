package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.dtos.FamilyProfileDTO;
import edu.uth.nurseborn.services.FamilyProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/family-profiles")
public class FamilyProfileController {
    private static final Logger logger = LoggerFactory.getLogger(FamilyProfileController.class);

    @Autowired
    private FamilyProfileService familyProfileService;

    @PostMapping("/{userId}")
    public ResponseEntity<FamilyProfileDTO> createFamilyProfile(@PathVariable Long userId, @RequestBody FamilyProfileDTO familyProfileDTO) {
        logger.debug("Yêu cầu tạo FamilyProfile cho userId: {}", userId);
        FamilyProfileDTO createdProfile = familyProfileService.createFamilyProfile(userId, familyProfileDTO);
        return ResponseEntity.ok(createdProfile);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<FamilyProfileDTO> updateFamilyProfile(@PathVariable Long userId, @RequestBody FamilyProfileDTO familyProfileDTO) {
        logger.debug("Yêu cầu cập nhật FamilyProfile cho userId: {}", userId);
        FamilyProfileDTO updatedProfile = familyProfileService.updateFamilyProfile(userId, familyProfileDTO);
        return ResponseEntity.ok(updatedProfile);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<FamilyProfileDTO> getFamilyProfile(@PathVariable Long userId) {
        logger.debug("Yêu cầu lấy FamilyProfile cho userId: {}", userId);
        FamilyProfileDTO familyProfileDTO = familyProfileService.getFamilyProfileByUserId(userId);
        return ResponseEntity.ok(familyProfileDTO);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteFamilyProfile(@PathVariable Long userId) {
        logger.debug("Yêu cầu xóa FamilyProfile cho userId: {}", userId);
        familyProfileService.deleteFamilyProfile(userId);
        return ResponseEntity.noContent().build();
    }
}