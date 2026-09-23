package com.smartspace.decorator.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DecoratorMatchRequest {
    private Long hallId;
    private String eventType;
    private List<String> themes;
    private Integer guestCount;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private LocalDateTime slotStart;
    private LocalDateTime slotEnd;
}
