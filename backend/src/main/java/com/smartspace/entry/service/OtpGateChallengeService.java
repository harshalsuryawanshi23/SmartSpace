package com.smartspace.entry.service;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class OtpGateChallengeService {

    private final Map<String, OtpSession> sessions = new ConcurrentHashMap<>();
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Data
    @Builder
    public static class OtpSession {
        private String challengeId;
        private Long bookingId;
        private String phone;
        private String hashedOtp;
        private Instant expiresAt;
        private int attempts;
        private boolean locked;
    }

    public OtpSession createChallenge(Long bookingId, String phone) {
        String challengeId = UUID.randomUUID().toString();
        // Generate random 6 digit OTP
        String otp = String.format("%06d", (int)(Math.random() * 1000000));
        
        // Print to log since we don't have SMS setup
        log.info("========== OTP GENERATED ==========");
        log.info("Challenge ID: {}", challengeId);
        log.info("Phone: {}", phone);
        log.info("OTP: {}", otp);
        log.info("===================================");

        OtpSession session = OtpSession.builder()
                .challengeId(challengeId)
                .bookingId(bookingId)
                .phone(phone)
                .hashedOtp(passwordEncoder.encode(otp))
                .expiresAt(Instant.now().plusSeconds(180)) // 3 minutes
                .attempts(0)
                .locked(false)
                .build();
                
        sessions.put(challengeId, session);
        return session;
    }

    public boolean verify(String challengeId, String otp) {
        OtpSession session = sessions.get(challengeId);
        if (session == null) {
            log.warn("OTP Verify failed: challenge {} not found", challengeId);
            return false;
        }
        
        if (session.isLocked()) {
            log.warn("OTP Verify failed: challenge {} is locked", challengeId);
            return false;
        }
        
        if (Instant.now().isAfter(session.getExpiresAt())) {
            log.warn("OTP Verify failed: challenge {} expired", challengeId);
            return false;
        }
        
        session.setAttempts(session.getAttempts() + 1);
        
        if (passwordEncoder.matches(otp, session.getHashedOtp())) {
            sessions.remove(challengeId);
            return true;
        } else {
            if (session.getAttempts() >= 3) {
                session.setLocked(true);
                log.warn("OTP Verify failed: challenge {} locked after 3 attempts", challengeId);
            }
            return false;
        }
    }
    
    public OtpSession getSession(String challengeId) {
        return sessions.get(challengeId);
    }
}
