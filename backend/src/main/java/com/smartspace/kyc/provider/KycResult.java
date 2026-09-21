package com.smartspace.kyc.provider;

public record KycResult(
    boolean verified,
    String providerRef,
    String verifiedName,
    String maskedId,
    boolean mobileLinked,
    String failureReason
) {}
