package com.smartspace.booking.service;

import com.smartspace.booking.entity.PricingSuggestion;
import com.smartspace.booking.repository.PricingSuggestionRepository;
import com.smartspace.listing.entity.Hall;
import com.smartspace.listing.repository.HallRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingAdvisorJob {

    private final HallRepository hallRepository;
    private final PricingSuggestionRepository pricingSuggestionRepository;

    // Cron expression for nightly run (e.g., 2 AM)
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void analyzeAndSuggestPricing() {
        log.info("Starting Pricing Advisor Job...");
        List<Hall> halls = hallRepository.findAll();

        for (Hall hall : halls) {
            // MVP mock analysis
            // In a real scenario, this would query booking cells over the last 8 weeks for each 2-hour block
            double mockOccupancy = Math.random(); // 0.0 to 1.0

            if (mockOccupancy < 0.20) {
                // Suggest Discount
                int pct = 5 * Math.round(Math.min(25f, (float)((0.20 - mockOccupancy) / 0.20 * 25)) / 5);
                pct = Math.max(5, pct); // at least 5%
                
                PricingSuggestion ps = new PricingSuggestion();
                ps.setHall(hall);
                ps.setDayOfWeek("SATURDAY");
                ps.setStartHour(10);
                ps.setEndHour(12);
                ps.setSuggestionType("DISCOUNT");
                ps.setPercentage(pct);
                ps.setRationaleJson("{\"reason\": \"Low occupancy (under 20%)\"}");
                ps.setStatus("PENDING");
                pricingSuggestionRepository.save(ps);

            } else if (mockOccupancy > 0.80) {
                // Suggest Surge
                PricingSuggestion ps = new PricingSuggestion();
                ps.setHall(hall);
                ps.setDayOfWeek("SUNDAY");
                ps.setStartHour(18);
                ps.setEndHour(20);
                ps.setSuggestionType("SURGE");
                ps.setPercentage(10);
                ps.setRationaleJson("{\"reason\": \"High demand (over 80% occupancy)\"}");
                ps.setStatus("PENDING");
                pricingSuggestionRepository.save(ps);
            }
        }
        log.info("Finished Pricing Advisor Job.");
    }
}
