package com.smartspace.decorator.controller;

import com.smartspace.decorator.dto.DecoratorMatchRequest;
import com.smartspace.decorator.dto.DecoratorMatchResponse;
import com.smartspace.decorator.service.DecoratorMatchService;
import com.smartspace.user.repository.UserRepository;
import com.smartspace.security.auth.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DecoratorController {

    private final DecoratorMatchService matchService;
    private final com.smartspace.decorator.service.DecoratorConsoleService consoleService;
    private final UserRepository userRepository;

    @PostMapping("/decorator-matches")
    public ResponseEntity<List<DecoratorMatchResponse>> getMatches(
            @RequestBody DecoratorMatchRequest request,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(matchService.getMatches(request, limit));
    }

    @GetMapping("/vendor/packages")
    public ResponseEntity<List<com.smartspace.decorator.dto.DecoratorPackageResponse>> getPackages() {
        String publicIdStr = SecurityUtils.getCurrentUserPublicId();
        Long userId = userRepository.findByPublicId(publicIdStr)
                .orElseThrow(() -> new IllegalStateException("User not found"))
                .getId();
        return ResponseEntity.ok(consoleService.getPackages(userId));
    }

    @GetMapping("/vendor/calendar")
    public ResponseEntity<List<com.smartspace.decorator.dto.DecoratorCalendarResponse>> getCalendar() {
        String publicIdStr = SecurityUtils.getCurrentUserPublicId();
        Long userId = userRepository.findByPublicId(publicIdStr)
                .orElseThrow(() -> new IllegalStateException("User not found"))
                .getId();
        return ResponseEntity.ok(consoleService.getCalendar(userId));
    }
}
