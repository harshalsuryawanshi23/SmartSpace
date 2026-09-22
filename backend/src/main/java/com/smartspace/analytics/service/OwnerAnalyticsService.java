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
        // Find bookings (hallId can be used for filtering, here we filter simply by time)
        // In a real app we would have a repository method filtering by ownerId and hallId
        List<Booking> bookings = bookingRepository.findAll(); // Simplified for now
        
        long totalBookings = 0;
        long occupiedMinutes = 0;
        BigDecimal grossRevenue = BigDecimal.ZERO;
        BigDecimal ownerEarnings = BigDecimal.ZERO;
        long cancellations = 0;
        long noShows = 0;

        for (Booking b : bookings) {
            // Filter logic if needed
            if (b.getStartAt().isBefore(start) || b.getStartAt().isAfter(end)) continue;
            
            // Assume we've verified ownerId matches
            
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

        return OwnerAnalyticsSummary.builder()
                .totalBookings(totalBookings)
                .occupiedHours(occupiedMinutes / 60)
                .availableHours(availableMinutes / 60)
                .occupancyPercentage(occupancy)
                .grossRevenue(grossRevenue)
                .ownerEarnings(ownerEarnings)
                .cancellationRate(cancelRate)
                .noShowRate(noShowRate)
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
        return sb.toString();
    }
}
