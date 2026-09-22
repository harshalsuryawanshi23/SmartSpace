package com.smartspace.booking.service;

import com.smartspace.common.exception.DomainException;
import com.smartspace.listing.entity.Hall;
import com.smartspace.listing.entity.HallStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.LocalTime;

@Component
public class BookingRulesValidator {

    private static final int MIN_LEAD_HOURS = 24;

    public void validate(Hall hall, Instant startAt, Instant endAt, int guestCount, boolean isMember, Instant now) {
        if (hall.getStatus() != HallStatus.ACTIVE) {
            throw new DomainException("HALL_INACTIVE", "Hall is not active for booking");
        }
        
        if (endAt.isBefore(startAt) || endAt.equals(startAt)) {
            throw new DomainException("INVALID_TIME_RANGE", "End time must be strictly after start time");
        }

        // Granularity: must be :00 or :30
        ZonedDateTime startZdt = startAt.atZone(ZoneId.of("UTC")); // Or Asia/Kolkata depending on spec, but let's assume aligned to UTC or local equally works for 30m
        ZonedDateTime endZdt = endAt.atZone(ZoneId.of("UTC"));
        if ((startZdt.getMinute() != 0 && startZdt.getMinute() != 30) || startZdt.getSecond() != 0) {
            throw new DomainException("INVALID_GRANULARITY", "Start time must be aligned to 30 minutes");
        }
        if ((endZdt.getMinute() != 0 && endZdt.getMinute() != 30) || endZdt.getSecond() != 0) {
            throw new DomainException("INVALID_GRANULARITY", "End time must be aligned to 30 minutes");
        }

        // Lead time
        Instant minLead = now.plus(Duration.ofHours(MIN_LEAD_HOURS));
        if (startAt.isBefore(minLead)) {
            throw new DomainException("MIN_LEAD_TIME", "Booking must be made at least 24 hours in advance");
        }

        // Advance window
        int maxDays = isMember ? hall.getAdvanceDaysMember() : hall.getAdvanceDaysPublic();
        Instant maxAdvance = now.plus(Duration.ofDays(maxDays));
        if (startAt.isAfter(maxAdvance)) {
            throw new DomainException("MAX_ADVANCE_TIME", "Cannot book beyond " + maxDays + " days in advance");
        }

        // Duration bounds
        long minutes = Duration.between(startAt, endAt).toMinutes();
        if (minutes < hall.getMinSlotMinutes()) {
            throw new DomainException("MIN_DURATION", "Booking duration is less than hall minimum " + hall.getMinSlotMinutes() + " mins");
        }
        if (minutes > hall.getMaxSlotMinutes()) {
            throw new DomainException("MAX_DURATION", "Booking duration is greater than hall maximum " + hall.getMaxSlotMinutes() + " mins");
        }

        // Capacity
        int maxCapacity = Math.max(
                hall.getCapacitySeated() != null ? hall.getCapacitySeated() : 0, 
                hall.getCapacityStanding() != null ? hall.getCapacityStanding() : 0);
        if (guestCount > maxCapacity) {
            throw new DomainException("CAPACITY_EXCEEDED", "Guest count exceeds hall capacity");
        }

        // Latest End Time
        if (hall.getLatestEndTime() != null) {
            LocalTime endLocalTime = endAt.atZone(ZoneId.of("Asia/Kolkata")).toLocalTime();
            if (endLocalTime.isAfter(hall.getLatestEndTime()) || (endLocalTime.equals(LocalTime.MIDNIGHT) && hall.getLatestEndTime().isBefore(LocalTime.MAX))) {
                throw new DomainException("LATEST_END_TIME", "Booking ends after the latest allowed end time");
            }
        }
        
        // Note: checking opening hours, blackouts and quiet hours requires DB queries (or passing them in).
        // Those will be checked during the slot availability build process.
    }
}
