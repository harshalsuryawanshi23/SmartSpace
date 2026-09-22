package com.smartspace.entry.job;

import com.smartspace.entry.service.EntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HallStatusJob {

    private final EntryService entryService;

    @Scheduled(fixedRate = 60000) // every 1 min for development
    public void sweepCleaningHalls() {
        log.debug("Running HallStatus job...");
        try {
            entryService.sweepCleaningHalls();
        } catch (Exception e) {
            log.error("Error sweeping Cleaning Halls", e);
        }
    }
}
