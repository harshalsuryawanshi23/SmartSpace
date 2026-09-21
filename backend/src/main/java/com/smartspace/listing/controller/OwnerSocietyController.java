package com.smartspace.listing.controller;

import com.smartspace.auth.service.AuthContextService;
import com.smartspace.listing.dto.SocietyCreateRequest;
import com.smartspace.listing.dto.SocietyDto;
import com.smartspace.listing.service.SocietyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/owner/societies")
@RequiredArgsConstructor
public class OwnerSocietyController {

    private final SocietyService societyService;
    private final AuthContextService authContextService;

    @GetMapping
    public ResponseEntity<List<SocietyDto>> getMySocieties() {
        Long currentUserId = authContextService.getCurrentUserId();
        return ResponseEntity.ok(societyService.getSocietiesForManager(currentUserId));
    }

    @PostMapping
    public ResponseEntity<SocietyDto> createSociety(@RequestBody SocietyCreateRequest request) {
        Long currentUserId = authContextService.getCurrentUserId();
        return ResponseEntity.ok(societyService.createSociety(currentUserId, request));
    }

    @PostMapping("/{id}/members/{userId}/approve")
    public ResponseEntity<?> approveMember(@PathVariable("id") String societyPublicId, @PathVariable("userId") String memberPublicId) {
        Long currentUserId = authContextService.getCurrentUserId();
        societyService.approveMember(societyPublicId, currentUserId, memberPublicId);
        return ResponseEntity.ok().build();
    }
}
