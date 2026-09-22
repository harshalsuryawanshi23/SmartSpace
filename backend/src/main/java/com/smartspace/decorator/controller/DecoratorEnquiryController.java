package com.smartspace.decorator.controller;

import com.smartspace.decorator.entity.DecoratorEnquiry;
import com.smartspace.decorator.service.DecoratorEnquiryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/decorator-enquiries")
public class DecoratorEnquiryController {

    private final DecoratorEnquiryService enquiryService;

    public DecoratorEnquiryController(DecoratorEnquiryService enquiryService) {
        this.enquiryService = enquiryService;
    }

    @PostMapping
    public ResponseEntity<DecoratorEnquiry> createEnquiry(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, Object> payload) {
        
        Long bookingId = Long.valueOf(payload.get("bookingId").toString());
        Long decoratorId = Long.valueOf(payload.get("decoratorId").toString());
        Long packageId = payload.containsKey("packageId") ? Long.valueOf(payload.get("packageId").toString()) : null;
        String message = (String) payload.get("message");

        return ResponseEntity.ok(enquiryService.createEnquiry(userId, bookingId, decoratorId, packageId, message));
    }

    @PostMapping("/{publicId}/respond")
    public ResponseEntity<DecoratorEnquiry> respondToEnquiry(
            @PathVariable String publicId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, Object> payload) {
        
        // Note: For real auth, we'd find the decoratorId tied to the userId.
        // Assuming we pass decoratorId in payload or header for simplification.
        Long decoratorId = Long.valueOf(payload.get("decoratorId").toString());
        boolean accept = Boolean.parseBoolean(payload.get("accept").toString());
        BigDecimal quotedPrice = payload.containsKey("quotedPrice") ? new BigDecimal(payload.get("quotedPrice").toString()) : null;
        String message = (String) payload.get("message");

        return ResponseEntity.ok(enquiryService.decoratorRespond(decoratorId, publicId, accept, quotedPrice, message));
    }

    @PostMapping("/{publicId}/confirm")
    public ResponseEntity<DecoratorEnquiry> confirmEnquiry(
            @PathVariable String publicId,
            @RequestHeader("X-User-Id") Long userId) {
        
        return ResponseEntity.ok(enquiryService.renterConfirm(userId, publicId));
    }
}
