package com.smartspace.decorator.dto;

import com.smartspace.listing.entity.LayoutType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MatchContext {
    private Integer capacitySeated;
    private Integer capacityStanding;
    private LayoutType layoutType;
    private BigDecimal lat;
    private BigDecimal lng;
    private Integer powerPoints;
    
    private String eventType;
    private List<String> themes;
    private Integer guestCount;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    
    private LocalDateTime slotStart;
    private LocalDateTime slotEnd;
}
