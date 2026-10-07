package com.smartspace.kyc.dto;

import lombok.Data;

@Data
public class KycResult {
    private String status; // VERIFIED, REJECTED
    private String reason;
}
