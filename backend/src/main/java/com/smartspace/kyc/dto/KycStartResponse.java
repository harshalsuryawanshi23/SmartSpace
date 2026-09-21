package com.smartspace.kyc.dto;

public record KycStartResponse(
    String sessionId,
    String redirectUrl
) {}
