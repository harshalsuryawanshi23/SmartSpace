package com.smartspace.decorator.service;

import com.smartspace.decorator.dto.DecoratorMatchRequest;
import com.smartspace.decorator.dto.DecoratorMatchResponse;
import com.smartspace.decorator.entity.Decorator;
import com.smartspace.decorator.entity.DecoratorBlackout;
import com.smartspace.decorator.entity.DecoratorPackage;
import com.smartspace.decorator.repository.DecoratorBlackoutRepository;
import com.smartspace.decorator.repository.DecoratorEnquiryRepository;
import com.smartspace.decorator.repository.DecoratorPackageRepository;
import com.smartspace.decorator.repository.DecoratorRepository;
import com.smartspace.listing.entity.Hall;
import com.smartspace.listing.repository.HallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DecoratorMatchService {

    private final DecoratorRepository decoratorRepository;
    private final DecoratorPackageRepository packageRepository;
    private final DecoratorBlackoutRepository blackoutRepository;
    private final DecoratorEnquiryRepository enquiryRepository;
    private final HallRepository hallRepository;

    public List<DecoratorMatchResponse> getMatches(DecoratorMatchRequest request, int limit) {
        Hall hall = hallRepository.findById(request.getHallId()).orElseThrow();
        
        List<DecoratorPackage> allActivePackages = packageRepository.findByActiveTrue();
        
        List<DecoratorMatchResponse> responses = new ArrayList<>();
        Map<Long, List<DecoratorMatchResponse>> decoratorToPackages = new HashMap<>();
        
        for (DecoratorPackage pkg : allActivePackages) {
            Decorator decorator = pkg.getDecorator();
            
            // F1: Decorator APPROVED
            if (decorator.getStatus() != Decorator.DecoratorStatus.APPROVED) continue;
            
            // F2: Haversine distance <= radius
            double distance = haversine(hall.getLat().doubleValue(), hall.getLng().doubleValue(),
                                        decorator.getLat().doubleValue(), decorator.getLng().doubleValue());
            if (distance > decorator.getServiceRadiusKm().doubleValue()) continue;
            
            // F3: Event types match or ANY
            if (request.getEventType() != null && !request.getEventType().isEmpty()) {
                if (!pkg.getEventTypes().contains("ANY") && !pkg.getEventTypes().contains(request.getEventType())) {
                    continue;
                }
            }
            
            // F4: Space size constraints
            int S = hall.getCapacitySeated();
            if (S < 0.8 * pkg.getMinCapacity() || S > 1.25 * pkg.getMaxCapacity()) continue;
            
            // F5: Blackouts and Enquiries
            boolean blocked = false;
            if (request.getSlotStart() != null && request.getSlotEnd() != null) {
                var start = request.getSlotStart().minusMinutes(pkg.getSetupMinutes());
                var end = request.getSlotEnd().plusMinutes(pkg.getTeardownMinutes());
                
                if (!blackoutRepository.findOverlappingBlackouts(decorator.getId(), start, end).isEmpty()) {
                    blocked = true;
                } else if (!enquiryRepository.findOverlappingEnquiries(decorator.getId(), start, end).isEmpty()) {
                    blocked = true;
                }
            }
            if (blocked) continue;
            
            // Sub-scores
            double capacityFit = calculateCapacityFit(S, pkg.getMinCapacity(), pkg.getMaxCapacity());
            
            double layoutFit = (pkg.getLayoutTypes().contains("ANY") || pkg.getLayoutTypes().contains(hall.getLayoutType().name())) ? 1.0 : 0.0;
            
            double themeFit = 0.5;
            if (request.getThemes() != null && !request.getThemes().isEmpty()) {
                Set<String> reqThemes = new HashSet<>(request.getThemes());
                Set<String> pkgThemes = new HashSet<>(pkg.getThemeTags());
                
                Set<String> intersection = new HashSet<>(reqThemes);
                intersection.retainAll(pkgThemes);
                
                Set<String> union = new HashSet<>(reqThemes);
                union.addAll(pkgThemes);
                
                double coverage = (double) intersection.size() / reqThemes.size();
                double jaccard = (double) intersection.size() / union.size();
                
                themeFit = 0.75 * coverage + 0.25 * jaccard;
            }
            
            double base = pkg.getBasePrice().doubleValue();
            double ppg = pkg.getPricePerGuest().doubleValue();
            int guests = request.getGuestCount() != null ? request.getGuestCount() : 0;
            double price = base + (ppg * guests);
            
            double budgetFit = 0.7;
            if (request.getBudgetMax() != null) {
                double max = request.getBudgetMax().doubleValue();
                if (price <= max) {
                    budgetFit = (request.getBudgetMin() != null && price >= request.getBudgetMin().doubleValue()) ? 1.0 : 0.9;
                } else {
                    budgetFit = Math.max(0.0, 1.0 - 2.0 * (price - max) / max);
                }
            }
            
            double proximityFit = 1.0 - Math.min(1.0, distance / decorator.getServiceRadiusKm().doubleValue());
            
            double qualityFit = decorator.getTrustScore() != null ? decorator.getTrustScore() / 100.0 : 0.6;
            
            double rawScore = 0.20 * capacityFit + 0.10 * layoutFit + 0.25 * themeFit + 0.20 * budgetFit + 0.10 * proximityFit + 0.15 * qualityFit;
            int score = (int) Math.round(rawScore * 100);
            
            // Explanations
            List<String> reasons = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            
            if (capacityFit >= 0.8) reasons.add("Fits " + pkg.getMinCapacity() + "-" + pkg.getMaxCapacity() + " guests \u2014 right for this hall");
            else if (capacityFit <= 0.5) warnings.add("Capacity match is sub-optimal");
            
            if (themeFit >= 0.8 && request.getThemes() != null && !request.getThemes().isEmpty()) {
                Set<String> matchThemes = new HashSet<>(request.getThemes());
                matchThemes.retainAll(pkg.getThemeTags());
                if (!matchThemes.isEmpty()) {
                    reasons.add("Matches " + String.join(", ", matchThemes));
                }
            } else if (themeFit <= 0.5 && request.getThemes() != null && !request.getThemes().isEmpty()) {
                warnings.add("Does not match all requested themes");
            }
            
            if (budgetFit >= 0.8 && request.getBudgetMax() != null) {
                double diff = request.getBudgetMax().doubleValue() - price;
                if (diff >= 0) reasons.add(String.format("₹%.0f under budget", diff));
            } else if (budgetFit <= 0.5 && request.getBudgetMax() != null) {
                double diff = price - request.getBudgetMax().doubleValue();
                warnings.add(String.format("₹%.0f over budget", diff));
            }
            
            if (proximityFit >= 0.8) reasons.add(String.format("%.1f km away", distance));
            else if (proximityFit <= 0.5) warnings.add(String.format("%.1f km away", distance));
            
            if (qualityFit >= 0.8 && decorator.getTrustScore() != null) reasons.add("Trust " + decorator.getTrustScore());
            else if (qualityFit <= 0.6) reasons.add("New vendor");
            
            Map<String, Double> breakdown = new HashMap<>();
            breakdown.put("capacity", capacityFit);
            breakdown.put("layout", layoutFit);
            breakdown.put("theme", themeFit);
            breakdown.put("budget", budgetFit);
            breakdown.put("proximity", proximityFit);
            breakdown.put("quality", qualityFit);
            
            DecoratorMatchResponse.TrustDto trust = DecoratorMatchResponse.TrustDto.builder()
                    .score(decorator.getTrustScore())
                    .badge(decorator.getTrustScore() != null && decorator.getTrustScore() >= 80 ? "HIGH" : "STANDARD")
                    .build();
            
            DecoratorMatchResponse res = DecoratorMatchResponse.builder()
                    .decoratorId(decorator.getId())
                    .businessName(decorator.getBusinessName())
                    .packageId(pkg.getId())
                    .packageName(pkg.getName())
                    .score(score)
                    .priceEstimate(BigDecimal.valueOf(price))
                    .distanceKm(distance)
                    .trust(trust)
                    .reasons(reasons)
                    .warnings(warnings)
                    .breakdown(breakdown)
                    .build();
                    
            decoratorToPackages.computeIfAbsent(decorator.getId(), k -> new ArrayList<>()).add(res);
        }
        
        for (Map.Entry<Long, List<DecoratorMatchResponse>> entry : decoratorToPackages.entrySet()) {
            List<DecoratorMatchResponse> pkgs = entry.getValue();
            pkgs.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
            
            DecoratorMatchResponse best = pkgs.get(0);
            best.setAlternativePackages(pkgs.size() - 1);
            responses.add(best);
        }
        
        responses.sort((a, b) -> {
            int scoreCmp = Integer.compare(b.getScore(), a.getScore());
            if (scoreCmp != 0) return scoreCmp;
            int distCmp = Double.compare(a.getDistanceKm(), b.getDistanceKm());
            if (distCmp != 0) return distCmp;
            Integer trustA = a.getTrust().getScore() != null ? a.getTrust().getScore() : 0;
            Integer trustB = b.getTrust().getScore() != null ? b.getTrust().getScore() : 0;
            return Integer.compare(trustB, trustA);
        });
        
        return responses.stream().limit(limit).collect(Collectors.toList());
    }
    
    private double calculateCapacityFit(int S, int min, int max) {
        if (S >= min && S <= max) return 1.0;
        if (S < min) {
            double lowerBound = 0.8 * min;
            if (S <= lowerBound) return 0.0;
            return (S - lowerBound) / (min - lowerBound);
        } else {
            double upperBound = 1.25 * max;
            if (S >= upperBound) return 0.0;
            return (upperBound - S) / (upperBound - max);
        }
    }
    
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
