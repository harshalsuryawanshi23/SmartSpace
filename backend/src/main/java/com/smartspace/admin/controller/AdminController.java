package com.smartspace.admin.controller;

import com.smartspace.admin.service.AdminService;
import com.smartspace.security.auth.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/users/{userId}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> suspendUser(@PathVariable Long userId, @RequestParam String reason) {
        Long adminId = SecurityUtils.getCurrentUserId();
        adminService.suspendUser(adminId, userId, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reactivateUser(@PathVariable Long userId) {
        Long adminId = SecurityUtils.getCurrentUserId();
        adminService.reactivateUser(adminId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/revoke-kyc")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> revokeKyc(@PathVariable Long userId) {
        Long adminId = SecurityUtils.getCurrentUserId();
        adminService.revokeKyc(adminId, userId);
        return ResponseEntity.ok().build();
    }
}
