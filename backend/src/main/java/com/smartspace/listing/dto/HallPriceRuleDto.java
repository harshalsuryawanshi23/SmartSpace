package com.smartspace.listing.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class HallPriceRuleDto {
    private String id;
    private String label;
    private Integer daysMask;
    private LocalTime fromTime;
    private LocalTime toTime;
    private BigDecimal pricePerHour;
    private Integer priority;
    private Boolean active;
}
