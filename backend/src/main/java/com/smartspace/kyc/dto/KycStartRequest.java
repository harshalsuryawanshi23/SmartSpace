package com.smartspace.kyc.dto;

import lombok.Data;

@Data
public class KycStartRequest {
    private String vendor; // DIGILOCKER, UIDAI, etc.
}
