package com.smartspace.trust.controller;

import com.smartspace.trust.dto.RatingRequest;
import com.smartspace.trust.entity.Rating;
import com.smartspace.trust.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings/{bookingId}/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    // Renter rating Hall
    @PostMapping("/hall")
    public ResponseEntity<Rating> rateHall(
            @PathVariable Long bookingId,
            @RequestBody @Valid RatingRequest request,
            @RequestParam Long hallId,
            // Security principle provides userId. Assuming simple representation here for demo
            @RequestHeader("X-User-Id") Long userId) {

        Rating rating = ratingService.submitRating(bookingId, request, userId, Rating.RaterSide.RENTER, Rating.SubjectType.HALL, hallId);
        return ResponseEntity.ok(rating);
    }

    // Hall rating Renter
    @PostMapping("/renter")
    public ResponseEntity<Rating> rateRenter(
            @PathVariable Long bookingId,
            @RequestBody @Valid RatingRequest request,
            @RequestParam Long renterId,
            @RequestHeader("X-User-Id") Long userId) {

        Rating rating = ratingService.submitRating(bookingId, request, userId, Rating.RaterSide.HALL_SIDE, Rating.SubjectType.RENTER, renterId);
        return ResponseEntity.ok(rating);
    }
}
