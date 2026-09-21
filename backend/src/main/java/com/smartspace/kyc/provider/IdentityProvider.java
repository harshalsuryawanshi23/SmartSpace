package com.smartspace.kyc.provider;

public interface IdentityProvider {
    String code();
    KycSession start(KycStartRequest req);
    KycResult fetchResult(String sessionId);
}
