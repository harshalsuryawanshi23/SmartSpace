package com.smartspace.booking.job;

import com.smartspace.booking.service.LockExpiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LockExpiryJob {

    private final LockExpiryService lockExpiryService;

    @Scheduled(fixedRate = 30000) // every 30 seconds
    public void sweepExpiredLocks() {
        log.debug("Running lock expiry job...");
        try {
            lockExpiryService.sweepExpiredLocks();
        } catch (Exception e) {
            log.error("Error sweeping expired locks", e);
        }
    }
}
