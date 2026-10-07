package com.smartspace.dispute.controller;

import com.smartspace.dispute.dto.DisputeRaiseRequest;
import com.smartspace.dispute.dto.DisputeResolveRequest;
import com.smartspace.dispute.entity.Dispute;
import com.smartspace.dispute.entity.DisputeEvidence;
import com.smartspace.dispute.service.DisputeService;
import com.smartspace.security.auth.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeService disputeService;

    @GetMapping("/disputes/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<java.util.List<Dispute>> getMyDisputes() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(disputeService.getMyDisputes(userId));
    }

    @PostMapping("/disputes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Dispute> raiseDispute(@Valid @RequestBody DisputeRaiseRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Dispute dispute = disputeService.raiseDispute(userId, request);
        return ResponseEntity.ok(dispute);
    }

    @PostMapping("/disputes/{id}/evidence")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DisputeEvidence> uploadEvidence(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "note", required = false) String note) {
        Long userId = SecurityUtils.getCurrentUserId();
        // Here we would typically save the file to FileStorageService and get the path
        String filePath = "/uploads/" + file.getOriginalFilename(); // Simplified
        DisputeEvidence evidence = disputeService.uploadEvidence(userId, id, filePath, note);
        return ResponseEntity.ok(evidence);
    }

    @PostMapping("/admin/disputes/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Dispute> resolveDispute(
            @PathVariable Long id,
            @Valid @RequestBody DisputeResolveRequest request) {
        Long adminId = SecurityUtils.getCurrentUserId();
        Dispute dispute = disputeService.resolveDispute(adminId, id, request);
        return ResponseEntity.ok(dispute);
    }

    @GetMapping("/admin/disputes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.List<Dispute>> getAllDisputes() {
        return ResponseEntity.ok(disputeService.getAllDisputes());
    }
}
