package com.smartspace.notification.service;

import com.smartspace.notification.adapter.NotificationAdapter;
import com.smartspace.notification.entity.NotificationOutbox;
import com.smartspace.notification.entity.OutboxStatus;
import com.smartspace.notification.repository.NotificationOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatcher {

    private final NotificationOutboxRepository outboxRepository;
    private final List<NotificationAdapter> adapters;
    private final Clock clock;

    @Scheduled(fixedDelay = 5000)
    public void dispatchPendingNotifications() {
        Instant now = clock.instant();
        List<NotificationOutbox> pending = outboxRepository.findPendingNotifications(
                OutboxStatus.PENDING, now, PageRequest.of(0, 50));

        for (NotificationOutbox outbox : pending) {
            try {
                NotificationAdapter adapter = getAdapter(outbox.getChannel());
                adapter.send(outbox);
                
                outbox.setStatus(OutboxStatus.SENT);
                outbox.setSentAt(now);
                outbox.setAttempts(outbox.getAttempts() + 1);
            } catch (Exception e) {
                log.error("Failed to send notification: {}", outbox.getId(), e);
                outbox.setLastError(e.getMessage());
                outbox.setAttempts(outbox.getAttempts() + 1);
                
                if (outbox.getAttempts() >= 3) {
                    outbox.setStatus(OutboxStatus.DEAD);
                } else {
                    outbox.setNextAttemptAt(now.plus(5, ChronoUnit.MINUTES));
                }
            } finally {
                outboxRepository.save(outbox);
            }
        }
    }

    private NotificationAdapter getAdapter(com.smartspace.notification.entity.NotificationChannel channel) {
        return adapters.stream()
                .filter(a -> a.supports(channel))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No adapter found for channel " + channel));
    }
}
