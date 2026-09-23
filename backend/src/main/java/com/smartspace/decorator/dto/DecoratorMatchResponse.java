package com.smartspace.decorator.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DecoratorMatchResponse {
    private Long decoratorId;
    private String businessName;
    private Long packageId;
    private String packageName;
    private Integer score;
    private BigDecimal priceEstimate;
    private Double distanceKm;
    
    private TrustDto trust;
    
    private List<String> reasons;
    private List<String> warnings;
    
    private Integer alternativePackages;
    
    private Map<String, Double> breakdown;

    @Data
    @Builder
    public static class TrustDto {
        private Integer score;
        private String badge;
    }
}
