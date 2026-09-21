package com.smartspace.kyc.dto;

import jakarta.validation.constraints.NotBlank;

public record KycCompleteRequest(
    @NotBlank(message = "Session ID is required")
    String sessionId
) {}
