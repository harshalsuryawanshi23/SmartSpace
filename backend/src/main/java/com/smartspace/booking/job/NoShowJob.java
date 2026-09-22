package com.smartspace.booking.job;

import com.smartspace.entry.service.EntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoShowJob {

    private final EntryService entryService;

    @Scheduled(fixedRate = 60000) // every 1 min for development
    public void sweepNoShows() {
        log.debug("Running NoShow job...");
        try {
            entryService.sweepNoShows();
        } catch (Exception e) {
            log.error("Error sweeping NoShows", e);
        }
    }
}
