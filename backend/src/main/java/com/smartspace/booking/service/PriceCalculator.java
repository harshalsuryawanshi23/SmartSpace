package com.smartspace.booking.service;

import com.smartspace.booking.dto.PriceBreakdown;
import com.smartspace.listing.entity.Hall;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;

@Component
public class PriceCalculator {

    private static final BigDecimal PLATFORM_FEE_PERCENT = new BigDecimal("5.0");
    private static final BigDecimal TAX_PERCENT = new BigDecimal("18.0");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100.0");

    public PriceBreakdown calculate(Hall hall, Instant startAt, Instant endAt, boolean isMember) {
        // Calculate number of hours
        long minutes = Duration.between(startAt, endAt).toMinutes();
        BigDecimal hours = new BigDecimal(minutes).divide(new BigDecimal(60), 2, RoundingMode.HALF_UP);

        // TODO: integrate specific HallPriceRules if any exist. For now use basePricePerHour.
        BigDecimal base = hall.getBasePricePerHour().multiply(hours).setScale(2, RoundingMode.HALF_UP);

        BigDecimal discount = BigDecimal.ZERO;
        if (isMember && hall.getMemberDiscountPercent() != null) {
            discount = base.multiply(hall.getMemberDiscountPercent())
                           .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
        }

        BigDecimal subtotal = base.subtract(discount);

        BigDecimal fee = subtotal.multiply(PLATFORM_FEE_PERCENT)
                                 .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);

        BigDecimal subtotalPlusFee = subtotal.add(fee);
        
        BigDecimal tax = subtotalPlusFee.multiply(TAX_PERCENT)
                                        .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);

        BigDecimal total = subtotalPlusFee.add(tax).setScale(2, RoundingMode.HALF_UP);

        return PriceBreakdown.builder()
                .basePrice(base)
                .memberDiscount(discount)
                .subtotal(subtotal)
                .platformFee(fee)
                .tax(tax)
                .total(total)
                .currency("INR")
                .build();
    }
}
