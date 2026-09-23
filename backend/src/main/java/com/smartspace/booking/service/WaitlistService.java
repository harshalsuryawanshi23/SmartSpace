package com.smartspace.booking.service;

import com.smartspace.booking.entity.Booking;
import com.smartspace.booking.entity.WaitlistEntry;
import com.smartspace.booking.repository.WaitlistEntryRepository;
import com.smartspace.listing.entity.Hall;
import com.smartspace.listing.repository.HallRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WaitlistService {

    private final WaitlistEntryRepository waitlistEntryRepository;
    private final HallRepository hallRepository;
    // For MVP, we aren't injecting BookingService directly to avoid circular dependency
    // We would send an event or call a method to create the PENDING_PAYMENT booking.

    @Transactional
    public void promote(Long hallId, Instant freedWindowStart, Instant freedWindowEnd) {
        log.info("Checking waitlist for hall {} in window {} - {}", hallId, freedWindowStart, freedWindowEnd);
        
        ZonedDateTime start = freedWindowStart.atZone(java.time.ZoneId.of("UTC"));
        ZonedDateTime end = freedWindowEnd.atZone(java.time.ZoneId.of("UTC"));

        List<WaitlistEntry> candidates = waitlistEntryRepository
                .findByHallIdAndStatusAndStartTimeBetweenOrderByCreatedAtAsc(hallId, "WAITING", start, end);

        for (WaitlistEntry entry : candidates) {
            ZonedDateTime entryEnd = entry.getStartTime().plusMinutes(entry.getDurationMinutes());
            if (!entry.getStartTime().isBefore(start) && !entryEnd.isAfter(end)) {
                log.info("Promoting waitlist entry {} for user {}", entry.getId(), entry.getUser().getId());
                
                // 1. Create a system-initiated PENDING_PAYMENT booking with 15-minute lock
                // This would be handled by sending a message or calling BookingService.
                // bookingService.createBookingFromWaitlist(entry);

                // 2. Update status
                entry.setStatus("OFFERED");
                waitlistEntryRepository.save(entry);

                // Only promote one candidate per freed slot (simplified MVP)
                break;
            }
        }
    }
}
