package com.smartspace.analytics.controller;

import com.smartspace.analytics.dto.OwnerAnalyticsSummary;
import com.smartspace.analytics.service.OwnerAnalyticsService;
import com.smartspace.security.auth.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/owner/analytics")
@RequiredArgsConstructor
public class OwnerAnalyticsController {

    private final OwnerAnalyticsService analyticsService;

    @GetMapping("/summary")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<OwnerAnalyticsSummary> getSummary(
            @RequestParam(required = false) Long hallId,
            @RequestParam Instant start,
            @RequestParam Instant end) {
        Long ownerId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(analyticsService.getSummary(ownerId, hallId, start, end));
    }

    @GetMapping(value = "/export.csv", produces = "text/csv")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<String> exportCsv(
            @RequestParam(required = false) Long hallId,
            @RequestParam Instant start,
            @RequestParam Instant end) {
        Long ownerId = SecurityUtils.getCurrentUserId();
        String csv = analyticsService.generateCsvExport(ownerId, hallId, start, end);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=analytics.csv");
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=utf-8");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csv);
    }
}
