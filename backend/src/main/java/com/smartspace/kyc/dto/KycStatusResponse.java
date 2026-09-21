package com.smartspace.kyc.dto;

import java.time.LocalDateTime;

public record KycStatusResponse(
    String status,
    String verifiedName,
    String maskedId,
    String assuranceLevel,
    LocalDateTime expiresAt
) {}
