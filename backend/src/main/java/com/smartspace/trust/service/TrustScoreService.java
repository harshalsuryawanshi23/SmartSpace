package com.smartspace.trust.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartspace.booking.entity.ActorType;
import com.smartspace.booking.entity.Booking;
import com.smartspace.booking.entity.BookingStatus;
import com.smartspace.booking.entity.CancellationPolicy;
import com.smartspace.booking.repository.BookingRepository;
import com.smartspace.dispute.repository.DisputeRepository;
import com.smartspace.entry.entity.HandoverReport;
import com.smartspace.entry.repository.HandoverReportRepository;
import com.smartspace.trust.entity.Rating;
import com.smartspace.trust.entity.TrustScore;
import com.smartspace.trust.entity.TrustScoreId;
import com.smartspace.trust.repository.RatingRepository;
import com.smartspace.trust.repository.TrustScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrustScoreService {

    private final TrustScoreRepository trustScoreRepository;
    private final RatingRepository ratingRepository;
    private final BookingRepository bookingRepository;
    private final HandoverReportRepository handoverReportRepository;
    private final DisputeRepository disputeRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public TrustScore recomputeScore(Rating.SubjectType subjectType, Long subjectId) {
        if (subjectType == Rating.SubjectType.RENTER) {
            return recomputeResidentScore(subjectId);
        } else if (subjectType == Rating.SubjectType.HALL) {
            return recomputeHallScore(subjectId);
        } else {
            // Decorator logic deferred to Phase 12
            return null;
        }
    }

    private TrustScore recomputeResidentScore(Long renterId) {
        Instant oneYearAgo = Instant.now().minus(Duration.ofDays(365));
        
        List<Booking> allBookings = bookingRepository.findByRenterIdOrderByStartAtDesc(renterId)
                .stream()
                .filter(b -> b.getStartAt().isAfter(oneYearAgo))
                .collect(Collectors.toList());

        List<Booking> stays = allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CHECKED_OUT || b.getStatus() == BookingStatus.COMPLETED)
                .collect(Collectors.toList());

        double sumW = 0.0;
        double sumWS = 0.0;
        
        for (Booking stay : stays) {
            Optional<Rating> ratingOpt = ratingRepository.findByBookingIdAndRaterSideAndSubjectType(
                    stay.getId(), Rating.RaterSide.HALL_SIDE, Rating.SubjectType.RENTER);
            
            boolean rated = ratingOpt.isPresent();
            double q_i = 0.0;
            double collusionWeight = 1.0;
            if (rated) {
                Rating r = ratingOpt.get();
                q_i = (r.getStars() - 1) / 4.0;
                collusionWeight = r.getWeight().doubleValue();
            }

            double overstayMinutes = stay.getOverstayMinutes() != null ? stay.getOverstayMinutes() : 0.0;
            double p_i = Math.max(0.0, Math.min(1.0, 1.0 - (overstayMinutes / 60.0)));
            
            Optional<HandoverReport> handoverOpt = handoverReportRepository.findByBookingIdAndPhase(stay.getId(), HandoverReport.HandoverPhase.AFTER);
            double c_i = handoverOpt.map(h -> h.getChecklistScore().doubleValue()).orElse(1.0); // Assume 1.0 if no checklist

            double s_i = rated ? (0.5 * q_i + 0.25 * p_i + 0.25 * c_i) : (0.5 * p_i + 0.5 * c_i);
            
            long ageDays = Duration.between(stay.getEndAt(), Instant.now()).toDays();
            double decay_i = Math.max(0.1, Math.pow(0.5, ageDays / 180.0));
            
            double w_i = decay_i * collusionWeight * (rated ? 1.0 : 0.5);
            
            sumW += w_i;
            sumWS += (w_i * s_i);
        }

        // Penalties
        long lateCancellations = allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCELLED && b.getCancelledBy() == ActorType.RENTER)
                .filter(this::isLateCancellation)
                .count();

        long noShows = allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.NO_SHOW)
                .count();

        long upheldDisputes = disputeRepository.countUpheldDisputesAgainstRenter(renterId);

        double penalty = Math.min(0.40, 0.05 * lateCancellations + 0.15 * noShows + 0.20 * upheldDisputes);

        double m = 0.60;
        double C = 3.0;

        double rawScore = (C * m + sumWS) / (C + sumW);
        double finalScore = 100 * (1 - penalty) * rawScore;

        int verifiedStaysCount = stays.size();
        TrustScore.Badge badge = TrustScore.Badge.STANDARD;
        
        if (verifiedStaysCount < 3) {
            badge = TrustScore.Badge.NEW;
        } else if (verifiedStaysCount >= 5 && finalScore >= 80.0 && upheldDisputes == 0) {
            badge = TrustScore.Badge.TRUSTED;
        } else if (verifiedStaysCount >= 3 && finalScore < 40.0) {
            badge = TrustScore.Badge.WATCH;
        }

        return saveTrustScore(Rating.SubjectType.RENTER, renterId, finalScore, badge, verifiedStaysCount, 
                buildComponents(m, C, penalty, sumWS, sumW, lateCancellations, noShows, upheldDisputes));
    }

    private TrustScore recomputeHallScore(Long hallId) {
        Instant oneYearAgo = Instant.now().minus(Duration.ofDays(365));
        
        // Find bookings for the hall
        // In a real app we'd have a findByHallId method, but here we can query all or rely on Rating table.
        // The algorithm says: For each completed stay...
        // We'll get all ratings for this hall from renters
        List<Rating> ratings = ratingRepository.findBySubjectTypeAndSubjectIdOrderByCreatedAtDesc(Rating.SubjectType.HALL, hallId)
                .stream()
                .filter(r -> r.getCreatedAt().isAfter(LocalDateTime.now().minusDays(365)))
                .collect(Collectors.toList());

        double sumW = 0.0;
        double sumWR = 0.0;

        for (Rating r : ratings) {
            double r_i = (r.getStars() - 1) / 4.0;
            long ageDays = Duration.between(r.getCreatedAt(), LocalDateTime.now()).toDays();
            double decay_i = Math.max(0.1, Math.pow(0.5, ageDays / 180.0));
            double w_i = decay_i * r.getWeight().doubleValue();
            
            sumW += w_i;
            sumWR += (w_i * r_i);
        }

        // Reliability penalty
        // ownerCancellationRate365
        List<Booking> allHallBookings = bookingRepository.findByHallIdAndStartAtBetween(hallId, oneYearAgo, Instant.now());
        long ownerCancellations = allHallBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCELLED && b.getCancelledBy() == ActorType.OWNER)
                .count();
        
        double ownerCancellationRate = allHallBookings.isEmpty() ? 0.0 : (double) ownerCancellations / allHallBookings.size();
        double reliabilityPenalty = Math.min(0.30, 0.6 * ownerCancellationRate);

        double m = 0.70;
        double C = 5.0;

        double rawScore = (C * m + sumWR) / (C + sumW);
        double finalScore = 100 * (1 - reliabilityPenalty) * rawScore;

        int verifiedStaysCount = (int) allHallBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CHECKED_OUT || b.getStatus() == BookingStatus.COMPLETED)
                .count();

        TrustScore.Badge badge = TrustScore.Badge.STANDARD;
        if (verifiedStaysCount < 3) {
            badge = TrustScore.Badge.NEW;
        } else if (verifiedStaysCount >= 5 && finalScore >= 80.0) {
            badge = TrustScore.Badge.TRUSTED;
        } else if (verifiedStaysCount >= 3 && finalScore < 40.0) {
            badge = TrustScore.Badge.WATCH;
        }

        return saveTrustScore(Rating.SubjectType.HALL, hallId, finalScore, badge, verifiedStaysCount,
                buildComponents(m, C, reliabilityPenalty, sumWR, sumW, ownerCancellations, 0, 0));
    }

    private boolean isLateCancellation(Booking b) {
        // Assume late cancellation if cancelled within 24h of start, or based on policy.
        if (b.getCancelledAt() == null || b.getStartAt() == null) return false;
        long hoursBefore = Duration.between(b.getCancelledAt(), b.getStartAt()).toHours();
        if (b.getCancellationPolicy() == CancellationPolicy.STRICT && hoursBefore < 168) return true; // 7 days
        if (b.getCancellationPolicy() == CancellationPolicy.MODERATE && hoursBefore < 72) return true; // 3 days
        if (b.getCancellationPolicy() == CancellationPolicy.FLEXIBLE && hoursBefore < 24) return true; // 1 day
        return false;
    }

    private TrustScore saveTrustScore(Rating.SubjectType subjectType, Long subjectId, double score, TrustScore.Badge badge, 
                                      int verifiedStays, String components) {
        TrustScore ts = trustScoreRepository.findBySubjectTypeAndSubjectId(subjectType, subjectId)
                .orElse(TrustScore.builder()
                        .subjectType(subjectType)
                        .subjectId(subjectId)
                        .build());
        
        ts.setScore(BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP));
        ts.setBadge(badge);
        ts.setVerifiedStays(verifiedStays);
        ts.setComponents(components);
        ts.setComputedAt(LocalDateTime.now());
        
        return trustScoreRepository.save(ts);
    }

    private String buildComponents(double m, double C, double penalty, double sumWS, double sumW, long L, long N, long D) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("priorMean", m);
            map.put("priorWeight", C);
            map.put("penalty", penalty);
            map.put("sumWeightedScore", sumWS);
            map.put("sumWeight", sumW);
            map.put("lateCancellations", L);
            map.put("noShows", N);
            map.put("disputes", D);
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            log.error("Failed to serialize components", e);
            return "{}";
        }
    }
}
