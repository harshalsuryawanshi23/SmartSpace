package com.smartspace.analytics.controller;

import com.smartspace.analytics.dto.OwnerAnalyticsSummary;
import com.smartspace.analytics.service.OwnerAnalyticsService;
import com.smartspace.security.auth.SecurityUtils;
import com.smartspace.user.entity.User;
import com.smartspace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/owner/analytics")
@RequiredArgsConstructor
public class OwnerAnalyticsController {

    private final OwnerAnalyticsService analyticsService;
    private final UserRepository userRepository;

    @GetMapping("/summary")
    @PreAuthorize("hasRole('HALL_OWNER')")
    public ResponseEntity<OwnerAnalyticsSummary> getSummary(
            @RequestParam(required = false) Long hallId,
            @RequestParam Instant start,
            @RequestParam Instant end) {
        String publicId = SecurityUtils.getCurrentUserPublicId();
        User user = userRepository.findByPublicId(publicId).orElseThrow();
        Long ownerId = user.getId();
        return ResponseEntity.ok(analyticsService.getSummary(ownerId, hallId, start, end));
    }

    @GetMapping(value = "/export.csv", produces = "text/csv")
    @PreAuthorize("hasRole('HALL_OWNER')")
    public ResponseEntity<String> exportCsv(
            @RequestParam(required = false) Long hallId,
            @RequestParam Instant start,
            @RequestParam Instant end) {
        String publicId = SecurityUtils.getCurrentUserPublicId();
        User user = userRepository.findByPublicId(publicId).orElseThrow();
        Long ownerId = user.getId();
        String csv = analyticsService.generateCsvExport(ownerId, hallId, start, end);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=analytics.csv");
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=utf-8");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csv);
    }
}
