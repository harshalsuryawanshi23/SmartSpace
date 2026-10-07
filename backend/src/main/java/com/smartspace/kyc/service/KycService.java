package com.smartspace.kyc.service;

import com.smartspace.common.exception.DomainException;
import com.smartspace.kyc.dto.ConsentRequest;
import com.smartspace.kyc.dto.KycResult;
import com.smartspace.kyc.dto.KycStartRequest;
import com.smartspace.kyc.dto.KycStartResponse;
import com.smartspace.kyc.dto.KycStatusResponse;
import com.smartspace.kyc.entity.Consent;
import com.smartspace.kyc.entity.ConsentPurpose;
import com.smartspace.kyc.entity.KycStatus;
import com.smartspace.kyc.entity.KycVerification;
import com.smartspace.kyc.provider.IdentityProvider;
import com.smartspace.kyc.provider.KycSession;
import com.smartspace.kyc.repository.ConsentRepository;
import com.smartspace.kyc.repository.KycVerificationRepository;
import com.smartspace.user.entity.User;
import com.smartspace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KycService {

    private final ConsentRepository consentRepository;
    private final KycVerificationRepository kycVerificationRepository;
    private final IdentityProvider identityProvider;
    private final UserRepository userRepository;
    private final Clock clock;

    @Transactional
    public void recordConsent(Long userId, ConsentRequest request, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException(com.smartspace.common.exception.ErrorCode.VALIDATION_ERROR, "User not found"));

        Consent consent = new Consent();
        consent.setUser(user);
        consent.setPurpose(ConsentPurpose.KYC);
        consent.setPolicyVersion(request.policyVersion());
        consent.setGrantedAt(LocalDateTime.now(clock));
        consent.setIp(ipAddress);

        consentRepository.save(consent);
    }

    @Transactional
    public KycStartResponse startKyc(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException(com.smartspace.common.exception.ErrorCode.VALIDATION_ERROR, "User not found"));

        Consent activeConsent = consentRepository.findFirstByUserIdAndPurposeAndRevokedAtIsNullOrderByGrantedAtDesc(userId, ConsentPurpose.KYC)
                .orElseThrow(() -> new DomainException(com.smartspace.common.exception.ErrorCode.VALIDATION_ERROR, "Active KYC consent is required"));

        KycSession session = identityProvider.start(new com.smartspace.kyc.provider.KycStartRequest(userId));

        KycVerification verification = new KycVerification();
        verification.setUser(user);
        verification.setProvider(identityProvider.code());
        verification.setProviderRef(session.sessionId());
        verification.setStatus(KycStatus.PENDING);
        verification.setConsent(activeConsent);
        verification.setCreatedAt(LocalDateTime.now(clock));
        verification.setUpdatedAt(LocalDateTime.now(clock));
        
        kycVerificationRepository.save(verification);

        return new KycStartResponse(session.sessionId(), session.redirectUrl());
    }

    @Transactional
    public void completeKyc(Long userId, String sessionId) {
        KycVerification verification = kycVerificationRepository.findFirstByProviderRef(sessionId)
                .orElseThrow(() -> new DomainException(com.smartspace.common.exception.ErrorCode.VALIDATION_ERROR, "KYC session not found"));

        if (!verification.getUser().getId().equals(userId)) {
            throw new DomainException(com.smartspace.common.exception.ErrorCode.FORBIDDEN, "Unauthorized KYC session");
        }

        if (verification.getStatus() != KycStatus.PENDING) {
            throw new DomainException(com.smartspace.common.exception.ErrorCode.ILLEGAL_STATE_TRANSITION, "KYC session is not pending");
        }

        com.smartspace.kyc.provider.KycResult result = identityProvider.fetchResult(sessionId);
        
        LocalDateTime now = LocalDateTime.now(clock);
        verification.setUpdatedAt(now);

        if (result.verified()) {
            verification.setStatus(KycStatus.VERIFIED);
            verification.setVerifiedName(result.verifiedName());
            verification.setMaskedId(result.maskedId());
            verification.setMobileLinked(result.mobileLinked());
            verification.setVerifiedAt(now);
            verification.setExpiresAt(now.plusMonths(12)); // Default 12 months expiry
        } else {
            verification.setStatus(KycStatus.FAILED);
            verification.setFailureReason(result.failureReason());
        }

        kycVerificationRepository.save(verification);
    }

    @Transactional(readOnly = true)
    public KycStatusResponse getKycStatus(Long userId) {
        Optional<KycVerification> latestOpt = kycVerificationRepository.findLatestByUserIdAndStatusIn(userId, 
                List.of(KycStatus.VERIFIED, KycStatus.PENDING, KycStatus.FAILED));

        if (latestOpt.isEmpty()) {
            return new KycStatusResponse("NONE", null, null, "NONE", null);
        }

        KycVerification latest = latestOpt.get();
        String assuranceLevel = "NONE";
        
        if (latest.getStatus() == KycStatus.VERIFIED) {
            if (latest.isMobileLinked() && latest.getUser().getPhoneVerifiedAt() != null) {
                assuranceLevel = "HIGH";
            } else {
                assuranceLevel = "STANDARD";
            }
        }

        return new KycStatusResponse(
            latest.getStatus().name(),
            latest.getVerifiedName(),
            latest.getMaskedId(),
            assuranceLevel,
            latest.getExpiresAt()
        );
    }

    @Transactional
    public void revokeKyc(Long userId) {
        // Revoke consent
        consentRepository.findFirstByUserIdAndPurposeAndRevokedAtIsNullOrderByGrantedAtDesc(userId, ConsentPurpose.KYC)
            .ifPresent(consent -> {
                consent.setRevokedAt(LocalDateTime.now(clock));
                consentRepository.save(consent);
            });

        // Revoke verifications
        kycVerificationRepository.findLatestByUserIdAndStatusIn(userId, List.of(KycStatus.VERIFIED))
            .ifPresent(verification -> {
                verification.setStatus(KycStatus.REVOKED);
                verification.setUpdatedAt(LocalDateTime.now(clock));
                kycVerificationRepository.save(verification);
            });
    }

    @Transactional(readOnly = true)
    public boolean hasValidKyc(Long userId) {
        Optional<KycVerification> latestOpt = kycVerificationRepository.findLatestByUserIdAndStatusIn(userId, List.of(KycStatus.VERIFIED));
        if (latestOpt.isEmpty()) {
            return false;
        }
        KycVerification latest = latestOpt.get();
        return latest.getExpiresAt().isAfter(LocalDateTime.now(clock));
    }
}
