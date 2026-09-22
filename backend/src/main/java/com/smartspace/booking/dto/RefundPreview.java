package com.smartspace.booking.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RefundPreview {
    private BigDecimal expectedRefund;
    private String policyApplied;
    private String reason;
}
