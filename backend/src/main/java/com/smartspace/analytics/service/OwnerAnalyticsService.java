package com.smartspace.analytics.service;

import com.smartspace.analytics.dto.OwnerAnalyticsSummary;
import com.smartspace.booking.entity.Booking;
import com.smartspace.booking.entity.BookingStatus;
import com.smartspace.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OwnerAnalyticsService {

    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public OwnerAnalyticsSummary getSummary(Long ownerId, Long hallId, Instant start, Instant end) {
        List<Booking> bookings;
        if (hallId != null) {
            bookings = bookingRepository.findByHallIdAndStartAtBetween(hallId, start, end);
            // Must verify hall owner
        } else {
            bookings = bookingRepository.findByHallOwnerUserIdAndStartAtBetween(ownerId, start, end);
        }
        
        long totalBookings = 0;
        long occupiedMinutes = 0;
        BigDecimal grossRevenue = BigDecimal.ZERO;
        BigDecimal ownerEarnings = BigDecimal.ZERO;
        long cancellations = 0;
        long noShows = 0;
        long totalPeakHeadcount = 0;
        long totalDeclaredHeadcountForPeak = 0;
        
        java.util.Map<String, Integer> heatmap = new java.util.HashMap<>();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(java.time.ZoneId.systemDefault());

        for (Booking b : bookings) {
            // Verify ownerId matches if hallId was provided (for security)
            if (hallId != null && !b.getHall().getOwnerUserId().equals(ownerId)) {
                continue;
            }
            
            totalBookings++;
            if (b.getStatus() == BookingStatus.CHECKED_IN || 
                b.getStatus() == BookingStatus.CHECKED_OUT || 
                b.getStatus() == BookingStatus.COMPLETED) {
                
                occupiedMinutes += ChronoUnit.MINUTES.between(b.getStartAt(), b.getEndAt());
                if (b.getPriceTotal() != null) {
                    grossRevenue = grossRevenue.add(b.getPriceTotal());
                }
                if (b.getPriceBase() != null) {
                    ownerEarnings = ownerEarnings.add(b.getPriceBase()); // Simplification
                }
                
                if (b.getPeakHeadcount() != null && b.getPeakHeadcount() > 0) {
                    totalPeakHeadcount += b.getPeakHeadcount();
                    totalDeclaredHeadcountForPeak += b.getGuestCount();
                }
                
                String dateStr = formatter.format(b.getStartAt());
                heatmap.put(dateStr, heatmap.getOrDefault(dateStr, 0) + 1);
            } else if (b.getStatus() == BookingStatus.CANCELLED) {
                cancellations++;
            } else if (b.getStatus() == BookingStatus.NO_SHOW) {
                noShows++;
            }
        }

        long availableMinutes = ChronoUnit.MINUTES.between(start, end); // Simplified available time
        if (availableMinutes < 0) availableMinutes = 0;

        BigDecimal occupancy = availableMinutes > 0 
                ? BigDecimal.valueOf(occupiedMinutes).divide(BigDecimal.valueOf(availableMinutes), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
                
        BigDecimal cancelRate = totalBookings > 0
                ? BigDecimal.valueOf(cancellations).divide(BigDecimal.valueOf(totalBookings), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        BigDecimal noShowRate = totalBookings > 0
                ? BigDecimal.valueOf(noShows).divide(BigDecimal.valueOf(totalBookings), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        BigDecimal headcountRatio = totalDeclaredHeadcountForPeak > 0
                ? BigDecimal.valueOf(totalPeakHeadcount).divide(BigDecimal.valueOf(totalDeclaredHeadcountForPeak), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return OwnerAnalyticsSummary.builder()
                .totalBookings(totalBookings)
                .occupiedHours(occupiedMinutes / 60)
                .availableHours(availableMinutes / 60)
                .occupancyPercentage(occupancy)
                .grossRevenue(grossRevenue)
                .ownerEarnings(ownerEarnings)
                .cancellationRate(cancelRate)
                .noShowRate(noShowRate)
                .avgHeadcountDeclaredRatio(headcountRatio)
                .usageHeatmap(heatmap)
                .build();
    }

    @Transactional(readOnly = true)
    public String generateCsvExport(Long ownerId, Long hallId, Instant start, Instant end) {
        OwnerAnalyticsSummary summary = getSummary(ownerId, hallId, start, end);
        StringBuilder sb = new StringBuilder();
        sb.append("Metric,Value\n");
        sb.append("Total Bookings,").append(summary.getTotalBookings()).append("\n");
        sb.append("Occupied Hours,").append(summary.getOccupiedHours()).append("\n");
        sb.append("Available Hours,").append(summary.getAvailableHours()).append("\n");
        sb.append("Occupancy %,").append(summary.getOccupancyPercentage()).append("\n");
        sb.append("Gross Revenue,").append(summary.getGrossRevenue()).append("\n");
        sb.append("Owner Earnings,").append(summary.getOwnerEarnings()).append("\n");
        sb.append("Cancellation Rate %,").append(summary.getCancellationRate()).append("\n");
        sb.append("No-Show Rate %,").append(summary.getNoShowRate()).append("\n");
        sb.append("Peak Headcount vs Declared %,").append(summary.getAvgHeadcountDeclaredRatio()).append("\n");
        return sb.toString();
    }
}
