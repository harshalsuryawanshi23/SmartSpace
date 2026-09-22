package com.smartspace.booking.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PriceBreakdown {
    private BigDecimal basePrice;
    private BigDecimal memberDiscount;
    private BigDecimal subtotal;
    private BigDecimal platformFee;
    private BigDecimal tax;
    private BigDecimal total;
    private String currency;
}
