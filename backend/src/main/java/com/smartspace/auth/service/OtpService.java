package com.smartspace.auth.service;

import com.smartspace.auth.entity.OtpChallenge;
import com.smartspace.auth.entity.OtpPurpose;
import com.smartspace.auth.repository.OtpChallengeRepository;
import com.smartspace.common.exception.DomainException;
import com.smartspace.common.exception.ErrorCode;
import com.smartspace.common.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpChallengeRepository otpChallengeRepository;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.security.otp-pepper}")
    private String otpPepper;

    @Transactional
    public String generateAndSaveOtp(String target, OtpPurpose purpose, Long userId) {
        // Prevent spamming
        Optional<OtpChallenge> existing = otpChallengeRepository.findTopByTargetAndPurposeOrderByCreatedAtDesc(target, purpose);
        if (existing.isPresent()) {
            OtpChallenge challenge = existing.get();
            if (challenge.getCreatedAt().plus(1, ChronoUnit.MINUTES).isAfter(clock.instant())) {
                throw new DomainException(ErrorCode.RATE_LIMIT_EXCEEDED, "Please wait before requesting another OTP");
            }
        }

        String rawOtp = String.format("%06d", secureRandom.nextInt(1000000));
        String codeHash = hashOtp(rawOtp);

        OtpChallenge challenge = OtpChallenge.builder()
                .publicId(IdGenerator.generatePublicId("otp"))
                .target(target)
                .purpose(purpose)
                .userId(userId)
                .codeHash(codeHash)
                .expiresAt(clock.instant().plus(10, ChronoUnit.MINUTES))
                .build();

        otpChallengeRepository.save(challenge);

        // TODO: Enqueue to outbox

        return rawOtp; // Returned temporarily for SMS/Email adapter hooking later
    }

    @Transactional
    public void verifyOtp(String publicId, String target, OtpPurpose purpose, String rawOtp) {
        OtpChallenge challenge = otpChallengeRepository.findByPublicId(publicId)
                .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND, "OTP challenge not found"));

        if (!challenge.getTarget().equals(target) || challenge.getPurpose() != purpose) {
            throw new DomainException(ErrorCode.VALIDATION_ERROR, "Invalid OTP challenge");
        }

        if (challenge.getVerifiedAt() != null) {
            throw new DomainException(ErrorCode.VALIDATION_ERROR, "OTP already verified");
        }

        if (challenge.getExpiresAt().isBefore(clock.instant())) {
            throw new DomainException(ErrorCode.VALIDATION_ERROR, "OTP expired");
        }

        if (challenge.getAttempts() >= challenge.getMaxAttempts()) {
            throw new DomainException(ErrorCode.VALIDATION_ERROR, "Max OTP attempts exceeded");
        }

        challenge.setAttempts(challenge.getAttempts() + 1);

        String expectedHash = hashOtp(rawOtp);
        if (!expectedHash.equals(challenge.getCodeHash())) {
            otpChallengeRepository.save(challenge);
            throw new DomainException(ErrorCode.UNAUTHORIZED, "Invalid OTP");
        }

        challenge.setVerifiedAt(clock.instant());
        otpChallengeRepository.save(challenge);
    }

    private String hashOtp(String rawOtp) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(otpPepper.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(rawOtp.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hmacBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash OTP", e);
        }
    }
}
