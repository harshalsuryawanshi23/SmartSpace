package com.smartspace.booking.dto;

import lombok.Data;

import java.math.BigDecimal;

import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RefundPreview {
    private BigDecimal expectedRefund;
    private String policyApplied;
    private String reason;
}
