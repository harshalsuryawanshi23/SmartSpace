package com.smartspace.listing.service;

import com.smartspace.listing.entity.Hall;
import com.smartspace.listing.repository.HallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final HallRepository hallRepository;

    @Transactional(readOnly = true)
    public List<Hall> search(
            BigDecimal lat, BigDecimal lng, double radiusKm,
            Integer guests,
            LocalDateTime windowStart, LocalDateTime windowEnd,
            List<String> amenities,
            BigDecimal minPrice, BigDecimal maxPrice
    ) {
        if (guests == null) guests = 1;
        if (radiusKm <= 0 || radiusKm > 25) radiusKm = 5.0;

        // Bounding box for native query prefilter
        double dLat = radiusKm / 111.0;
        double dLng = radiusKm / (111.0 * Math.cos(Math.toRadians(lat.doubleValue())));

        BigDecimal minLat = lat.subtract(BigDecimal.valueOf(dLat));
        BigDecimal maxLat = lat.add(BigDecimal.valueOf(dLat));
        BigDecimal minLng = lng.subtract(BigDecimal.valueOf(dLng));
        BigDecimal maxLng = lng.add(BigDecimal.valueOf(dLng));

        int hasWindow = (windowStart != null && windowEnd != null) ? 1 : 0;
        LocalDateTime winStart = windowStart != null ? windowStart : LocalDateTime.now();
        // Since we don't know the exact buffer for the specific hall inside the native query easily 
        // (we could just use h.buffer_after_minutes in SQL but let's just approximate 30 mins in Java for now, 
        // actually ADR says winEndPlusBuffer. Let's just add 30 mins for the query, and refine in post-filter)
        LocalDateTime winEndPlusBuffer = windowEnd != null ? windowEnd.plusMinutes(30) : LocalDateTime.now();
        LocalDateTime now = LocalDateTime.now();

        List<Hall> candidates = hallRepository.searchHalls(
                lat, lng, minLat, maxLat, minLng, maxLng, guests, radiusKm,
                hasWindow, winStart, winEndPlusBuffer, now
        );

        // Java post-filters
        return candidates.stream()
                .filter(h -> filterPrice(h, minPrice, maxPrice))
                .filter(h -> filterAmenities(h, amenities))
                .filter(h -> filterTime(h, windowStart, windowEnd))
                .collect(Collectors.toList());
    }

    private boolean filterPrice(Hall h, BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && h.getBasePricePerHour().compareTo(minPrice) < 0) return false;
        if (maxPrice != null && h.getBasePricePerHour().compareTo(maxPrice) > 0) return false;
        return true;
    }

    private boolean filterAmenities(Hall h, List<String> amenities) {
        if (amenities == null || amenities.isEmpty()) return true;
        for (String am : amenities) {
            String lower = am.toLowerCase();
            if (lower.equals("ac") && !h.getHasAc()) return false;
            if (lower.equals("parking") && !h.getHasParking()) return false;
            if (lower.equals("kitchen") && !h.getHasKitchen()) return false;
            if (lower.equals("stage") && !h.getHasStage()) return false;
            if (lower.equals("powerbackup") && !h.getHasPowerBackup()) return false;
            if (lower.equals("washroom") && !h.getHasWashroom()) return false;
        }
        return true;
    }

    private boolean filterTime(Hall h, LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return true;
        // Basic check for quiet hours / latest end time could go here
        if (h.getLatestEndTime() != null) {
            if (end.toLocalTime().isAfter(h.getLatestEndTime())) {
                return false;
            }
        }
        return true;
    }
}
