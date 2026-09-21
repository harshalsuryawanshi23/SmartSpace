package com.smartspace.listing.controller;

import com.smartspace.listing.entity.Hall;
import com.smartspace.listing.entity.Society;
import com.smartspace.listing.service.AdminApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/approvals")
@RequiredArgsConstructor
public class AdminApprovalController {

    private final AdminApprovalService adminApprovalService;

    @GetMapping("/societies")
    public ResponseEntity<List<Society>> getPendingSocieties() {
        // returning raw entities for MVP admin panel
        return ResponseEntity.ok(adminApprovalService.getPendingSocieties());
    }

    @PostMapping("/societies/{id}")
    public ResponseEntity<?> reviewSociety(@PathVariable String id, @RequestBody Map<String, Object> body) {
        boolean approve = (Boolean) body.get("approve");
        String reason = (String) body.get("reason");
        adminApprovalService.reviewSociety(id, approve, reason);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/halls")
    public ResponseEntity<List<Hall>> getPendingHalls() {
        // returning raw entities for MVP admin panel
        return ResponseEntity.ok(adminApprovalService.getPendingHalls());
    }

    @PostMapping("/halls/{id}")
    public ResponseEntity<?> reviewHall(@PathVariable String id, @RequestBody Map<String, Object> body) {
        boolean approve = (Boolean) body.get("approve");
        String reason = (String) body.get("reason");
        adminApprovalService.reviewHall(id, approve, reason);
        return ResponseEntity.ok().build();
    }
}
