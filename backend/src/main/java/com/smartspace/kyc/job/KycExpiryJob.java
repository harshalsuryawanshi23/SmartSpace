package com.smartspace.kyc.job;

import com.smartspace.kyc.entity.KycVerification;
import com.smartspace.kyc.repository.KycVerificationRepository;
import com.smartspace.notification.entity.NotificationChannel;
import com.smartspace.notification.entity.NotificationOutbox;
import com.smartspace.notification.repository.NotificationOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class KycExpiryJob {

    private final KycVerificationRepository kycVerificationRepository;
    private final NotificationOutboxRepository notificationOutboxRepository;
    private final Clock clock;

    /**
     * Run daily at 02:00
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void notifyExpiringKyc() {
        log.info("Starting KycExpiryJob");

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime threshold = now.plusDays(14); // 14 days warning

        List<KycVerification> expiringSoon = kycVerificationRepository.findExpiringSoon(now, threshold);

        int count = 0;
        for (KycVerification verification : expiringSoon) {
            String dedupeKey = "KYC_EXPIRY_WARN_" + verification.getId();
            
            // Check if we already created a warning for this verification
            if (!notificationOutboxRepository.existsByDedupeKey(dedupeKey)) {
                NotificationOutbox notification = NotificationOutbox.builder()
                        .userId(verification.getUser().getId())
                        .channel(NotificationChannel.EMAIL) // Default to email for important warnings
                        .templateCode("KYC_EXPIRING")
                        .language(verification.getUser().getPreferredLanguage().name().toLowerCase())
                        .destination(verification.getUser().getEmail())
                        .payload("{\"expiresAt\": \"" + verification.getExpiresAt().toString() + "\"}")
                        .nextAttemptAt(Instant.now(clock))
                        .dedupeKey(dedupeKey)
                        .build();

                notificationOutboxRepository.save(notification);
                count++;
            }
        }

        log.info("Finished KycExpiryJob, created {} warning notifications", count);
    }
}
