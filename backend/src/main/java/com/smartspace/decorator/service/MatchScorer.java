package com.smartspace.decorator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartspace.decorator.dto.MatchContext;
import com.smartspace.decorator.dto.MatchResult;
import com.smartspace.decorator.entity.Decorator;
import com.smartspace.decorator.entity.DecoratorPackage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MatchScorer {

    private final ObjectMapper objectMapper;
    private static final double EARTH_RADIUS_KM = 6371.0;

    public MatchScorer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public MatchResult score(Decorator decorator, List<DecoratorPackage> packages, MatchContext context) {
        if (decorator.getVerificationStatus() != Decorator.VerificationStatus.APPROVED) {
            return null; // Hard filter F1
        }

        // F2 - distance
        double distanceKm = haversine(
                context.getLat().doubleValue(), context.getLng().doubleValue(),
                decorator.getBaseLat().doubleValue(), decorator.getBaseLng().doubleValue()
        );
        if (distanceKm > decorator.getServiceRadiusKm().doubleValue()) {
            return null;
        }

        MatchResult bestMatch = null;
        List<DecoratorPackage> validPackages = new ArrayList<>();

        for (DecoratorPackage pkg : packages) {
            if (!pkg.getActive()) continue;

            List<String> eventTypes = parseJsonArray(pkg.getEventTypes());
            List<String> themeTags = parseJsonArray(pkg.getThemeTags());
            List<String> layoutTypes = parseJsonArray(pkg.getLayoutTypes());

            // F3 - event type
            if (!eventTypes.contains("ANY") && (context.getEventType() == null || !eventTypes.contains(context.getEventType()))) {
                continue;
            }

            // F4 - space size
            int S = context.getCapacitySeated() != null ? context.getCapacitySeated() : 0;
            if (S > 0) {
                double minCap = 0.8 * pkg.getMinCapacity();
                double maxCap = 1.25 * pkg.getMaxCapacity();
                if (S < minCap || S > maxCap) {
                    continue;
                }
            }

            validPackages.add(pkg);

            // Calculate Sub-scores
            double capacityFit = calculateCapacityFit(S, pkg.getMinCapacity(), pkg.getMaxCapacity());
            double layoutFit = calculateLayoutFit(layoutTypes, context.getLayoutType() != null ? context.getLayoutType().name() : null);
            double themeFit = calculateThemeFit(themeTags, context.getThemes());
            
            BigDecimal price = pkg.getBasePrice();
            if (context.getGuestCount() != null && context.getGuestCount() > 0) {
                price = price.add(pkg.getPricePerGuest().multiply(BigDecimal.valueOf(context.getGuestCount())));
            }
            double budgetFit = calculateBudgetFit(price, context.getBudgetMin(), context.getBudgetMax());
            
            double proximityFit = 1.0 - Math.min(1.0, distanceKm / decorator.getServiceRadiusKm().doubleValue());
            
            double qualityFit = decorator.getTrustScore() != null ? decorator.getTrustScore().doubleValue() / 100.0 : 0.6;

            // Step 3 - Weighted total
            double score = 100.0 * (
                0.20 * capacityFit +
                0.10 * layoutFit +
                0.25 * themeFit +
                0.20 * budgetFit +
                0.10 * proximityFit +
                0.15 * qualityFit
            );

            List<String> positiveReasons = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            generateExplanations(positiveReasons, warnings, capacityFit, layoutFit, themeFit, budgetFit, proximityFit, qualityFit, pkg);

            // Step 5 soft check would go here or outside
            // F5 - blackout check usually outside

            if (bestMatch == null || score > bestMatch.getMatchScore().doubleValue()) {
                bestMatch = MatchResult.builder()
                        .decoratorId(decorator.getId())
                        .businessName(decorator.getBusinessName())
                        .bestPackage(pkg)
                        .matchScore(BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP))
                        .positiveReasons(positiveReasons)
                        .warnings(warnings)
                        .proximityKm(BigDecimal.valueOf(distanceKm))
                        .trustScore(decorator.getTrustScore())
                        .build();
            }
        }

        if (bestMatch != null) {
            bestMatch.setAlternativePackagesCount(validPackages.size() - 1);
        }

        return bestMatch;
    }

    private double calculateCapacityFit(int s, int min, int max) {
        if (s == 0) return 1.0;
        if (s >= min && s <= max) return 1.0;
        double minCap = 0.8 * min;
        double maxCap = 1.25 * max;
        if (s < min && s >= minCap) {
            return (s - minCap) / (min - minCap);
        }
        if (s > max && s <= maxCap) {
            return (maxCap - s) / (maxCap - max);
        }
        return 0;
    }

    private double calculateLayoutFit(List<String> layoutTypes, String layout) {
        if (layout == null) return 1.0;
        return (layoutTypes.contains("ANY") || layoutTypes.contains(layout)) ? 1.0 : 0.0;
    }

    private double calculateThemeFit(List<String> pkgThemes, List<String> reqThemes) {
        if (reqThemes == null || reqThemes.isEmpty()) return 0.5;
        if (pkgThemes.isEmpty()) return 0.0;

        Set<String> pkgSet = pkgThemes.stream().map(String::toLowerCase).collect(Collectors.toSet());
        Set<String> reqSet = reqThemes.stream().map(String::toLowerCase).collect(Collectors.toSet());
        
        Set<String> intersection = reqSet.stream().filter(pkgSet::contains).collect(Collectors.toSet());
        Set<String> union = new java.util.HashSet<>(reqSet);
        union.addAll(pkgSet);

        double coverage = (double) intersection.size() / reqSet.size();
        double jaccard = (double) intersection.size() / union.size();

        return 0.75 * coverage + 0.25 * jaccard;
    }

    private double calculateBudgetFit(BigDecimal price, BigDecimal budgetMin, BigDecimal budgetMax) {
        if (budgetMax == null) return 0.7;
        
        double p = price.doubleValue();
        double max = budgetMax.doubleValue();
        
        if (p <= max) {
            if (budgetMin != null && p >= budgetMin.doubleValue()) {
                return 1.0;
            }
            return 0.9;
        }
        
        return Math.max(0, 1.0 - 2.0 * (p - max) / max);
    }

    private void generateExplanations(List<String> positives, List<String> warnings, 
                                    double capacityFit, double layoutFit, double themeFit, 
                                    double budgetFit, double proximityFit, double qualityFit, 
                                    DecoratorPackage pkg) {
        if (capacityFit >= 0.8) positives.add("Fits " + pkg.getMinCapacity() + "-" + pkg.getMaxCapacity() + " guests - right for this hall");
        else if (capacityFit <= 0.5) warnings.add("Slightly outside ideal capacity");

        if (layoutFit >= 0.8) positives.add("Works with this hall's layout");
        else if (layoutFit <= 0.5) warnings.add("Might not be ideal for this hall layout");

        if (themeFit >= 0.8) positives.add("Matches your theme preferences");
        else if (themeFit <= 0.5) warnings.add("Some themes not fully covered");

        if (budgetFit >= 0.8) positives.add("Within your budget");
        else if (budgetFit <= 0.5) warnings.add("Exceeds your budget");

        if (proximityFit >= 0.8) positives.add("Located nearby");
        
        if (qualityFit >= 0.8) positives.add("Highly rated decorator");
        else if (qualityFit <= 0.5) warnings.add("New or lower rated decorator");
    }

    private List<String> parseJsonArray(String json) {
        if (json == null || json.isEmpty()) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
