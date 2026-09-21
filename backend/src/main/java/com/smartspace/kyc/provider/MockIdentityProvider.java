package com.smartspace.kyc.provider;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockIdentityProvider implements IdentityProvider {

    @Override
    public String code() {
        return "MOCK";
    }

    @Override
    public KycSession start(KycStartRequest req) {
        String sessionId = UUID.randomUUID().toString();
        // Point to the frontend mock page
        String redirectUrl = "http://localhost:5173/kyc/mock?session=" + sessionId;
        return new KycSession(sessionId, redirectUrl);
    }

    @Override
    public KycResult fetchResult(String sessionId) {
        // The mock frontend sets the sessionId to contain the outcome for simplicity.
        // e.g., sessionId = "VERIFIED_..."
        // In a real provider, we'd call their API with the sessionId.
        
        if (sessionId.startsWith("FAILED")) {
            return new KycResult(false, sessionId, null, null, false, "Mock failure selected");
        }
        
        if (sessionId.startsWith("MISMATCH")) {
             return new KycResult(false, sessionId, "Wrong Name", "XXXX-XXXX-9999", true, "Name mismatch");
        }

        if (sessionId.startsWith("NOMOBILE")) {
             return new KycResult(true, sessionId, "Mock User", "XXXX-XXXX-1111", false, null);
        }

        // Default: VERIFIED
        String random4 = String.format("%04d", (int)(Math.random() * 10000));
        return new KycResult(true, sessionId, "Verified User", "XXXX-XXXX-" + random4, true, null);
    }
}
