package com.smartspace.listing.controller;

import com.smartspace.auth.service.AuthContextService;
import com.smartspace.listing.dto.*;
import com.smartspace.listing.entity.Hall;
import com.smartspace.listing.entity.HallPhoto;
import com.smartspace.listing.repository.HallPhotoRepository;
import com.smartspace.listing.repository.HallRepository;
import com.smartspace.listing.service.FileStorageService;
import com.smartspace.listing.service.HallService;
import com.smartspace.listing.service.HallSettingsService;
import com.smartspace.listing.service.HallStaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/owner/halls")
@RequiredArgsConstructor
public class OwnerHallController {

    private final HallService hallService;
    private final HallSettingsService hallSettingsService;
    private final HallStaffService hallStaffService;
    private final FileStorageService fileStorageService;
    private final AuthContextService authContextService;
    private final HallRepository hallRepository;
    private final HallPhotoRepository hallPhotoRepository;

    @GetMapping
    public ResponseEntity<List<HallDto>> getMyHalls() {
        Long currentUserId = authContextService.getCurrentUserId();
        return ResponseEntity.ok(hallService.getHallsForOwner(currentUserId));
    }

    @PostMapping
    public ResponseEntity<HallDto> createHall(@RequestBody HallCreateRequest request) {
        Long currentUserId = authContextService.getCurrentUserId();
        return ResponseEntity.ok(hallService.createHall(currentUserId, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HallDto> getHall(@PathVariable String id) {
        return ResponseEntity.ok(hallService.getHall(id));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submitHall(@PathVariable String id) {
        Long currentUserId = authContextService.getCurrentUserId();
        hallService.submitHallForApproval(id, currentUserId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/opening-hours")
    public ResponseEntity<?> updateOpeningHours(@PathVariable String id, @RequestBody List<HallOpeningHoursDto> dtos) {
        Long currentUserId = authContextService.getCurrentUserId();
        hallSettingsService.replaceOpeningHours(id, currentUserId, dtos);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/price-rules")
    public ResponseEntity<?> updatePriceRules(@PathVariable String id, @RequestBody List<HallPriceRuleDto> dtos) {
        Long currentUserId = authContextService.getCurrentUserId();
        hallSettingsService.replacePriceRules(id, currentUserId, dtos);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/staff")
    public ResponseEntity<HallStaffDto> addStaff(@PathVariable String id, @RequestBody HallStaffRequest request) {
        Long currentUserId = authContextService.getCurrentUserId();
        return ResponseEntity.ok(hallStaffService.addStaff(id, currentUserId, request));
    }

    @GetMapping("/{id}/staff")
    public ResponseEntity<List<HallStaffDto>> getStaff(@PathVariable String id) {
        Long currentUserId = authContextService.getCurrentUserId();
        return ResponseEntity.ok(hallStaffService.getStaff(id, currentUserId));
    }

    @DeleteMapping("/{id}/staff/{userId}")
    public ResponseEntity<?> removeStaff(@PathVariable String id, @PathVariable String userId) {
        Long currentUserId = authContextService.getCurrentUserId();
        hallStaffService.removeStaff(id, currentUserId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/photos")
    public ResponseEntity<HallPhotoDto> uploadPhoto(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        Long currentUserId = authContextService.getCurrentUserId();
        Hall hall = hallRepository.findByPublicId(id).orElseThrow();
        if (!hall.getOwnerUserId().equals(currentUserId)) throw new SecurityException();
        
        String fileName = fileStorageService.storeFile(file);
        
        HallPhoto photo = HallPhoto.builder()
                .hallId(hall.getId())
                .filePath("/uploads/" + fileName)
                .sortOrder(0)
                .build();
        photo = hallPhotoRepository.save(photo);
        
        HallPhotoDto dto = new HallPhotoDto();
        dto.setId(photo.getId().toString());
        dto.setUrl(photo.getFilePath());
        dto.setSortOrder(photo.getSortOrder());
        return ResponseEntity.ok(dto);
    }
}
