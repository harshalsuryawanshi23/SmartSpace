package com.smartspace.decorator.controller;

import com.smartspace.decorator.dto.CreateEnquiryRequest;
import com.smartspace.decorator.dto.RespondEnquiryRequest;
import com.smartspace.decorator.entity.DecoratorEnquiry;
import com.smartspace.decorator.service.DecoratorEnquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.smartspace.security.auth.SecurityUtils;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DecoratorEnquiryController {

    private final DecoratorEnquiryService enquiryService;
    private final com.smartspace.decorator.service.DecoratorConsoleService consoleService;

    // Renter API
    @PostMapping("/decorator-enquiries")
    @PreAuthorize("hasRole('RENTER')")
    public ResponseEntity<DecoratorEnquiry> createEnquiry(@RequestBody CreateEnquiryRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(enquiryService.createEnquiry(request, userId)); 
    }

    @PostMapping("/decorator-enquiries/{id}/confirm")
    @PreAuthorize("hasRole('RENTER')")
    public ResponseEntity<Void> confirmEnquiry(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        enquiryService.confirmEnquiry(id, userId);
        return ResponseEntity.ok().build();
    }

    // Vendor (Decorator) API
    @GetMapping("/vendor/enquiries")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<java.util.List<com.smartspace.decorator.dto.DecoratorEnquiryResponse>> getVendorEnquiries() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(
            consoleService.getEnquiries(userId)
        );
    }

    @PostMapping("/vendor/enquiries/{id}/respond")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<Void> respondEnquiry(
            @PathVariable Long id,
            @RequestBody RespondEnquiryRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        enquiryService.respondToEnquiry(id, userId, request); 
        return ResponseEntity.ok().build();
    }
}
