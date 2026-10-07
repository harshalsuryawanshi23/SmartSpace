package com.smartspace.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OwnerAnalyticsSummary {
    private long totalBookings;
    private long occupiedHours;
    private long availableHours;
    private BigDecimal occupancyPercentage;
    private BigDecimal grossRevenue;
    private BigDecimal ownerEarnings;
    private BigDecimal cancellationRate;
    private BigDecimal noShowRate;
    private BigDecimal avgHeadcountDeclaredRatio;
    private java.util.Map<String, Integer> usageHeatmap;
}
