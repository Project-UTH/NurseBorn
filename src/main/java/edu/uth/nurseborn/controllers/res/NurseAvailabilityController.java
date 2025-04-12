package edu.uth.nurseborn.controllers.res;

import edu.uth.nurseborn.dtos.NurseAvailabilityDTO;
import edu.uth.nurseborn.services.NurseAvailabilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nurse-availability")
public class NurseAvailabilityController {

    private static final Logger logger = LoggerFactory.getLogger(NurseAvailabilityController.class);

    @Autowired
    private NurseAvailabilityService nurseAvailabilityService;

    @PostMapping
    @PreAuthorize("hasRole('nurse')")
    public ResponseEntity<NurseAvailabilityDTO> createNurseAvailability(
            @RequestBody NurseAvailabilityDTO nurseAvailabilityDTO) {
        logger.debug("Yêu cầu tạo NurseAvailability cho nurseProfileId: {}",
                nurseAvailabilityDTO.getNurseProfileId());
        NurseAvailabilityDTO createdAvailability =
                nurseAvailabilityService.createNurseAvailability(nurseAvailabilityDTO);
        return ResponseEntity.ok(createdAvailability);
    }

    @GetMapping("/nurse/{nurseProfileId}")
    @PreAuthorize("hasAnyRole('nurse', 'family', 'admin')")
    public ResponseEntity<List<NurseAvailabilityDTO>> getAvailabilitiesByNurseProfileId(
            @PathVariable Integer nurseProfileId) {
        logger.debug("Yêu cầu lấy danh sách NurseAvailability cho nurseProfileId: {}", nurseProfileId);
        List<NurseAvailabilityDTO> availabilities =
                nurseAvailabilityService.getAvailabilitiesByNurseProfileId(nurseProfileId);
        return ResponseEntity.ok(availabilities);
    }

    @GetMapping("/{availabilityId}")
    @PreAuthorize("hasAnyRole('nurse', 'family', 'admin')")
    public ResponseEntity<NurseAvailabilityDTO> getNurseAvailability(
            @PathVariable Integer availabilityId) {
        logger.debug("Yêu cầu lấy NurseAvailability cho availabilityId: {}", availabilityId);
        NurseAvailabilityDTO nurseAvailabilityDTO =
                nurseAvailabilityService.getNurseAvailabilityById(availabilityId);
        return ResponseEntity.ok(nurseAvailabilityDTO);
    }

    @PutMapping("/{availabilityId}")
    @PreAuthorize("hasRole('nurse')")
    public ResponseEntity<NurseAvailabilityDTO> updateNurseAvailability(
            @PathVariable Integer availabilityId,
            @RequestBody NurseAvailabilityDTO nurseAvailabilityDTO) {
        logger.debug("Yêu cầu cập nhật NurseAvailability cho availabilityId: {}", availabilityId);
        NurseAvailabilityDTO updatedAvailability =
                nurseAvailabilityService.updateNurseAvailability(availabilityId, nurseAvailabilityDTO);
        return ResponseEntity.ok(updatedAvailability);
    }

    @DeleteMapping("/{availabilityId}")
    @PreAuthorize("hasRole('nurse')")
    public ResponseEntity<Void> deleteNurseAvailability(@PathVariable Integer availabilityId) {
        logger.debug("Yêu cầu xóa NurseAvailability cho availabilityId: {}", availabilityId);
        nurseAvailabilityService.deleteNurseAvailability(availabilityId);
        return ResponseEntity.ok().build();
    }
}