package com.smartspace.decorator.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RespondEnquiryRequest {
    private boolean accept;
    private BigDecimal quotedPrice;
    private String note;
}
