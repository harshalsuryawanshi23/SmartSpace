package com.smartspace.decorator.controller;

import com.smartspace.decorator.dto.CreateEnquiryRequest;
import com.smartspace.decorator.dto.RespondEnquiryRequest;
import com.smartspace.decorator.entity.DecoratorEnquiry;
import com.smartspace.decorator.service.DecoratorEnquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DecoratorEnquiryController {

    private final DecoratorEnquiryService enquiryService;

    // Renter API
    @PostMapping("/decorator-enquiries")
    public ResponseEntity<DecoratorEnquiry> createEnquiry(
            @RequestBody CreateEnquiryRequest request,
            @RequestAttribute("userId") Long userId) {
        // In a real scenario userId is taken from SecurityContext, here it's mocked via attribute/header
        return ResponseEntity.ok(enquiryService.createEnquiry(request, userId != null ? userId : 1L)); // Fallback for mocking
    }

    @PostMapping("/decorator-enquiries/{id}/confirm")
    public ResponseEntity<Void> confirmEnquiry(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        enquiryService.confirmEnquiry(id, userId != null ? userId : 1L);
        return ResponseEntity.ok().build();
    }

    // Vendor (Decorator) API
    @PostMapping("/vendor/enquiries/{id}/respond")
    public ResponseEntity<Void> respondEnquiry(
            @PathVariable Long id,
            @RequestBody RespondEnquiryRequest request,
            @RequestAttribute("userId") Long userId) {
        // Fallback for mocking if no auth filter
        enquiryService.respondToEnquiry(id, userId != null ? userId : 2L, request); 
        return ResponseEntity.ok().build();
    }
}
