package com.smartspace.decorator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartspace.decorator.dto.MatchContext;
import com.smartspace.decorator.dto.MatchResult;
import com.smartspace.decorator.entity.Decorator;
import com.smartspace.decorator.entity.DecoratorPackage;
import com.smartspace.listing.entity.LayoutType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MatchScorerTest {

    private MatchScorer matchScorer;

    @BeforeEach
    public void setup() {
        matchScorer = new MatchScorer(new ObjectMapper());
    }

    @Test
    public void testMatchScoring() {
        Decorator decorator = new Decorator();
        decorator.setId(1L);
        decorator.setVerificationStatus(Decorator.VerificationStatus.APPROVED);
        decorator.setBaseLat(new BigDecimal("18.5362"));
        decorator.setBaseLng(new BigDecimal("73.8939")); // Koregaon Park
        decorator.setServiceRadiusKm(new BigDecimal("15.0"));
        decorator.setTrustScore(new BigDecimal("80.0")); // qualityFit = 0.8
        
        DecoratorPackage pkg = new DecoratorPackage();
        pkg.setActive(true);
        pkg.setMinCapacity(30);
        pkg.setMaxCapacity(100);
        pkg.setEventTypes("[\"BIRTHDAY\", \"ANNIVERSARY\"]");
        pkg.setThemeTags("[\"floral\", \"traditional\"]");
        pkg.setLayoutTypes("[\"OPEN_HALL\"]");
        pkg.setBasePrice(new BigDecimal("5000"));
        pkg.setPricePerGuest(new BigDecimal("0"));

        MatchContext context = MatchContext.builder()
                .capacitySeated(50) // S=50, fits in 30-100 -> capacityFit = 1.0
                .layoutType(LayoutType.OPEN_HALL) // layoutFit = 1.0
                .lat(new BigDecimal("18.5679")) // Viman Nagar (dist ~3.5km) -> proximityFit ~0.76
                .lng(new BigDecimal("73.9143"))
                .eventType("BIRTHDAY")
                .themes(List.of("floral")) // intersection 1, req 1, union 2. coverage = 1, jaccard=0.5 -> 0.75 + 0.125 = 0.875
                .budgetMax(new BigDecimal("6000")) // price 5000 <= 6000 -> budgetFit = 0.9 (since no budgetMin)
                .build();

        MatchResult result = matchScorer.score(decorator, List.of(pkg), context);

        assertNotNull(result);
        assertEquals(1L, result.getDecoratorId());
        
        // Let's check proximity ~3.5 -> 3.5/15 = 0.233 -> proximityFit = 0.766
        // Capacity: 0.20 * 1 = 0.20
        // Layout: 0.10 * 1 = 0.10
        // Theme: 0.25 * 0.875 = 0.21875
        // Budget: 0.20 * 0.9 = 0.18
        // Proximity: 0.10 * 0.766 = 0.0766
        // Quality: 0.15 * 0.8 = 0.12
        // Total = 0.89535 -> 89.54
        
        assertTrue(result.getMatchScore().doubleValue() > 80.0);
        assertTrue(result.getPositiveReasons().size() > 0);
    }
}
