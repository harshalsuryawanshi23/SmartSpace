package com.smartspace.listing.controller;

import com.smartspace.listing.entity.Hall;
import com.smartspace.listing.entity.Locality;
import com.smartspace.listing.service.LocalityService;
import com.smartspace.listing.service.SearchService;
import com.smartspace.listing.repository.HallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PublicDiscoveryController {

    private final SearchService searchService;
    private final LocalityService localityService;
    private final HallRepository hallRepository;

    @GetMapping("/halls/search")
    public ResponseEntity<List<Hall>> searchHalls(
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lng,
            @RequestParam(required = false, defaultValue = "5.0") double radiusKm,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime windowStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime windowEnd,
            @RequestParam(required = false) Integer guests,
            @RequestParam(required = false) List<String> amenities,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        List<Hall> results = searchService.search(
                lat, lng, radiusKm, guests, windowStart, windowEnd,
                amenities, minPrice, maxPrice
        );
        return ResponseEntity.ok(results);
    }

    @GetMapping("/halls/{id}")
    public ResponseEntity<Hall> getHallDetail(@PathVariable Long id) {
        return hallRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/halls/{id}/availability")
    public ResponseEntity<?> getHallAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        // Mock read model response
        return ResponseEntity.ok().build();
    }

    @GetMapping("/halls/{id}/reviews")
    public ResponseEntity<?> getHallReviews(@PathVariable Long id) {
        // Mock reviews
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/geo/localities")
    public ResponseEntity<List<Locality>> searchLocalities(@RequestParam String q) {
        return ResponseEntity.ok(localityService.searchLocalities(q));
    }
}
