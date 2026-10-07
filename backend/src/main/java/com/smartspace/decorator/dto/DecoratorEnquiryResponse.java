package com.smartspace.decorator.dto;

import com.smartspace.decorator.entity.DecoratorEnquiry;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class DecoratorEnquiryResponse {
    private Long id;
    private String bookingRef;
    private String message;
    private DecoratorEnquiry.EnquiryStatus status;
    private String packageName;
    private BigDecimal quotedPrice;
    private String note;
    private LocalDateTime createdAt;
}
