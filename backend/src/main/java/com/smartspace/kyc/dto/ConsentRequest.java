package com.smartspace.kyc.dto;

import jakarta.validation.constraints.NotBlank;

public record ConsentRequest(
    @NotBlank(message = "Policy version is required")
    String policyVersion
) {}
