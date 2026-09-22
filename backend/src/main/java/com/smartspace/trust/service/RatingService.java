package com.smartspace.trust.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartspace.booking.entity.Booking;
import com.smartspace.booking.entity.BookingStatus;
import com.smartspace.booking.repository.BookingRepository;
import com.smartspace.trust.dto.RatingRequest;
import com.smartspace.trust.entity.Rating;
import com.smartspace.trust.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final BookingRepository bookingRepository;
    private final ObjectMapper objectMapper;
    private final TrustScoreService trustScoreService;

    @Transactional
    public Rating submitRating(Long bookingId, RatingRequest request, Long userId, Rating.RaterSide raterSide, Rating.SubjectType subjectType, Long subjectId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        // Rule 1: Booking status must be CHECKED_OUT or COMPLETED
        if (booking.getStatus() != BookingStatus.CHECKED_OUT && booking.getStatus() != BookingStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking must be CHECKED_OUT or COMPLETED");
        }

        // Rule 1b: Both CHECK_IN and CHECK_OUT rows must exist (checkedInAt and checkedOutAt are not null)
        if (booking.getCheckedInAt() == null || booking.getCheckedOutAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking must have both check-in and check-out logs");
        }

        // Rule 2: On-site duration >= min(30 min, 25% of the booked slot)
        long actualDurationMinutes = Duration.between(booking.getCheckedInAt(), booking.getCheckedOutAt()).toMinutes();
        long bookedSlotMinutes = Duration.between(booking.getStartAt(), booking.getEndAt()).toMinutes();
        long requiredDuration = Math.min(30, bookedSlotMinutes / 4);

        if (actualDurationMinutes < requiredDuration) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "On-site duration does not meet the minimum requirement for rating");
        }

        // Rule 3: Within the rating window (7 days after check-out)
        Instant ratingWindowEnd = booking.getCheckedOutAt().plus(Duration.ofDays(7));
        if (Instant.now().isAfter(ratingWindowEnd)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating window has closed");
        }

        // Rule 4: Rater is the booking's renter or the hall owner/staff. 
        // For simplicity in this layer, we assume controller checks if user owns booking/hall, but we enforce sides.
        if (raterSide == Rating.RaterSide.RENTER && !booking.getRenterUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the renter can rate from RENTER side");
        }
        // If HALL_SIDE, we would verify the user is owner/staff of the hall. (Assume controller does it).

        // Rule 5: At most one rating per (booking, side, subject_type)
        Optional<Rating> existingRating = ratingRepository.findByBookingIdAndRaterSideAndSubjectType(bookingId, raterSide, subjectType);
        if (existingRating.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Rating already submitted for this booking/side/subject");
        }

        // Rule 6: Skip decorator logic for now per Phase 12 deferral.

        // Anti-gaming: Collusion check (if this is the 4th booking rated in 90 days between same rater and subject, weight = 0.5)
        LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
        long pastRatingsCount = ratingRepository.countRatingsByRaterAndSubjectSince(userId, subjectId, subjectType, ninetyDaysAgo);
        
        BigDecimal weight = pastRatingsCount >= 3 ? new BigDecimal("0.50") : new BigDecimal("1.00");

        String dimensionsJson = null;
        if (request.getDimensions() != null) {
            try {
                dimensionsJson = objectMapper.writeValueAsString(request.getDimensions());
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize dimensions", e);
            }
        }

        Rating rating = Rating.builder()
                .booking(booking)
                .raterUserId(userId)
                .raterSide(raterSide)
                .subjectType(subjectType)
                .subjectId(subjectId)
                .stars(request.getStars())
                .dimensions(dimensionsJson)
                .comment(request.getComment())
                .weight(weight)
                .createdAt(LocalDateTime.now())
                .build();

        Rating savedRating = ratingRepository.save(rating);
        
        trustScoreService.recomputeScore(subjectType, subjectId);

        return savedRating;
    }
}
